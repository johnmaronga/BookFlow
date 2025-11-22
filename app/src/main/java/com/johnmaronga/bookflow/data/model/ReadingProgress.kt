package com.johnmaronga.bookflow.data.model

data class ReadingProgress(
    val id: String,
    val bookId: String,
    val currentPage: Int,
    val totalPages: Int,
    val status: ReadingStatus,
    val startDate: Long? = null,
    val finishDate: Long? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class ReadingStatus {
    WANT_TO_READ,
    CURRENTLY_READING,
    FINISHED,
    DNF // Did Not Finish
}
