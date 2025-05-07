package com.example.memory.data.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile private var instances: MutableMap<String, AppDatabase> = mutableMapOf()
    @Volatile private var progressInstance: ProgressDatabase? = null

    fun getDatabase(context: Context, dbName: String): AppDatabase {
        return instances[dbName] ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                dbName // this will be something like "course_math.db"
            )
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .addMigrations(MIGRATION_5_6)
                .addMigrations(MIGRATION_6_7)
                .addMigrations(MIGRATION_7_8)
                .addMigrations(MIGRATION_8_9)
                .build()
            instances[dbName] = instance
            instance
        }
    }

    fun getProgressDatabase(context: Context): ProgressDatabase {
        return progressInstance ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                ProgressDatabase::class.java,
                "language_progress.db"
            )
                .addMigrations(MIGRATION_PROGRESS_1_2)
                .build()
            progressInstance = instance
            instance
        }
    }
}

