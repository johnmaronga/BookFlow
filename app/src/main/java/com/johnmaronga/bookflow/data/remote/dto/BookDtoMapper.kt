package com.johnmaronga.bookflow.data.remote.dto

import com.johnmaronga.bookflow.data.model.Book

fun BookItemDto.toBook(): Book {
    val isbn = volumeInfo.industryIdentifiers
        ?.firstOrNull { it.type == "ISBN_13" || it.type == "ISBN_10" }
        ?.identifier

    return Book(
        id = id,
        title = volumeInfo.title,
        author = volumeInfo.authors?.joinToString(", ") ?: "Unknown Author",
        description = volumeInfo.description,
        coverImageUrl = volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"),
        isbn = isbn,
        publishedDate = volumeInfo.publishedDate,
        pageCount = volumeInfo.pageCount,
        categories = volumeInfo.categories ?: emptyList(),
        averageRating = volumeInfo.averageRating,
        ratingsCount = volumeInfo.ratingsCount
    )
}

fun GoogleBooksResponse.toBooks(): List<Book> {
    return items?.map { it.toBook() } ?: emptyList()
}
