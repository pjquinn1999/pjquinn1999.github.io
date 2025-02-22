package com.example.project3

import androidx.lifecycle.ViewModel

class WeightViewModel(private val repository: WeightRepository) : ViewModel() {
    private var weights: List<WeightEntry> = emptyList()

    fun loadWeights(userId: Long) {
        weights = repository.getAllWeightsForUser(userId)
    }

    fun getWeights(): List<WeightEntry> = weights

    fun addWeight(userId: Long, weight: Double, date: String): Long {
        return repository.addWeight(userId, weight, date)
    }

    fun updateWeight(weightId: Long, weight: Double, date: String): Int {
        return repository.updateWeight(weightId, weight, date)
    }

    fun deleteWeight(weightId: Long): Int {
        return repository.deleteWeight(weightId)
    }
}