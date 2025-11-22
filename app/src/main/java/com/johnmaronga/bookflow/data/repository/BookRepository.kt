package com.johnmaronga.bookflow.data.repository

import com.johnmaronga.bookflow.data.model.Book
import com.johnmaronga.bookflow.data.model.ReadingProgress
import com.johnmaronga.bookflow.data.model.Review
import kotlinx.coroutines.flow.Flow

interface BookRepository {

    // Book operations
    fun getAllBooks(): Flow<List<Book>>
    suspend fun getBookById(bookId: String): Book?
    fun getBookByIdFlow(bookId: String): Flow<Book?>
    suspend fun searchBooksLocal(query: String): Flow<List<Book>>
    suspend fun searchBooksRemote(query: String): Result<List<Book>>
    suspend fun getTrendingBooks(): Result<List<Book>>
    suspend fun getBooksByCategory(category: String): Result<List<Book>>
    suspend fun insertBook(book: Book)
    suspend fun deleteBook(bookId: String)

    // Reading Progress operations
    fun getAllProgress(): Flow<List<ReadingProgress>>
    suspend fun getProgressByBookId(bookId: String): ReadingProgress?
    fun getProgressByBookIdFlow(bookId: String): Flow<ReadingProgress?>
    fun getCurrentlyReading(): Flow<List<ReadingProgress>>
    suspend fun insertOrUpdateProgress(progress: ReadingProgress)
    suspend fun deleteProgress(bookId: String)

    // Review operations
    fun getAllReviews(): Flow<List<Review>>
    suspend fun getReviewByBookId(bookId: String): Review?
    fun getReviewByBookIdFlow(bookId: String): Flow<Review?>
    suspend fun insertOrUpdateReview(review: Review)
    suspend fun deleteReview(reviewId: String)

    // Sync operations
    suspend fun syncBooks(): Result<Unit>
}
