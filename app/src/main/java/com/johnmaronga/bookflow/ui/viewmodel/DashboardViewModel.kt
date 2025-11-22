package com.johnmaronga.bookflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.johnmaronga.bookflow.data.model.Book
import com.johnmaronga.bookflow.data.model.ReadingProgress
import com.johnmaronga.bookflow.data.model.Review
import com.johnmaronga.bookflow.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val currentlyReading: List<BookWithProgress> = emptyList(),
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

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<UiState<List<Book>>>(UiState.Idle)
    val searchResults: StateFlow<UiState<List<Book>>> = _searchResults.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Collect currently reading books with their details
                launch {
                    repository.getCurrentlyReading().collect { progressList ->
                        val booksWithProgress = progressList.mapNotNull { progress ->
                            val book = repository.getBookById(progress.bookId)
                            book?.let { BookWithProgress(it, progress) }
                        }
                        _uiState.value = _uiState.value.copy(currentlyReading = booksWithProgress)
                    }
                }

                // Collect recent books
                launch {
                    repository.getAllBooks().collect { books ->
                        _uiState.value = _uiState.value.copy(
                            recentBooks = books.take(10),
                            isLoading = false
                        )
                    }
                }

                // Collect recent reviews
                launch {
                    repository.getAllReviews().collect { reviews ->
                        _uiState.value = _uiState.value.copy(recentReviews = reviews.take(5))
                    }
                }

                // Collect all progress for stats calculation
                launch {
                    repository.getAllProgress().collect { allProgress ->
                        calculateStats(allProgress)
                    }
                }

                // Load trending books from API
                loadTrendingBooks()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun loadTrendingBooks() {
        viewModelScope.launch {
            repository.getTrendingBooks().fold(
                onSuccess = { books ->
                    _uiState.value = _uiState.value.copy(trendingBooks = books)
                },
                onFailure = { error ->
                    // Silently fail for trending books, not critical
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load trending books: ${error.message}"
                    )
                }
            )
        }
    }

    fun searchBooks(query: String) {
        if (query.isBlank()) {
            _searchResults.value = UiState.Idle
            return
        }

        viewModelScope.launch {
            _searchResults.value = UiState.Loading

            repository.searchBooksRemote(query).fold(
                onSuccess = { books ->
                    _searchResults.value = UiState.Success(books)
                },
                onFailure = { error ->
                    _searchResults.value = UiState.Error(
                        error.message ?: "Failed to search books"
                    )
                }
            )
        }
    }

    fun addBook(book: Book) {
        viewModelScope.launch {
            try {
                repository.insertBook(book)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add book: ${e.message}"
                )
            }
        }
    }

    fun updateReadingProgress(progress: ReadingProgress) {
        viewModelScope.launch {
            try {
                repository.insertOrUpdateProgress(progress)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update progress: ${e.message}"
                )
            }
        }
    }

    fun addOrUpdateReview(review: Review) {
        viewModelScope.launch {
            try {
                repository.insertOrUpdateReview(review)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save review: ${e.message}"
                )
            }
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)

            repository.syncBooks().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSyncing = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        error = "Sync failed: ${error.message}"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSearchResults() {
        _searchResults.value = UiState.Idle
    }

    private fun calculateStats(allProgress: List<ReadingProgress>) {
        val booksRead = allProgress.count { it.status == com.johnmaronga.bookflow.data.model.ReadingStatus.FINISHED }
        val pagesRead = allProgress
            .filter { it.status == com.johnmaronga.bookflow.data.model.ReadingStatus.FINISHED }
            .sumOf { it.totalPages }

        // Calculate reading streak (consecutive days with reading activity)
        val readingStreak = calculateReadingStreak(allProgress)

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
