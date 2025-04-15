package com.example.memory.data.dao

import androidx.room.*
import com.example.memory.data.entity.FlashcardInsight

@Dao
interface FlashcardInsightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: FlashcardInsight)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsights(insights: List<FlashcardInsight>)

    @Query("SELECT * FROM flashcard_insights WHERE flashcardId = :id")
    suspend fun getInsight(id: String): FlashcardInsight?

    @Query("SELECT * FROM flashcard_insights")
    suspend fun getAllInsights(): List<FlashcardInsight>

    @Update
    suspend fun updateInsight(insight: FlashcardInsight)

    @Query("""
    SELECT flashcardId,
           mistakeWeight,
           (randPart + mistakeWeight) AS score
    FROM (
        SELECT flashcardId,
               (ABS(RANDOM()) % 1000000) / 10000.0 AS randPart,
               (1 + lastSwipe) * 10 AS mistakeWeight
        FROM flashcard_insights
    )
    ORDER BY score DESC
    LIMIT 10
    """)
    suspend fun debugWeightedPicks(): List<DebugPick>

    @Query("SELECT SUM(timesReviewed) FROM flashcard_insights")
    suspend fun getTotalTimesReviewed(): Int?
}

data class DebugPick(
    val flashcardId: String,
    val mistakeWeight: Double,
    val score: Double
)