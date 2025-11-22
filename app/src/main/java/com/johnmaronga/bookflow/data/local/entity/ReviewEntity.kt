package com.johnmaronga.bookflow.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.johnmaronga.bookflow.data.model.Review

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class ReviewEntity(
    @PrimaryKey
    val id: String,
    val bookId: String,
    val rating: Float,
    val reviewText: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Extension functions for mapping
fun ReviewEntity.toReview() = Review(
    id = id,
    bookId = bookId,
    rating = rating,
    reviewText = reviewText,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Review.toEntity() = ReviewEntity(
    id = id,
    bookId = bookId,
    rating = rating,
    reviewText = reviewText,
    createdAt = createdAt,
    updatedAt = updatedAt
)
