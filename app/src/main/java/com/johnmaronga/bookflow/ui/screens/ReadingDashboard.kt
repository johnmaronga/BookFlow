package com.johnmaronga.bookflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.johnmaronga.bookflow.ui.theme.BookFlowTheme

@Composable
fun DashboardScreen(
    onAddBookClick: () -> Unit = {},
    onSeeAllClick: (String) -> Unit = {},
    onBookClick: (String) -> Unit = {},
    onWebSearchClick: () -> Unit = {},
    currentlyReading: List<Book> = emptyList(),
    recommendations: List<Book> = emptyList(),
    recentReviews: List<Review> = emptyList(),
    readingStats: ReadingStats = ReadingStats()
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBookClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Book")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header with Stats
            item {
                DashboardHeader(
                    stats = readingStats,
                    onSearchClick = { onSeeAllClick("search") }
                )
            }

            // Quick Actions
            item {
                QuickActionsRow(
                    onAddBookClick = onAddBookClick,
                    //onSearchClick = { onSeeAllClick("search") },
                    onWebSearchClick = onWebSearchClick
                )
            }

            // Currently Reading Section
            item {
                SectionHeader(
                    title = "Currently Reading",
                    onSeeAllClick = { onSeeAllClick("currently_reading") }
                )
            }

            if (currentlyReading.isEmpty()) {
                item {
                    EmptyColumnCard(
                        message = "No books currently being read",
                        actionText = "Add Book",
                        onActionClick = onAddBookClick
                    )
                }
            } else {
                items(currentlyReading) { book ->
                    BookProgressCard(
                        book = book,
                        onClick = { onBookClick(book.id) }
                    )
                }
            }

            // Want to Read Section
            item {
                SectionHeader(
                    title = "Want to Read",
                    onSeeAllClick = { onSeeAllClick("want_to_read") }
                )
            }
            item {
                EmptyColumnCard(
                    message = "No books in want to read list",
                    actionText = "Browse Books",
                    onActionClick = { onSeeAllClick("search") }
                )
            }

            // Personalized Recommendations Section
            item {
                SectionHeader(
                    title = "Personalized Recommendations",
                    onSeeAllClick = { onSeeAllClick("recommendations") }
                )
            }

            if (recommendations.isEmpty()) {
                item {
                    EmptyColumnCard(
                        message = "Rate more books to get personalized recommendations"
                    )
                }
            } else {
                items(recommendations.chunked(2)) { rowBooks ->
                    BookRow(
                        books = rowBooks,
                        onBookClick = { bookId -> onBookClick(bookId) }
                    )
                }
            }

            // Recent Reviews Section
            item {
                SectionHeader(
                    title = "Recent Reviews",
                    onSeeAllClick = { onSeeAllClick("reviews") }
                )
            }

            if (recentReviews.isEmpty()) {
                item {
                    EmptyColumnCard(message = "No reviews yet")
                }
            } else {
                items(recentReviews) { review ->
                    ReviewCard(
                        review = review,
                        onBookClick = { onBookClick(review.bookId) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun DashboardHeader(
    stats: ReadingStats,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Reading Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, "Search Books")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reading Stats Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Books Read",
                value = stats.booksRead.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Pages Read",
                value = stats.pagesRead.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Streak",
                value = "${stats.readingStreak} days",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickActionsRow(
    onAddBookClick: () -> Unit,
    //onSearchClick: () -> Unit,
    onWebSearchClick: () -> Unit

) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            text = "Add Book",
            icon = Icons.Default.Add,
            onClick = onAddBookClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            text = "Web Search",
            icon = Icons.Default.Search,
            onClick = onWebSearchClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BookProgressCard(
    book: Book,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Book Cover - Using placeholder instead of AsyncImage
            BookCoverPlaceholder(
                modifier = Modifier
                    .size(60.dp)
                    .clip(MaterialTheme.shapes.medium)
            )

            Spacer(modifier = Modifier.size(16.dp))

            // Book Info and Progress
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.size(8.dp))

                // Progress Bar
                val progress = if (book.totalPages > 0) {
                    book.currentPage.toFloat() / book.totalPages.toFloat()
                } else 0f

                Column {
                    Text(
                        text = "${book.currentPage}/${book.totalPages} pages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun BookCoverPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clip(MaterialTheme.shapes.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Using a text emoji as placeholder - you can replace with an actual image resource
        Text(
            text = "📚",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun BookRow(
    books: List<Book>,
    onBookClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        books.forEach { book ->
            BookCoverCard(
                book = book,
                onClick = { onBookClick(book.id) },
                modifier = Modifier.weight(1f)
            )
        }
        // Fill empty space if odd number of books
        if (books.size % 2 != 0) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun BookCoverCard(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column {
            // Book cover placeholder
            BookCoverPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ReviewCard(
    review: Review,
    onBookClick: () -> Unit
) {
    Card(
        onClick = onBookClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Star rating
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (index < review.rating.toInt())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = String.format("%.1f", review.rating),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = review.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyColumnCard(
    message: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            actionText?.let { text ->
                Spacer(modifier = Modifier.height(16.dp))
                ElevatedCard(
                    onClick = onActionClick ?: {},
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// Keep your existing SectionHeader composable unchanged
@Composable
fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "See all",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

// Data classes for the new functionality
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String? = null,
    val isbn: String? = null,
    val totalPages: Int = 0,
    val currentPage: Int = 0,
    val shelf: BookShelf = BookShelf.WANT_TO_READ,
    val rating: Float = 0f,
    val startDate: String? = null,
    val endDate: String? = null
)

data class Review(
    val id: String,
    val bookId: String,
    val rating: Float,
    val content: String,
    val date: String,
    val spoiler: Boolean = false
)

data class ReadingStats(
    val booksRead: Int = 0,
    val pagesRead: Int = 0,
    val readingStreak: Int = 0,
    val favoriteGenre: String = ""
)

enum class BookShelf {
    CURRENTLY_READING, WANT_TO_READ, READ, DNF
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    BookFlowTheme {
        DashboardScreen(
            onWebSearchClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WebSearchDialogPreview() {
    BookFlowTheme {
        WebSearchDialog(
            onDismiss = {},
            onSearch = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenWithDataPreview() {
    val sampleBooks = listOf(
        Book(
            id = "1",
            title = "The Midnight Library",
            author = "Matt Haig",
            totalPages = 304,
            currentPage = 150
        ),
        Book(
            id = "2",
            title = "Project Hail Mary",
            author = "Andy Weir",
            totalPages = 476,
            currentPage = 89
        )
    )

    val sampleReviews = listOf(
        Review(
            id = "1",
            bookId = "3",
            rating = 4.5f,
            content = "Absolutely loved this book! The character development was incredible and the plot kept me engaged until the very end.",
            date = "2024-01-15"
        )
    )

    val sampleStats = ReadingStats(
        booksRead = 12,
        pagesRead = 3847,
        readingStreak = 15,
        favoriteGenre = "Science Fiction"
    )

    BookFlowTheme {
        DashboardScreen(
            currentlyReading = sampleBooks,
            recentReviews = sampleReviews,
            readingStats = sampleStats
        )
    }
}