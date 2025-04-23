package com.example.memory.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.memory.data.dao.CategoryDao
import com.example.memory.data.dao.FlashcardCategoryDao
import com.example.memory.data.entity.FlashcardInsight
import com.example.memory.data.dao.FlashcardInsightDao
import com.example.memory.data.dao.ProgressInsightDao
import com.example.memory.data.entity.Category
import com.example.memory.data.entity.FlashcardCategoryCrossRef
import com.example.memory.data.entity.LanguageProgress

@Database(entities = [FlashcardInsight::class, Category::class, FlashcardCategoryCrossRef::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashcardInsightDao(): FlashcardInsightDao
    abstract fun categoryDao(): CategoryDao
    abstract fun flashcardCategoryDao(): FlashcardCategoryDao
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

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
      CREATE TABLE IF NOT EXISTS `categories` (
        `name` TEXT NOT NULL PRIMARY KEY
      )
    """.trimIndent())
        db.execSQL("""
      CREATE TABLE IF NOT EXISTS `flashcard_category_xref` (
        `flashcardId`   TEXT NOT NULL,
        `categoryName`  TEXT NOT NULL,
        PRIMARY KEY (`flashcardId`,`categoryName`),
        FOREIGN KEY(`flashcardId`)
          REFERENCES `flashcard_insights`(`flashcardId`)
            ON DELETE CASCADE,
        FOREIGN KEY(`categoryName`)
          REFERENCES `categories`(`name`)
            ON DELETE CASCADE
      )
    """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_xref_categoryName` ON `flashcard_category_xref`(`categoryName`)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // insert all your standard categories exactly once
        db.execSQL("""
      INSERT OR IGNORE INTO categories(name) VALUES
        ('animals'),
        ('food'),
        ('hobbies'),
        ('house & home'),
        ('family & relationships'),
        ('profession & workplace'),
        ('verb'),
        ('adjective'),
        ('adverb')
    """.trimIndent())
    }
}

@Database(entities = [LanguageProgress::class], version = 2, exportSchema = false)
abstract class ProgressDatabase : RoomDatabase() {
    abstract fun progressInsightDao(): ProgressInsightDao
}

val MIGRATION_PROGRESS_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) add the new column with a harmless default
        db.execSQL("""
      ALTER TABLE progress_insights
        ADD COLUMN lastCheckedEpochDay INTEGER NOT NULL DEFAULT 0
    """.trimIndent())

        // 2) back‑fill it to "today" for all existing rows
        //    CAST(julianday('now') - 2440587.5 AS INTEGER) gives days since 1970‑01‑01
        db.execSQL("""
      UPDATE progress_insights
         SET lastCheckedEpochDay = CAST(julianday('now') - 2440587.5 AS INTEGER)
    """.trimIndent())
    }
}

