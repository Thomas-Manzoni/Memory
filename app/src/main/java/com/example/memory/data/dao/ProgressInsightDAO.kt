package com.example.memory.data.dao

import androidx.room.*
import com.example.memory.data.entity.LanguageProgress

@Dao
interface ProgressInsightDao {
    @Query("SELECT * FROM progress_insights WHERE languageId = :languageId")
    suspend fun getProgress(languageId: String): LanguageProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: LanguageProgress)

    @Query("SELECT totalSwipes FROM progress_insights WHERE languageId = :languageId")
    suspend fun getTotalSwipes(languageId: String): Int?

    @Query("SELECT EXISTS(SELECT 1 FROM progress_insights WHERE languageId = :languageId)")
    suspend fun hasProgressForLanguage(languageId: String): Boolean

    @Query("SELECT * FROM progress_insights")
    suspend fun getAllProgress(): List<LanguageProgress>

    @Update
    suspend fun updateProgress(progress: LanguageProgress)
}

