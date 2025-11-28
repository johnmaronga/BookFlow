package com.johnmaronga.bookflow.data.local.dao

import androidx.room.*
import com.johnmaronga.bookflow.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress ORDER BY lastUpdated DESC")
    fun getAllProgress(): Flow<List<ReadingProgressEntity>>

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId")
    suspend fun getProgressByBookId(bookId: String): ReadingProgressEntity?

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId")
    fun getProgressByBookIdFlow(bookId: String): Flow<ReadingProgressEntity?>

    @Query("SELECT * FROM reading_progress WHERE status = 'WANT_TO_READ'")
    fun getWantToRead(): Flow<List<ReadingProgressEntity>>

    @Query("SELECT * FROM reading_progress WHERE status = :status ORDER BY lastUpdated DESC")
    fun getProgressByStatus(status: String): Flow<List<ReadingProgressEntity>>

    @Query("SELECT * FROM reading_progress WHERE status = 'CURRENTLY_READING' ORDER BY lastUpdated DESC")
    fun getCurrentlyReading(): Flow<List<ReadingProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ReadingProgressEntity)

    @Update
    suspend fun updateProgress(progress: ReadingProgressEntity)

    @Delete
    suspend fun deleteProgress(progress: ReadingProgressEntity)

    @Query("DELETE FROM reading_progress WHERE bookId = :bookId")
    suspend fun deleteProgressByBookId(bookId: String)
}
