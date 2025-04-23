package com.example.memory.data.dao

import androidx.room.*
import com.example.memory.data.entity.Category
import com.example.memory.data.entity.FlashcardCategoryCrossRef
import com.example.memory.data.entity.FlashcardInsight
import com.example.memory.data.entity.FlashcardWithCategories

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

    @Query("DELETE FROM flashcard_insights")
    suspend fun deleteAllInsights()

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

    @Query("""
    SELECT flashcardId, sectionIndex, unitIndex
      FROM flashcard_insights
    ORDER BY lastReviewed DESC
    LIMIT :limit
  """)
    suspend fun getRecentInsightPositions(limit: Int = 7): List<InsightPosition>

    @Query("""
    SELECT flashcardId, sectionIndex, unitIndex
      FROM flashcard_insights
      WHERE lastSwipe = -1
    ORDER BY lastReviewed DESC
    LIMIT :limit
  """)
    suspend fun getRecentMissedInsightPositions(limit: Int = 7): List<InsightPosition>
}

data class DebugPick(
    val flashcardId: String,
    val mistakeWeight: Double,
    val score: Double
)

data class InsightPosition(
    val flashcardId: String,
    val sectionIndex: Int,
    val unitIndex: Int
)

// ---------------- category

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(cat: Category)

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>
}

@Dao
interface FlashcardCategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(xref: FlashcardCategoryCrossRef)

    // 3) Load a flashcard + its categories
    @Transaction
    @Query("SELECT * FROM flashcard_insights WHERE flashcardId = :id")
    suspend fun loadWithCategories(id: String): FlashcardWithCategories

    // 4) Load all + categories
    @Transaction
    @Query("SELECT * FROM flashcard_insights")
    suspend fun loadAllWithCategories(): List<FlashcardWithCategories>

    // 5) Load only those in a given category
    @Transaction
    @Query("""
    SELECT f.*
      FROM flashcard_insights AS f
      JOIN flashcard_category_xref AS x
        ON f.flashcardId = x.flashcardId
     WHERE x.categoryName = :catName
  """)
    suspend fun loadByCategory(catName: String): List<FlashcardWithCategories>
}