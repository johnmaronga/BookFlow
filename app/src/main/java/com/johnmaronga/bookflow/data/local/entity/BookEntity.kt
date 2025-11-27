package com.johnmaronga.bookflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.johnmaronga.bookflow.data.local.converter.StringListConverter
import com.johnmaronga.bookflow.data.model.Book

@Entity(tableName = "books")
@TypeConverters(StringListConverter::class)
data class BookEntity(
    @PrimaryKey
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
    val ratingsCount: Int? = null,
    val addedAt: Long = System.currentTimeMillis()
)

// Extension functions for mapping
fun BookEntity.toBook() = Book(
    id = id,
    title = title,
    author = author,
    description = description,
    coverImageUrl = coverImageUrl,
    isbn = isbn,
    publishedDate = publishedDate,
    pageCount = pageCount,
    categories = categories,
    averageRating = averageRating,
    ratingsCount = ratingsCount
)

/* fun Book.toEntity() = BookEntity(
    id = id,
    title = title,
    author = author,
    description = description,
    coverImageUrl = coverImageUrl,
    isbn = isbn,
    publishedDate = publishedDate,
    pageCount = pageCount,
    categories = categories,
    averageRating = averageRating,
    ratingsCount = ratingsCount
)

 */
fun Book.toEntity(): BookEntity {

    return BookEntity(
        id = id,
        title = title,
        author = author,
        description = description,
        coverImageUrl = coverImageUrl,  // Note: different field name!
        isbn = isbn,
        publishedDate = publishedDate,
        pageCount = pageCount, // Map totalPages to pageCount
        categories = categories,
        averageRating = averageRating,
        ratingsCount = ratingsCount,
        addedAt = System.currentTimeMillis()
    )
}
