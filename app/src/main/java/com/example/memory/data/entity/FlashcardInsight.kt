package com.example.memory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcard_insights")
data class FlashcardInsight(
    @PrimaryKey val flashcardId: String,
    val timesReviewed: Int = 0,
    val timesCorrect: Int = 0,
    val timesWrong: Int = 0,
    val lastReviewed: Long = 0L,
    val description: String = "Init",
    val sectionIndex: Int = -1,
    val unitIndex: Int = -1,
    val lastSwipe: Int = 0
)