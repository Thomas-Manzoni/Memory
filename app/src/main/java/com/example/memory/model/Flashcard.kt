package com.example.memory.model

data class Flashcard(
    val text: String,
    val translations: List<String>
)

data class FlashcardUnit(
    val unitName: String,
    val flashcards: List<Flashcard>
)
