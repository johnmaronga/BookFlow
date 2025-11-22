package com.johnmaronga.bookflow.data.model

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isbn: String? = null,
    val publishedDate: String? = null,
    val pageCount: Int? = null,
    val categories: List<String> = emptyList(),
    val averageRating: Float? = null,
    val ratingsCount: Int? = null
)
