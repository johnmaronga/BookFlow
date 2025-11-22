package com.johnmaronga.bookflow.data.local.dao

import androidx.room.*
import com.johnmaronga.bookflow.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE bookId = :bookId")
    suspend fun getReviewByBookId(bookId: String): ReviewEntity?

    @Query("SELECT * FROM reviews WHERE bookId = :bookId")
    fun getReviewByBookIdFlow(bookId: String): Flow<ReviewEntity?>

    @Query("SELECT * FROM reviews WHERE id = :reviewId")
    suspend fun getReviewById(reviewId: String): ReviewEntity?

    @Query("SELECT * FROM reviews WHERE rating >= :minRating ORDER BY createdAt DESC")
    fun getReviewsByMinRating(minRating: Float): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Update
    suspend fun updateReview(review: ReviewEntity)

    @Delete
    suspend fun deleteReview(review: ReviewEntity)

    @Query("DELETE FROM reviews WHERE bookId = :bookId")
    suspend fun deleteReviewsByBookId(bookId: String)

    @Query("DELETE FROM reviews WHERE id = :reviewId")
    suspend fun deleteReviewById(reviewId: String)
}
