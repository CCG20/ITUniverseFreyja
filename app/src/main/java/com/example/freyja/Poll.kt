package com.example.freyja

data class Poll(
    val id: Long,
    val title: String,
    val body: String?,
    val deadline: String?
)
