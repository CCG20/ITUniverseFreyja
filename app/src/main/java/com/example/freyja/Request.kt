package com.example.freyja

data class Request(
    val id: Long,
    val residentId: Long,
    val topic: String,
    val body: String,
    val date: String,
    val status: String
)
