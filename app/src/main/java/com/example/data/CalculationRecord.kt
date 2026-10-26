package com.example.data

data class CalculationRecord(
    val id: String,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String = "REAL" // REAL, COMPLEX, MATRIX
)
