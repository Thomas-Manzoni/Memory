package com.example.memory.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import com.example.memory.data.entity.LearnStatusConverter

@Database(entities =
    [FlashcardInsight::class,
    Category::class,
    FlashcardCategoryCrossRef::class],
    version = 9,
    exportSchema = false
)
@TypeConverters(LearnStatusConverter::class)
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

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE flashcard_insights ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create new table with updated schema (replacing lastSwipe with learnStatus)
        db.execSQL("""
            CREATE TABLE flashcard_insights_new (
                flashcardId TEXT NOT NULL PRIMARY KEY,
                timesReviewed INTEGER NOT NULL,
                timesCorrect INTEGER NOT NULL,
                timesWrong INTEGER NOT NULL,
                lastReviewed INTEGER NOT NULL,
                description TEXT NOT NULL,
                sectionIndex INTEGER NOT NULL,
                unitIndex INTEGER NOT NULL,
                learnStatus INTEGER NOT NULL,
                isFavorite INTEGER NOT NULL
            )
        """)

        // Copy data from old table to new table, mapping lastSwipe to learnStatus
        db.execSQL("""
            INSERT INTO flashcard_insights_new
            SELECT flashcardId, timesReviewed, timesCorrect, timesWrong, lastReviewed,
                   description, sectionIndex, unitIndex, 
                   0, isFavorite
            FROM flashcard_insights
        """)

        // Drop old table and rename new one
        db.execSQL("DROP TABLE flashcard_insights")
        db.execSQL("ALTER TABLE flashcard_insights_new RENAME TO flashcard_insights")
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

