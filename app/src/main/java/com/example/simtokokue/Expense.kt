package com.example.simtokokue

data class Expense(
    val id: String,
    val description: String,
    val amount: Double,
    val category: String,
    val date: Long
)
