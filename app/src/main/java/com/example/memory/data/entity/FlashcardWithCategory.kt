package com.example.memory.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

// This is the dataclass that will be returned when asked, it will return the flashcard and a list of categories
data class FlashcardWithCategories(
    @Embedded
    val flashcard: FlashcardInsight,

    @Relation(
        parentColumn  = "flashcardId",   // in FlashcardInsight
        entityColumn  = "name",          // in Category
        associateBy   = Junction(
            value = FlashcardCategoryCrossRef::class,
            parentColumn = "flashcardId",  // column in the junction that refers to FlashcardInsight
            entityColumn = "categoryName"  // column in the junction that refers to Category.name
        )
    )
    val categories: List<Category>
)
