package com.example.memory.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.memory.data.entity.FlashcardInsight
import com.example.memory.data.dao.FlashcardInsightDao

@Database(entities = [FlashcardInsight::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashcardInsightDao(): FlashcardInsightDao
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE flashcard_insights ADD COLUMN sectionIndex INTEGER NOT NULL DEFAULT -1")
        db.execSQL("ALTER TABLE flashcard_insights ADD COLUMN unitIndex INTEGER NOT NULL DEFAULT -1")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE flashcard_insights ADD COLUMN lastSwipe INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE flashcard_insights ADD COLUMN timesWrong INTEGER NOT NULL DEFAULT 0")
    }
}
