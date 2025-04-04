package com.example.memory.data.dao

import androidx.room.*
import com.example.memory.data.entity.FlashcardInsight

@Dao
interface FlashcardInsightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: FlashcardInsight)

    @Query("SELECT * FROM flashcard_insights WHERE flashcardId = :id")
    suspend fun getInsight(id: String): FlashcardInsight?

    @Update
    suspend fun updateInsight(insight: FlashcardInsight)
}
