package com.johnmaronga.bookflow.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.johnmaronga.bookflow.data.model.Book
import com.johnmaronga.bookflow.data.model.ReadingProgress
import com.johnmaronga.bookflow.data.model.ReadingStatus
import com.johnmaronga.bookflow.data.model.Review
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    book: Book,
    progress: ReadingProgress? = null,
    review: Review? = null,
    onBackClick: () -> Unit,
    onUpdateProgress: (ReadingProgress) -> Unit,
    onSaveReview: (Review) -> Unit
) {
    var currentPage by remember { mutableIntStateOf(progress?.currentPage ?: 0) }
    var rating by remember { mutableFloatStateOf(review?.rating ?: 0f) }
    var reviewText by remember { mutableStateOf(review?.reviewText ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Book cover and info
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                BookCoverPlaceholder(
                    modifier = Modifier.size(120.dp, 180.dp)
                )

                Spacer(modifier = Modifier.size(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    book.publishedDate?.let {
                        Text(
                            text = "Published: $it",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    book.pageCount?.let {
                        Text(
                            text = "$it pages",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            book.description?.let { desc ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Reading Progress
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Reading Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Current Page: $currentPage / ${book.pageCount ?: 0}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = currentPage.toFloat(),
                        onValueChange = { currentPage = it.toInt() },
                        valueRange = 0f..(book.pageCount?.toFloat() ?: 100f),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val newProgress = ReadingProgress(
                                id = progress?.id ?: UUID.randomUUID().toString(),
                                bookId = book.id,
                                currentPage = currentPage,
                                totalPages = book.pageCount ?: 0,
                                status = when {
                                    currentPage == 0 -> ReadingStatus.WANT_TO_READ
                                    currentPage >= (book.pageCount ?: 0) -> ReadingStatus.FINISHED
                                    else -> ReadingStatus.CURRENTLY_READING
                                },
                                startDate = progress?.startDate ?: System.currentTimeMillis(),
                                finishDate = if (currentPage >= (book.pageCount ?: 0)) 
                                    System.currentTimeMillis() else null
                            )
                            onUpdateProgress(newProgress)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Progress")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Review
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Your Review",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Star rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rating: ", style = MaterialTheme.typography.bodyMedium)
                        repeat(5) { index ->
                            IconButton(
                                onClick = { rating = (index + 1).toFloat() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (index < rating.toInt())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Text(
                            text = rating.toInt().toString(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Write your review") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 6
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (rating > 0) {
                                val newReview = Review(
                                    id = review?.id ?: UUID.randomUUID().toString(),
                                    bookId = book.id,
                                    rating = rating,
                                    reviewText = reviewText.ifBlank { null },
                                    createdAt = review?.createdAt ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSaveReview(newReview)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = rating > 0
                    ) {
                        Text("Save Review")
                    }
                }
            }
        }
    }
}
