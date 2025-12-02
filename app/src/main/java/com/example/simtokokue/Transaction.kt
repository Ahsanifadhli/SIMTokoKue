package com.example.simtokokue

data class Transaction(
    val id: String,
    val date: Long,
    val items: List<CartItem>,
    val totalPrice: Double,
)
