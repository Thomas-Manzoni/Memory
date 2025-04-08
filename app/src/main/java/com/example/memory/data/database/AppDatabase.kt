package com.example.memory.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.memory.data.entity.FlashcardInsight
import com.example.memory.data.dao.FlashcardInsightDao

@Database(entities = [FlashcardInsight::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashcardInsightDao(): FlashcardInsightDao
}