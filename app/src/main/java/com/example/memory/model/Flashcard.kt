package com.example.memory.model

data class Flashcard(
    val text: String,
    val translations: List<String>,
    val wordId: String
)

data class FlashcardUnit(
    val unitName: String,
    val flashcards: List<Flashcard>
)

data class FlashcardSection(
    val sectionName:    String,
    val units:  List<FlashcardUnit>
)
