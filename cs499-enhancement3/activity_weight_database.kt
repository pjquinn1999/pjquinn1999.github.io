package com.example.project3

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class activity_weight_database : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: WeightAdapter
    private lateinit var dateInput: TextInputEditText
    private lateinit var weightInput: TextInputEditText
    private var userId: Long = -1
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()
    private var viewMode: String = "ADD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_database)

        userId = intent.getLongExtra("USER_ID", -1)
        viewMode = intent.getStringExtra("VIEW_MODE") ?: "ADD"

        if (userId == -1L) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        dbHelper = DatabaseHelper(this)
        setupUI()
        loadData()

        // Set the title based on view mode
        supportActionBar?.title = when(viewMode) {
            "CALENDAR" -> "Weight Calendar"
            "EDIT" -> "Edit Weight"
            else -> "Add Weight"
        }
    }

    private fun setupUI() {
        dateInput = findViewById(R.id.dateInput)
        weightInput = findViewById(R.id.weightInput)

        dateInput.setOnClickListener { showDatePicker() }
        dateInput.setText(dateFormat.format(calendar.time))

        val recyclerView = findViewById<RecyclerView>(R.id.weightRecyclerView)
        adapter = WeightAdapter(
            weights = emptyList(),
            onEditClick = { weight -> showEditDialog(weight) },
            onDeleteClick = { weight -> showDeleteConfirmation(weight) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val addButton = findViewById<MaterialButton>(R.id.addButton)
        val inputLayout = findViewById<View>(R.id.inputLayout)

        when (viewMode) {
            "CALENDAR" -> {
                // Hide only the input layout in calendar mode
                inputLayout.visibility = View.GONE
                addButton.text = "Add Weight Entry"
                addButton.setOnClickListener {
                    // Switch to ADD mode
                    viewMode = "ADD"
                    inputLayout.visibility = View.VISIBLE
                    addButton.text = "Add Entry"
                    addButton.setOnClickListener { addWeight() }
                    supportActionBar?.title = "Add Weight"
                }
            }
            "EDIT" -> {
                addButton.text = "Update Entry"
                // Handle edit mode setup...
                intent.getLongExtra("WEIGHT_ID", -1).takeIf { it != -1L }?.let { weightId ->
                    val weight = dbHelper.getWeight(weightId)
                    weight?.let { showEditDialog(it) }
                }
            }
            else -> {
                addButton.text = "Add Entry"
                addButton.setOnClickListener { addWeight() }
            }
        }
    }


    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                dateInput.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showDeleteConfirmation(weightEntry: WeightEntry) {
        AlertDialog.Builder(this)
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this weight entry?")
            .setPositiveButton("Delete") { _, _ ->
                try {
                    val result = dbHelper.deleteWeight(weightEntry.id)
                    if (result > 0) {
                        Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
                        loadData()
                        setResult(RESULT_OK)
                    } else {
                        Toast.makeText(this, "Error deleting entry", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(weightEntry: WeightEntry) {
        dateInput.setText(weightEntry.date)
        weightInput.setText(weightEntry.weight.toString())

        val addButton = findViewById<MaterialButton>(R.id.addButton)
        val originalText = addButton.text
        addButton.text = "Update Entry"

        addButton.setOnClickListener {
            updateWeight(weightEntry.id)
            addButton.text = originalText
            addButton.setOnClickListener { addWeight() }
        }
    }

    private fun loadData() {
        try {
            val weights = dbHelper.getAllWeightsForUser(userId)
            adapter.updateData(weights)
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addWeight() {
        try {
            val weightStr = weightInput.text?.toString()?.trim() ?: ""
            val date = dateInput.text?.toString() ?: ""

            if (weightStr.isEmpty()) {
                weightInput.error = "Please enter weight"
                return
            }

            if (date.isEmpty()) {
                dateInput.error = "Please select a date"
                return
            }

            val weight = weightStr.toDouble()
            if (weight <= 0 || weight >= 1000) {
                weightInput.error = "Weight must be between 0 and 1000 lbs"
                return
            }

            val weightId = dbHelper.addWeight(userId, weight, date)
            if (weightId != -1L) {
                Toast.makeText(this, "Weight entry added", Toast.LENGTH_SHORT).show()
                weightInput.text?.clear()
                loadData()
                setResult(RESULT_OK)
            } else {
                Toast.makeText(this, "Error adding weight entry", Toast.LENGTH_SHORT).show()
            }
        } catch (e: NumberFormatException) {
            weightInput.error = "Invalid weight value"
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWeight(weightId: Long) {
        try {
            val weightStr = weightInput.text?.toString()?.trim() ?: ""
            val date = dateInput.text?.toString() ?: ""

            if (weightStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return
            }

            val weight = weightStr.toDouble()
            if (weight <= 0 || weight >= 1000) {
                weightInput.error = "Weight must be between 0 and 1000 lbs"
                return
            }

            val result = dbHelper.updateWeight(weightId, weight, date)
            if (result > 0) {
                Toast.makeText(this, "Weight entry updated", Toast.LENGTH_SHORT).show()
                weightInput.text?.clear()
                loadData()
                setResult(RESULT_OK)
            } else {
                Toast.makeText(this, "Error updating weight entry", Toast.LENGTH_SHORT).show()
            }
        } catch (e: NumberFormatException) {
            weightInput.error = "Invalid weight value"
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}