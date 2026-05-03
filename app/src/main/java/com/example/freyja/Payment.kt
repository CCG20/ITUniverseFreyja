package com.example.freyja

data class Payment(
    val id: Long,
    val residentId: Long,
    val date: String,
    val amount: Double,
    val note: String?
)
