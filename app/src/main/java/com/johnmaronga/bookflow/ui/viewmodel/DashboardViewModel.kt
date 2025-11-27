package com.johnmaronga.bookflow.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.johnmaronga.bookflow.data.model.Book
import com.johnmaronga.bookflow.data.model.ReadingProgress
import com.johnmaronga.bookflow.data.model.ReadingStatus
import com.johnmaronga.bookflow.data.model.Review
import com.johnmaronga.bookflow.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val currentlyReading: List<BookWithProgress> = emptyList(),
    val wantToRead: List<BookWithProgress> = emptyList(),
    val recentBooks: List<Book> = emptyList(),
    val recentReviews: List<Review> = emptyList(),
    val trendingBooks: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSyncing: Boolean = false,
    val readingStats: ReadingStats = ReadingStats()
)

data class BookWithProgress(
    val book: Book,
    val progress: ReadingProgress
)

data class ReadingStats(
    val booksRead: Int = 0,
    val pagesRead: Int = 0,
    val readingStreak: Int = 0
)

class DashboardViewModel(
    private val repository: BookRepository
) : ViewModel() {

    companion object {
        private const val TAG = "DashboardViewModel"
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<UiState<List<Book>>>(UiState.Idle)
    val searchResults: StateFlow<UiState<List<Book>>> = _searchResults.asStateFlow()

    // Debug state
    private val _debugInfo = MutableStateFlow<String?>(null)
    val debugInfo: StateFlow<String?> = _debugInfo.asStateFlow()

    init {
        Log.d(TAG, "🎯 DashboardViewModel initialized")
        loadDashboardData()
    }

    private fun loadDashboardData() {
        Log.d(TAG, "🔄 Loading dashboard data...")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Collect currently reading books with their details
                launch {
                    repository.getCurrentlyReading().collect { progressList ->
                        Log.d(TAG, "📚 Currently reading: ${progressList.size} books")
                        val booksWithProgress = progressList.mapNotNull { progress ->
                            val book = repository.getBookById(progress.bookId)
                            book?.let { BookWithProgress(it, progress) }
                        }
                        Log.d(TAG, "📚 Books with progress found: ${booksWithProgress.size}")
                        _uiState.value = _uiState.value.copy(currentlyReading = booksWithProgress)
                    }
                }

                // Collect recent books
                launch {
                    repository.getAllBooks().collect { books ->
                        Log.d(TAG, "📖 All books in database: ${books.size}")
                        _uiState.value = _uiState.value.copy(
                            recentBooks = books.take(10),
                            isLoading = false
                        )
                    }
                }

                // Collect recent reviews
                launch {
                    repository.getAllReviews().collect { reviews ->
                        Log.d(TAG, "⭐ Recent reviews: ${reviews.size}")
                        _uiState.value = _uiState.value.copy(recentReviews = reviews.take(5))
                    }
                }
                launch {
                    repository.getWantToRead().collect { progressList ->
                        val wantToReadBooks = progressList.mapNotNull { progress ->
                            val book = repository.getBookById(progress.bookId)
                            book?.let { BookWithProgress(it, progress) }
                        }
                        _uiState.value = _uiState.value.copy(wantToRead = wantToReadBooks)
                    }
                }

                // Collect all progress for stats calculation
                launch {
                    repository.getAllProgress().collect { allProgress ->
                        Log.d(TAG, "📊 Reading progress entries: ${allProgress.size}")
                        calculateStats(allProgress)
                    }
                }

                // Load trending books from API
                loadTrendingBooks()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading dashboard data: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun loadTrendingBooks() {
        Log.d(TAG, "🔥 Loading trending books...")
        viewModelScope.launch {
            repository.getTrendingBooks().fold(
                onSuccess = { books ->
                    Log.d(TAG, "🔥 Trending books loaded: ${books.size}")
                    _uiState.value = _uiState.value.copy(trendingBooks = books)
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Failed to load trending books: ${error.message}")
                    // Silently fail for trending books, not critical
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load trending books: ${error.message}"
                    )
                }
            )
        }
    }

    fun searchBooks(query: String) {
        Log.d(TAG, "🔍 Searching books for: '$query'")
        if (query.isBlank()) {
            _searchResults.value = UiState.Idle
            return
        }

        viewModelScope.launch {
            _searchResults.value = UiState.Loading

            repository.searchBooksRemote(query).fold(
                onSuccess = { books ->
                    Log.d(TAG, "🔍 Search found ${books.size} results for '$query'")
                    _searchResults.value = UiState.Success(books)
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Search failed: ${error.message}")
                    _searchResults.value = UiState.Error(
                        error.message ?: "Failed to search books"
                    )
                }
            )
        }
    }

    fun addBook(book: Book) {
        Log.d(TAG, "🎯 addBook() CALLED:")
        Log.d(TAG, "  - Book: '${book.title}' by ${book.author}")
        Log.d(TAG, "  - ID: ${book.id}")
        Log.d(TAG, "  - Page Count: ${book.pageCount}")
        Log.d(TAG, "  - Categories: ${book.categories}")

        viewModelScope.launch {
            try {
                Log.d(TAG, "🎯 Starting repository.insertBook()")
                repository.insertBook(book)
                Log.d(TAG, "🎯 repository.insertBook() completed")

                // Verify the book was actually added
                Log.d(TAG, "🎯 Verifying book was saved to database...")
                val retrievedBook = repository.getBookById(book.id)
                if (retrievedBook != null) {
                    Log.d(TAG, "✅ VERIFICATION PASSED: Book found in database after insertion!")
                    Log.d(TAG, "  - Retrieved title: '${retrievedBook.title}'")
                    Log.d(TAG, "  - Retrieved author: ${retrievedBook.author}")
                    _debugInfo.value = "✅ Book '${book.title}' added successfully!"
                } else {
                    Log.e(TAG, "❌ VERIFICATION FAILED: Book NOT found after insertion!")
                    _debugInfo.value = "❌ Book '${book.title}' was not saved to database!"
                }

                // Refresh the UI to show the new book
                Log.d(TAG, "🔄 Refreshing dashboard data to show new book...")
                loadDashboardData()

            } catch (e: Exception) {
                Log.e(TAG, "❌ ERROR adding book: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add book: ${e.message}"
                )
                _debugInfo.value = "❌ Error adding book: ${e.message}"
            }
        }
    }

    fun updateReadingProgress(progress: ReadingProgress) {
        Log.d(TAG, "📝 Updating reading progress:")
        Log.d(TAG, "  - Book ID: ${progress.bookId}")
        Log.d(TAG, "  - Progress: ${progress.currentPage}/${progress.totalPages}")
        Log.d(TAG, "  - Status: ${progress.status}")

        viewModelScope.launch {
            try {
                repository.insertOrUpdateProgress(progress)
                Log.d(TAG, "✅ Reading progress updated successfully")
                // Refresh to show updated progress
                loadDashboardData()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to update progress: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update progress: ${e.message}"
                )
            }
        }
    }

    fun addOrUpdateReview(review: Review) {
        Log.d(TAG, "⭐ Adding/updating review:")
        Log.d(TAG, "  - Book ID: ${review.bookId}")
        Log.d(TAG, "  - Rating: ${review.rating}/5")

        viewModelScope.launch {
            try {
                repository.insertOrUpdateReview(review)
                Log.d(TAG, "✅ Review saved successfully")
                // Refresh to show new review
                loadDashboardData()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to save review: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save review: ${e.message}"
                )
            }
        }
    }

    fun syncData() {
        Log.d(TAG, "🔄 Starting data sync...")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)

            repository.syncBooks().fold(
                onSuccess = {
                    Log.d(TAG, "✅ Sync completed successfully")
                    _uiState.value = _uiState.value.copy(isSyncing = false)
                    // Refresh data after sync
                    loadDashboardData()
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Sync failed: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        error = "Sync failed: ${error.message}"
                    )
                }
            )
        }
    }

    fun clearError() {
        Log.d(TAG, "🗑️ Clearing error")
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSearchResults() {
        Log.d(TAG, "🗑️ Clearing search results")
        _searchResults.value = UiState.Idle
    }

    // Debug methods
    fun debugDatabase() {
        Log.d(TAG, "🐛 DEBUG: Checking database state...")
        viewModelScope.launch {
            try {
                val debugResult = repository.debugGetAllBooks()
                Log.d(TAG, "🐛 Database debug completed")
                _debugInfo.value = debugResult
            } catch (e: Exception) {
                Log.e(TAG, "🐛 Debug failed: ${e.message}", e)
                _debugInfo.value = "❌ Debug error: ${e.message}"
            }
        }
    }

    fun debugBook(bookId: String) {
        Log.d(TAG, "🐛 DEBUG: Checking book: $bookId")
        viewModelScope.launch {
            try {
                val debugResult = repository.debugGetBook(bookId)
                _debugInfo.value = debugResult
            } catch (e: Exception) {
                _debugInfo.value = "❌ Book debug error: ${e.message}"
            }
        }
    }

    fun clearDebugInfo() {
        Log.d(TAG, "🗑️ Clearing debug info")
        _debugInfo.value = null
    }

    fun addBookToWantToRead(book: Book) {
        Log.d(TAG, "📚 Adding book to Want to Read: ${book.title}")

        viewModelScope.launch {
            try {
                // 1. Insert the book
                repository.insertBook(book)
                Log.d(TAG, "✅ Book inserted: ${book.title}")

                // 2. Create reading progress for "Want to Read"
                val progress = ReadingProgress(
                    id = "progress-${book.id}",
                    bookId = book.id,
                    currentPage = 0,
                    totalPages = book.pageCount ?: 0,
                    status = ReadingStatus.WANT_TO_READ, // Make sure you have this status
                    lastUpdated = System.currentTimeMillis()
                )

                repository.insertOrUpdateProgress(progress)
                Log.d(TAG, "✅ Reading progress created for Want to Read")

                // 3. Refresh the UI
                loadDashboardData()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error adding book to Want to Read: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add book: ${e.message}"
                )
            }
        }
    }

    private fun calculateStats(allProgress: List<ReadingProgress>) {
        val booksRead = allProgress.count { it.status == com.johnmaronga.bookflow.data.model.ReadingStatus.FINISHED }
        val pagesRead = allProgress
            .filter { it.status == com.johnmaronga.bookflow.data.model.ReadingStatus.FINISHED }
            .sumOf { it.totalPages }

        // Calculate reading streak (consecutive days with reading activity)
        val readingStreak = calculateReadingStreak(allProgress)

        Log.d(TAG, "📊 Stats calculated:")
        Log.d(TAG, "  - Books read: $booksRead")
        Log.d(TAG, "  - Pages read: $pagesRead")
        Log.d(TAG, "  - Reading streak: $readingStreak days")

        _uiState.value = _uiState.value.copy(
            readingStats = ReadingStats(
                booksRead = booksRead,
                pagesRead = pagesRead,
                readingStreak = readingStreak
            )
        )
    }

    private fun calculateReadingStreak(allProgress: List<ReadingProgress>): Int {
        if (allProgress.isEmpty()) return 0

        // Get all unique dates with reading activity (sorted descending)
        val readingDates = allProgress
            .map { it.lastUpdated }
            .distinct()
            .sortedDescending()

        if (readingDates.isEmpty()) return 0

        // Check if there's activity today or yesterday
        val today = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val mostRecent = readingDates.first()

        if (today - mostRecent > 2 * oneDayMs) {
            return 0 // Streak broken if no activity in last 2 days
        }

        // Count consecutive days
        var streak = 1
        for (i in 0 until readingDates.size - 1) {
            val daysDiff = (readingDates[i] - readingDates[i + 1]) / oneDayMs
            if (daysDiff <= 1) {
                streak++
            } else {
                break
            }
        }

        return streak
    }
}

