package com.example.memory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress_insights")
data class LanguageProgress(
    @PrimaryKey val languageId: String,
    val progressedSection: Int = 1,
    val progressedUnit: Int = 1,
    val totalSwipes: Int = 0,
    val swipesD1: Int = 0,
    val swipesD2: Int = 0,
    val swipesD3: Int = 0,
    val swipesD4: Int = 0,
    val swipesD5: Int = 0,
    val swipesD6: Int = 0,
    val swipesD7: Int = 0
)