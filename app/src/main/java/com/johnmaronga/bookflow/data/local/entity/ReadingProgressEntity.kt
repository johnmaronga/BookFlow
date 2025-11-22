package com.johnmaronga.bookflow.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.johnmaronga.bookflow.data.model.ReadingProgress
import com.johnmaronga.bookflow.data.model.ReadingStatus

@Entity(
    tableName = "reading_progress",
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
data class ReadingProgressEntity(
    @PrimaryKey
    val id: String,
    val bookId: String,
    val currentPage: Int,
    val totalPages: Int,
    val status: String,
    val startDate: Long? = null,
    val finishDate: Long? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

// Extension functions for mapping
fun ReadingProgressEntity.toReadingProgress() = ReadingProgress(
    id = id,
    bookId = bookId,
    currentPage = currentPage,
    totalPages = totalPages,
    status = ReadingStatus.valueOf(status),
    startDate = startDate,
    finishDate = finishDate,
    lastUpdated = lastUpdated
)

fun ReadingProgress.toEntity() = ReadingProgressEntity(
    id = id,
    bookId = bookId,
    currentPage = currentPage,
    totalPages = totalPages,
    status = status.name,
    startDate = startDate,
    finishDate = finishDate,
    lastUpdated = lastUpdated
)
