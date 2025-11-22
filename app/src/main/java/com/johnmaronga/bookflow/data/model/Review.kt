package com.johnmaronga.bookflow.data.model

data class Review(
    val id: String,
    val bookId: String,
    val rating: Float,
    val reviewText: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
