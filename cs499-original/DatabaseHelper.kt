package com.example.project3

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "WeightTrackerDB"
        private const val DATABASE_VERSION = 1

        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"

        // Weight entries table
        private const val TABLE_WEIGHTS = "weights"
        private const val COLUMN_WEIGHT_ID = "weight_id"
        private const val COLUMN_WEIGHT = "weight"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_NOTES = "notes"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create users table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL
            )
        """.trimIndent()

        // Create weights table
        val createWeightsTable = """
            CREATE TABLE $TABLE_WEIGHTS (
                $COLUMN_WEIGHT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_WEIGHT REAL NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_NOTES TEXT,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createWeightsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WEIGHTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // User operations
    fun addUser(username: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun validateUser(username: String, password: String): Long {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, password),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
        } else {
            -1
        }.also {
            cursor.close()
        }
    }

    // Weight operations
    fun addWeight(userId: Long, weight: Double, date: String, notes: String? = null): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_WEIGHT, weight)
            put(COLUMN_DATE, date)
            put(COLUMN_NOTES, notes)
        }
        return db.insert(TABLE_WEIGHTS, null, values)
    }

    fun updateWeight(weightId: Long, weight: Double, date: String, notes: String? = null): Int {
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
}