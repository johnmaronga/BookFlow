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
    val currentlyReading: List<ReadingProgress> = emptyList(),
    val recentBooks: List<Book> = emptyList(),
    val recentReviews: List<Review> = emptyList(),
    val trendingBooks: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSyncing: Boolean = false
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
                // Collect currently reading books
                launch {
                    repository.getCurrentlyReading().collect { progress ->
                        _uiState.value = _uiState.value.copy(currentlyReading = progress)
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
}
