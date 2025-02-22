package com.example.project3

class WeightRepository(private val dbHelper: DatabaseHelper) {
    fun getAllWeightsForUser(userId: Long): List<WeightEntry> {
        return dbHelper.getAllWeightsForUser(userId)
    }

    fun addWeight(userId: Long, weight: Double, date: String): Long {
        return dbHelper.addWeight(userId, weight, date)
    }

    fun updateWeight(weightId: Long, weight: Double, date: String): Int {
        return dbHelper.updateWeight(weightId, weight, date)
    }

    fun deleteWeight(weightId: Long): Int {
        return dbHelper.deleteWeight(weightId)
    }
}