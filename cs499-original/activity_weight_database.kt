package com.example.project3

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_database)

        userId = intent.getLongExtra("USER_ID", -1)
        if (userId == -1L) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        dbHelper = DatabaseHelper(this)
        setupUI()
        loadWeights()
    }

    private fun setupUI() {
        // Setup date picker
        dateInput = findViewById(R.id.dateInput)
        dateInput.setOnClickListener { showDatePicker() }
        dateInput.setText(dateFormat.format(calendar.time))

        // Setup weight input
        weightInput = findViewById(R.id.weightInput)

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.weightRecyclerView)
        adapter = WeightAdapter(
            weights = emptyList(),
            onEditClick = { weight -> showEditDialog(weight) },
            onDeleteClick = { weight -> showDeleteConfirmation(weight) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Setup add button
        findViewById<MaterialButton>(R.id.addButton).setOnClickListener {
            addWeight()
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

    private fun addWeight() {
        val weightStr = weightInput.text?.toString() ?: ""
        val date = dateInput.text?.toString() ?: ""

        if (weightStr.isEmpty()) {
            weightInput.error = "Please enter weight"
            return
        }

        try {
            val weight = weightStr.toDouble()
            val weightId = dbHelper.addWeight(userId, weight, date)
            if (weightId != -1L) {
                Toast.makeText(this, "Weight entry added", Toast.LENGTH_SHORT).show()
                weightInput.text?.clear()
                loadWeights()
            }
        } catch (e: NumberFormatException) {
            weightInput.error = "Invalid weight value"
        }
    }

    private fun showEditDialog(weightEntry: WeightEntry) {
        // Set the date and weight inputs to the current values
        dateInput.setText(weightEntry.date)
        weightInput.setText(weightEntry.weight.toString())

        // Change the add button temporarily
        val addButton = findViewById<MaterialButton>(R.id.addButton)
        val originalText = addButton.text
        addButton.text = "Update Entry"

        addButton.setOnClickListener {
            updateWeight(weightEntry.id)
            addButton.text = originalText
            // Reset the button click listener
            addButton.setOnClickListener { addWeight() }
        }
    }

    private fun updateWeight(weightId: Long) {
        val weightStr = weightInput.text?.toString() ?: ""
        val date = dateInput.text?.toString() ?: ""

        try {
            val weight = weightStr.toDouble()
            val result = dbHelper.updateWeight(weightId, weight, date)
            if (result > 0) {
                Toast.makeText(this, "Weight entry updated", Toast.LENGTH_SHORT).show()
                weightInput.text?.clear()
                loadWeights()
            }
        } catch (e: NumberFormatException) {
            weightInput.error = "Invalid weight value"
        }
    }

    private fun showDeleteConfirmation(weightEntry: WeightEntry) {
        AlertDialog.Builder(this)
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this weight entry?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteWeight(weightEntry.id)
                loadWeights()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadWeights() {
        val weights = dbHelper.getAllWeightsForUser(userId)
        adapter.updateData(weights)
    }
}