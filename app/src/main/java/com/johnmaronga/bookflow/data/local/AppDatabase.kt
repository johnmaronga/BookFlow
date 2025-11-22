package com.johnmaronga.bookflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.johnmaronga.bookflow.data.local.converter.StringListConverter
import com.johnmaronga.bookflow.data.local.dao.BookDao
import com.johnmaronga.bookflow.data.local.dao.ReadingProgressDao
import com.johnmaronga.bookflow.data.local.dao.ReviewDao
import com.johnmaronga.bookflow.data.local.dao.UserDao
import com.johnmaronga.bookflow.data.local.entity.BookEntity
import com.johnmaronga.bookflow.data.local.entity.ReadingProgressEntity
import com.johnmaronga.bookflow.data.local.entity.ReviewEntity
import com.johnmaronga.bookflow.data.local.entity.UserEntity

@Database(
    entities = [
        BookEntity::class,
        ReadingProgressEntity::class,
        ReviewEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun reviewDao(): ReviewDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bookflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
