package com.example.memory.data.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "flashcard_database"
            )
                .fallbackToDestructiveMigration() // This line allows for destructive migrations
                .build()
            INSTANCE = instance
            instance
        }
    }
}
