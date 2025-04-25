package com.example.memory.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "flashcard_insights")
data class FlashcardInsight(
    @PrimaryKey val flashcardId: String,
    val timesReviewed: Int = 0,
    val timesCorrect: Int = 0,
    val timesWrong: Int = 0,
    val lastReviewed: Long = 0L,
    val description: String = "",
    val sectionIndex: Int = -1,
    val unitIndex: Int = -1,
    val lastSwipe: Int = 0,
    val isFavorite: Boolean = false
)

// I have a table that holds only the categories
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val name: String   // e.g. "food", "animals", …
)

// And I think a table with a list of connections like [chicken(id), food] [chicken, animal]
@Entity(
    tableName = "flashcard_category_xref",
    primaryKeys = ["flashcardId", "categoryName"],
    foreignKeys = [
        ForeignKey(
            entity = FlashcardInsight::class,
            parentColumns = ["flashcardId"],
            childColumns  = ["flashcardId"],
            onDelete      = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["name"],
            childColumns  = ["categoryName"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoryName"])
    ]
)

data class FlashcardCategoryCrossRef(
    val flashcardId: String,
    val categoryName: String
)