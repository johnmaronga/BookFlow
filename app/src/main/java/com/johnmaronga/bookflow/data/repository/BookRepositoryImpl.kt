package com.johnmaronga.bookflow.data.repository

import com.johnmaronga.bookflow.data.local.dao.BookDao
import com.johnmaronga.bookflow.data.local.dao.ReadingProgressDao
import com.johnmaronga.bookflow.data.local.dao.ReviewDao
import com.johnmaronga.bookflow.data.local.entity.toBook
import com.johnmaronga.bookflow.data.local.entity.toEntity
import com.johnmaronga.bookflow.data.local.entity.toReadingProgress
import com.johnmaronga.bookflow.data.local.entity.toReview
import com.johnmaronga.bookflow.data.model.Book
import com.johnmaronga.bookflow.data.model.ReadingProgress
import com.johnmaronga.bookflow.data.model.Review
import com.johnmaronga.bookflow.data.remote.api.BookApiService
import com.johnmaronga.bookflow.data.remote.dto.toBooks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookRepositoryImpl(
    private val bookDao: BookDao,
    private val readingProgressDao: ReadingProgressDao,
    private val reviewDao: ReviewDao,
    private val apiService: BookApiService
) : BookRepository {

    // Book operations
    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map { entities ->
            entities.map { it.toBook() }
        }
    }

    override suspend fun getBookById(bookId: String): Book? {
        return bookDao.getBookById(bookId)?.toBook()
    }

    override fun getBookByIdFlow(bookId: String): Flow<Book?> {
        return bookDao.getBookByIdFlow(bookId).map { it?.toBook() }
    }

    override suspend fun searchBooksLocal(query: String): Flow<List<Book>> {
        return bookDao.searchBooks(query).map { entities ->
            entities.map { it.toBook() }
        }
    }

    override suspend fun searchBooksRemote(query: String): Result<List<Book>> {
        return try {
            val response = apiService.searchBooks(query)
            if (response.isSuccessful && response.body() != null) {
                val books = response.body()!!.toBooks()
                // Cache books locally
                books.forEach { book ->
                    bookDao.insertBook(book.toEntity())
                }
                Result.success(books)
            } else {
                Result.failure(Exception("Failed to fetch books: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTrendingBooks(): Result<List<Book>> {
        return try {
            val response = apiService.getTrendingBooks()
            if (response.isSuccessful && response.body() != null) {
                val books = response.body()!!.toBooks()
                Result.success(books)
            } else {
                Result.failure(Exception("Failed to fetch trending books: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBooksByCategory(category: String): Result<List<Book>> {
        return try {
            val response = apiService.searchBooksByCategory("subject:$category")
            if (response.isSuccessful && response.body() != null) {
                val books = response.body()!!.toBooks()
                Result.success(books)
            } else {
                Result.failure(Exception("Failed to fetch books by category: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun insertBook(book: Book) {
        bookDao.insertBook(book.toEntity())
    }

    override suspend fun deleteBook(bookId: String) {
        bookDao.deleteBookById(bookId)
    }

    // Reading Progress operations
    override fun getAllProgress(): Flow<List<ReadingProgress>> {
        return readingProgressDao.getAllProgress().map { entities ->
            entities.map { it.toReadingProgress() }
        }
    }

    override suspend fun getProgressByBookId(bookId: String): ReadingProgress? {
        return readingProgressDao.getProgressByBookId(bookId)?.toReadingProgress()
    }

    override fun getProgressByBookIdFlow(bookId: String): Flow<ReadingProgress?> {
        return readingProgressDao.getProgressByBookIdFlow(bookId).map { it?.toReadingProgress() }
    }

    override fun getCurrentlyReading(): Flow<List<ReadingProgress>> {
        return readingProgressDao.getCurrentlyReading().map { entities ->
            entities.map { it.toReadingProgress() }
        }
    }

    override suspend fun insertOrUpdateProgress(progress: ReadingProgress) {
        readingProgressDao.insertProgress(progress.toEntity())
    }

    override suspend fun deleteProgress(bookId: String) {
        readingProgressDao.deleteProgressByBookId(bookId)
    }

    // Review operations
    override fun getAllReviews(): Flow<List<Review>> {
        return reviewDao.getAllReviews().map { entities ->
            entities.map { it.toReview() }
        }
    }

    override suspend fun getReviewByBookId(bookId: String): Review? {
        return reviewDao.getReviewByBookId(bookId)?.toReview()
    }

    override fun getReviewByBookIdFlow(bookId: String): Flow<Review?> {
        return reviewDao.getReviewByBookIdFlow(bookId).map { it?.toReview() }
    }

    override suspend fun insertOrUpdateReview(review: Review) {
        reviewDao.insertReview(review.toEntity())
    }

    override suspend fun deleteReview(reviewId: String) {
        reviewDao.deleteReviewById(reviewId)
    }

    // Sync operations
    override suspend fun syncBooks(): Result<Unit> {
        return try {
            // Fetch trending books and cache them
            val trendingResult = getTrendingBooks()
            if (trendingResult.isSuccess) {
                trendingResult.getOrNull()?.forEach { book ->
                    bookDao.insertBook(book.toEntity())
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
