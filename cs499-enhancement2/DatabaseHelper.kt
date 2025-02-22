package com.example.project3

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64
import java.util.regex.Pattern

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "WeightTrackerDB"
        private const val DATABASE_VERSION = 2  // Incremented for schema changes

        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_SALT = "salt"  // New column for password salt

        // Weight entries table
        private const val TABLE_WEIGHTS = "weights"
        private const val COLUMN_WEIGHT_ID = "weight_id"
        private const val COLUMN_WEIGHT = "weight"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_NOTES = "notes"

        // Security constants
        private const val SALT_LENGTH = 16
        private const val MIN_PASSWORD_LENGTH = 8
        private val USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$")
        private const val MAX_NOTE_LENGTH = 1000
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create users table with salt column
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_SALT TEXT NOT NULL
            )
        """.trimIndent()

        // Create weights table with additional constraints
        val createWeightsTable = """
            CREATE TABLE $TABLE_WEIGHTS (
                $COLUMN_WEIGHT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_WEIGHT REAL NOT NULL CHECK ($COLUMN_WEIGHT > 0 AND $COLUMN_WEIGHT < 1000),
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_NOTES TEXT CHECK (length($COLUMN_NOTES) <= $MAX_NOTE_LENGTH),
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
                    ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createWeightsTable)

        // Create indices for better query performance
        db.execSQL("CREATE INDEX idx_weights_user_date ON $TABLE_WEIGHTS($COLUMN_USER_ID, $COLUMN_DATE)")
        db.execSQL("CREATE INDEX idx_username ON $TABLE_USERS($COLUMN_USERNAME)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add salt column to users table
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_SALT TEXT DEFAULT ''")
            // Rehash existing passwords with salt
            migratePasswords(db)
        }
    }

    private fun migratePasswords(db: SQLiteDatabase) {
        val cursor = db.query(TABLE_USERS, arrayOf(COLUMN_USER_ID, COLUMN_PASSWORD), null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val userId = it.getLong(it.getColumnIndexOrThrow(COLUMN_USER_ID))
                val oldPassword = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD))

                // Generate new salt and hash password
                val salt = generateSalt()
                val newHashedPassword = hashPassword(oldPassword, salt)

                // Update user record
                val values = ContentValues().apply {
                    put(COLUMN_PASSWORD, newHashedPassword)
                    put(COLUMN_SALT, Base64.encodeToString(salt, Base64.DEFAULT))
                }
                db.update(TABLE_USERS, values, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
            }
        }
    }

    // Security helper functions
    private fun isValidUsername(username: String): Boolean {
        return USERNAME_PATTERN.matcher(username).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH &&
                password.any { it.isDigit() } &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { !it.isLetterOrDigit() }
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        val hashedBytes = md.digest(password.toByteArray())
        return Base64.encodeToString(hashedBytes, Base64.DEFAULT)
    }

    // User operations with enhanced security
    @Throws(IllegalArgumentException::class)
    fun addUser(username: String, password: String): Long {
        if (!isValidUsername(username)) {
            throw IllegalArgumentException("Invalid username format")
        }
        if (!isValidPassword(password)) {
            throw IllegalArgumentException("Password does not meet security requirements")
        }

        val db = this.writableDatabase
        val salt = generateSalt()
        val hashedPassword = hashPassword(password, salt)

        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username.trim())
            put(COLUMN_PASSWORD, hashedPassword)
            put(COLUMN_SALT, Base64.encodeToString(salt, Base64.DEFAULT))
        }

        return db.insert(TABLE_USERS, null, values)
    }

    fun validateUser(username: String, password: String): Long {
        if (!isValidUsername(username) || password.isEmpty()) {
            return -1
        }

        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID, COLUMN_PASSWORD, COLUMN_SALT),
            "$COLUMN_USERNAME = ?",
            arrayOf(username.trim()),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                val storedSalt = Base64.decode(
                    it.getString(it.getColumnIndexOrThrow(COLUMN_SALT)),
                    Base64.DEFAULT
                )
                val hashedPassword = hashPassword(password, storedSalt)
                val storedPassword = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD))

                if (MessageDigest.isEqual(
                        hashedPassword.toByteArray(),
                        storedPassword.toByteArray()
                    )) {
                    it.getLong(it.getColumnIndexOrThrow(COLUMN_USER_ID))
                } else {
                    -1
                }
            } else {
                -1
            }
        }
    }

    // Weight operations with input validation
    @Throws(IllegalArgumentException::class)
    fun addWeight(userId: Long, weight: Double, date: String, notes: String? = null): Long {
        if (weight <= 0 || weight >= 1000) {
            throw IllegalArgumentException("Invalid weight value")
        }
        if (notes != null && notes.length > MAX_NOTE_LENGTH) {
            throw IllegalArgumentException("Notes exceed maximum length")
        }

        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_WEIGHT, weight)
            put(COLUMN_DATE, date)
            put(COLUMN_NOTES, notes)
        }
        return db.insert(TABLE_WEIGHTS, null, values)
    }

    @Throws(IllegalArgumentException::class)
    fun updateWeight(weightId: Long, weight: Double, date: String, notes: String? = null): Int {
        if (weight <= 0 || weight >= 1000) {
            throw IllegalArgumentException("Invalid weight value")
        }
        if (notes != null && notes.length > MAX_NOTE_LENGTH) {
            throw IllegalArgumentException("Notes exceed maximum length")
        }

        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_WEIGHT, weight)
            put(COLUMN_DATE, date)
            put(COLUMN_NOTES, notes)
        }
        return db.update(TABLE_WEIGHTS, values, "$COLUMN_WEIGHT_ID = ?", arrayOf(weightId.toString()))
    }

    fun deleteWeight(weightId: Long): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_WEIGHTS, "$COLUMN_WEIGHT_ID = ?", arrayOf(weightId.toString()))
    }

    fun getAllWeightsForUser(userId: Long): List<WeightEntry> {
        val weights = mutableListOf<WeightEntry>()
        val db = this.readableDatabase

        val cursor = db.query(
            TABLE_WEIGHTS,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_DATE DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                weights.add(
                    WeightEntry(
                        id = it.getLong(it.getColumnIndexOrThrow(COLUMN_WEIGHT_ID)),
                        userId = it.getLong(it.getColumnIndexOrThrow(COLUMN_USER_ID)),
                        weight = it.getDouble(it.getColumnIndexOrThrow(COLUMN_WEIGHT)),
                        date = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE)),
                        notes = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTES))
                    )
                )
            }
        }
        return weights
    }


    fun getWeight(weightId: Long): WeightEntry? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_WEIGHTS,
            null,
            "$COLUMN_WEIGHT_ID = ?",
            arrayOf(weightId.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                WeightEntry(
                    id = it.getLong(it.getColumnIndexOrThrow(COLUMN_WEIGHT_ID)),
                    userId = it.getLong(it.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    weight = it.getDouble(it.getColumnIndexOrThrow(COLUMN_WEIGHT)),
                    date = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE)),
                    notes = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTES))
                )
            } else null
        }
    }
}