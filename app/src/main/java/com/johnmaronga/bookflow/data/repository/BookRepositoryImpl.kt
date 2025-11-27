package com.johnmaronga.bookflow.data.repository

import android.util.Log
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class BookRepositoryImpl(
    private val bookDao: BookDao,
    private val readingProgressDao: ReadingProgressDao,
    private val reviewDao: ReviewDao,
    private val apiService: BookApiService
) : BookRepository {

    companion object {
        private const val TAG = "BookRepository"
    }

    // Book operations
    override fun getAllBooks(): Flow<List<Book>> {
        Log.d(TAG, "📖 Getting all books flow...")
        return bookDao.getAllBooks().map { entities ->
            Log.d(TAG, "📖 Retrieved ${entities.size} books from database")
            entities.map { it.toBook() }
        }
    }

    override suspend fun getBookById(bookId: String): Book? {
        Log.d(TAG, "🔍 Getting book by ID: $bookId")
        return bookDao.getBookById(bookId)?.toBook().also {
            if (it == null) {
                Log.d(TAG, "🔍 Book not found for ID: $bookId")
            } else {
                Log.d(TAG, "🔍 Found book: '${it.title}' by ${it.author}")
            }
        }
    }

    override fun getBookByIdFlow(bookId: String): Flow<Book?> {
        Log.d(TAG, "🔍 Getting book flow by ID: $bookId")
        return bookDao.getBookByIdFlow(bookId).map { it?.toBook() }
    }

    override suspend fun searchBooksLocal(query: String): Flow<List<Book>> {
        Log.d(TAG, "🔍 Searching books locally for: '$query'")
        return bookDao.searchBooks(query).map { entities ->
            Log.d(TAG, "🔍 Local search found ${entities.size} results for '$query'")
            entities.map { it.toBook() }
        }
    }

    override suspend fun searchBooksRemote(query: String): Result<List<Book>> {
        Log.d(TAG, "🌐 Searching books remotely for: '$query'")
        return try {
            val response = apiService.searchBooks(query)
            if (response.isSuccessful && response.body() != null) {
                val books = response.body()!!.toBooks()
                Log.d(TAG, "🌐 Remote search found ${books.size} results for '$query'")

                // Cache books locally
                books.forEach { book ->
                    Log.d(TAG, "💾 Caching book: '${book.title}'")
                    bookDao.insertBook(book.toEntity())
                }
                Result.success(books)
            } else {
                Log.e(TAG, "❌ Remote search failed: ${response.message()}")
                Result.failure(Exception("Failed to fetch books: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Remote search error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getTrendingBooks(): Result<List<Book>> {
        Log.d(TAG, "🔥 Getting trending books...")
        return try {
            val response = apiService.getTrendingBooks()
            if (response.isSuccessful && response.body() != null) {
                val books = response.body()!!.toBooks()
                Log.d(TAG, "🔥 Found ${books.size} trending books")
                Result.success(books)
            } else {
                Log.e(TAG, "❌ Failed to fetch trending books: ${response.message()}")
                Result.failure(Exception("Failed to fetch trending books: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Trending books error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getBooksByCategory(category: String): Result<List<Book>> {
        Log.d(TAG, "📚 Getting books by category: '$category'")
        return try {
            val response = apiService.searchBooksByCategory("subject:$category")
            if (response.isSuccessful && response.body() != null) {
                val books = response.body()!!.toBooks()
                Log.d(TAG, "📚 Found ${books.size} books in category '$category'")
                Result.success(books)
            } else {
                Log.e(TAG, "❌ Failed to fetch books by category: ${response.message()}")
                Result.failure(Exception("Failed to fetch books by category: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Category books error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun insertBook(book: Book) {
        Log.d(TAG, "🔄 INSERTING BOOK:")
        Log.d(TAG, "  - Title: '${book.title}'")
        Log.d(TAG, "  - ID: ${book.id}")
        Log.d(TAG, "  - Author: ${book.author}")
        Log.d(TAG, "  - Cover URL: ${book.coverImageUrl}")
        Log.d(TAG, "  - Page Count: ${book.pageCount}")
        Log.d(TAG, "  - Categories: ${book.categories}")
        Log.d(TAG, "  - Description length: ${book.description?.length ?: 0}")

        try {
            // Convert to entity
            val entity = book.toEntity()
            Log.d(TAG, "  ✅ Converted to BookEntity:")
            Log.d(TAG, "     - Entity ID: ${entity.id}")
            Log.d(TAG, "     - Entity addedAt: ${entity.addedAt}")
            Log.d(TAG, "     - Entity categories: ${entity.categories}")

            // Insert into database
            bookDao.insertBook(entity)
            Log.d(TAG, "  ✅ Successfully inserted into database")

            // Verify the book was actually saved
            val retrievedBook = bookDao.getBookById(book.id)
            if (retrievedBook != null) {
                Log.d(TAG, "  ✅ VERIFICATION PASSED: Book found in database after insertion!")
                Log.d(TAG, "     - Retrieved title: '${retrievedBook.title}'")
                Log.d(TAG, "     - Retrieved author: ${retrievedBook.author}")
                Log.d(TAG, "     - Retrieved addedAt: ${retrievedBook.addedAt}")
            } else {
                Log.e(TAG, "  ❌ VERIFICATION FAILED: Book NOT found in database after insertion!")
            }

        } catch (e: Exception) {
            Log.e(TAG, "  ❌ ERROR inserting book: ${e.message}", e)
        }
    }

    override suspend fun deleteBook(bookId: String) {
        Log.d(TAG, "🗑️ Deleting book with ID: $bookId")
        bookDao.deleteBookById(bookId)
        Log.d(TAG, "✅ Book deleted (if it existed)")
    }

    // Reading Progress operations
    override fun getAllProgress(): Flow<List<ReadingProgress>> {
        Log.d(TAG, "📊 Getting all reading progress...")
        return readingProgressDao.getAllProgress().map { entities ->
            Log.d(TAG, "📊 Retrieved ${entities.size} reading progress entries")
            entities.map { it.toReadingProgress() }
        }
    }

    override suspend fun getProgressByBookId(bookId: String): ReadingProgress? {
        Log.d(TAG, "📖 Getting reading progress for book: $bookId")
        return readingProgressDao.getProgressByBookId(bookId)?.toReadingProgress().also {
            if (it == null) {
                Log.d(TAG, "📖 No reading progress found for book: $bookId")
            } else {
                Log.d(TAG, "📖 Found progress: ${it.currentPage}/${it.totalPages} pages")
            }
        }
    }

    override fun getProgressByBookIdFlow(bookId: String): Flow<ReadingProgress?> {
        Log.d(TAG, "📖 Getting reading progress flow for book: $bookId")
        return readingProgressDao.getProgressByBookIdFlow(bookId).map { it?.toReadingProgress() }
    }

    override fun getCurrentlyReading(): Flow<List<ReadingProgress>> {
        Log.d(TAG, "📚 Getting currently reading books...")
        return readingProgressDao.getCurrentlyReading().map { entities ->
            Log.d(TAG, "📚 Found ${entities.size} currently reading books")
            entities.map { it.toReadingProgress() }
        }
    }

    override fun getWantToRead(): Flow<List<ReadingProgress>> {
        return readingProgressDao.getWantToRead().map { entities ->
            entities.map { it.toReadingProgress() }
        }
    }

    override suspend fun insertOrUpdateProgress(progress: ReadingProgress) {
        Log.d(TAG, "📝 Inserting/updating reading progress:")
        Log.d(TAG, "  - Book ID: ${progress.bookId}")
        Log.d(TAG, "  - Progress: ${progress.currentPage}/${progress.totalPages}")
        Log.d(TAG, "  - Status: ${progress.status}")

        readingProgressDao.insertProgress(progress.toEntity())
        Log.d(TAG, "✅ Reading progress saved")
    }

    override suspend fun deleteProgress(bookId: String) {
        Log.d(TAG, "🗑️ Deleting reading progress for book: $bookId")
        readingProgressDao.deleteProgressByBookId(bookId)
        Log.d(TAG, "✅ Reading progress deleted")
    }

    // Review operations - FIXED WITH CORRECT FIELD NAMES
    override fun getAllReviews(): Flow<List<Review>> {
        Log.d(TAG, "⭐ Getting all reviews...")
        return reviewDao.getAllReviews().map { entities ->
            Log.d(TAG, "⭐ Retrieved ${entities.size} reviews")
            entities.map { it.toReview() }
        }
    }

    override suspend fun getReviewByBookId(bookId: String): Review? {
        Log.d(TAG, "⭐ Getting review for book: $bookId")
        return reviewDao.getReviewByBookId(bookId)?.toReview().also {
            if (it == null) {
                Log.d(TAG, "⭐ No review found for book: $bookId")
            } else {
                Log.d(TAG, "⭐ Found review with rating: ${it.rating}/5")
            }
        }
    }

    override fun getReviewByBookIdFlow(bookId: String): Flow<Review?> {
        Log.d(TAG, "⭐ Getting review flow for book: $bookId")
        return reviewDao.getReviewByBookIdFlow(bookId).map { it?.toReview() }
    }

    override suspend fun insertOrUpdateReview(review: Review) {
        Log.d(TAG, "⭐ Inserting/updating review:")
        Log.d(TAG, "  - Book ID: ${review.bookId}")
        Log.d(TAG, "  - Rating: ${review.rating}/5")
        Log.d(TAG, "  - Review text length: ${review.reviewText?.length ?: 0}") // ✅ FIXED: using reviewText
        Log.d(TAG, "  - Created at: ${review.createdAt}")
        Log.d(TAG, "  - Updated at: ${review.updatedAt}")

        reviewDao.insertReview(review.toEntity())
        Log.d(TAG, "✅ Review saved")
    }

    override suspend fun deleteReview(reviewId: String) {
        Log.d(TAG, "🗑️ Deleting review with ID: $reviewId")
        reviewDao.deleteReviewById(reviewId)
        Log.d(TAG, "✅ Review deleted")
    }

    // Sync operations
    override suspend fun syncBooks(): Result<Unit> {
        Log.d(TAG, "🔄 Starting book sync...")
        return try {
            // Fetch trending books and cache them
            val trendingResult = getTrendingBooks()
            if (trendingResult.isSuccess) {
                val books = trendingResult.getOrNull() ?: emptyList()
                Log.d(TAG, "🔄 Caching ${books.size} trending books")
                books.forEach { book ->
                    bookDao.insertBook(book.toEntity())
                }
                Log.d(TAG, "✅ Sync completed successfully")
            } else {
                Log.e(TAG, "❌ Sync failed: ${trendingResult.exceptionOrNull()?.message}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Sync error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Debug methods
    override suspend fun debugGetAllBooks(): String {
        Log.d(TAG, "🐛 DEBUG: Getting all books info...")
        return try {
            val booksFlow = bookDao.getAllBooks()
            val books = booksFlow.first()
            val entities = booksFlow.first()

            val result = """
                📊 DATABASE DEBUG INFO:
                
                Total Books: ${books.size}
                Total Entities: ${entities.size}
                
                Books in Database:
                ${if (books.isEmpty()) "  - No books found" else books.joinToString("\n") { book -> "  - '${book.title}' by ${book.author} (ID: ${book.id})" }}
                
                Sample Entity Details:
                ${if (entities.isEmpty()) "  - No entities found" else entities.take(3).joinToString("\n") { entity -> "  - '${entity.title}' - addedAt: ${entity.addedAt}, categories: ${entity.categories}" }}
            """.trimIndent()

            Log.d(TAG, "🐛 DEBUG RESULT:\n$result")
            result
        } catch (e: Exception) {
            val error = "❌ ERROR reading database: ${e.message}"
            Log.e(TAG, error, e)
            error
        }
    }

    override suspend fun debugGetBook(bookId: String): String {
        Log.d(TAG, "🐛 DEBUG: Getting book info for ID: $bookId")
        return try {
            val book = bookDao.getBookById(bookId)
            val entity = bookDao.getBookById(bookId)

            if (book != null) {
                val result = """
                🔍 BOOK FOUND:
                - Title: ${book.title}
                - Author: ${book.author} 
                - ID: ${book.id}
                - Page Count: ${book.pageCount}
                - Categories: ${book.categories}
                - Entity addedAt: ${entity?.addedAt}
                - In database: ✅ YES
                """
                Log.d(TAG, "🐛 DEBUG RESULT: Book found - ${book.title}")
                result
            } else {
                val result = "❌ BOOK NOT FOUND: No book with ID '$bookId' in database"
                Log.d(TAG, "🐛 DEBUG RESULT: $result")
                result
            }
        } catch (e: Exception) {
            val error = "❌ ERROR: ${e.message}"
            Log.e(TAG, error, e)
            error
        }
    }
}