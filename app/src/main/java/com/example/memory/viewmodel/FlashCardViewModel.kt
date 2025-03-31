package com.example.memory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.memory.model.Flashcard
import com.example.memory.model.FlashcardUnit
import com.example.memory.repository.FlashcardRepository

class FlashcardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FlashcardRepository(application.applicationContext)

    private val _flashcardUnits = MutableLiveData<List<FlashcardUnit>>()
    val flashcardUnits: LiveData<List<FlashcardUnit>> get() = _flashcardUnits

    private val _currentUnit = MutableLiveData<FlashcardUnit?>()
    val currentUnit: LiveData<FlashcardUnit?> get() = _currentUnit

    private val _currentFlashcard = MutableLiveData<Flashcard?>()
    val currentFlashcard: LiveData<Flashcard?> get() = _currentFlashcard

    private var currentFlashcardIndex = 0

    init {
        loadFlashcards()
    }

    private fun loadFlashcards() {
        val units = repository.loadFlashcardsFromJson()
        _flashcardUnits.value = units

        // Automatically select the first unit if available
        if (units.isNotEmpty()) {
            selectUnit(43)
        }
    }

    fun selectUnit(index: Int) {
        _flashcardUnits.value?.let { units ->
            if (index in units.indices) {
                _currentUnit.value = units[index]
                currentFlashcardIndex = 0
                showFlashcard()
            }
        }
    }

    private fun showFlashcard() {
        _currentUnit.value?.flashcards?.let { flashcards ->
            if (flashcards.isNotEmpty()) {
                _currentFlashcard.value = flashcards[currentFlashcardIndex]
            }
        }
    }

    fun showNextFlashcard() {
        _currentUnit.value?.flashcards?.let { flashcards ->
            if (flashcards.isNotEmpty()) {
                // Increment the index before setting the flashcard
                currentFlashcardIndex = (currentFlashcardIndex + 1) % flashcards.size
                _currentFlashcard.value = flashcards[currentFlashcardIndex]
            }
        }
    }

    fun showPrevFlashcard() {
        _currentUnit.value?.flashcards?.let { flashcards ->
            if (flashcards.isNotEmpty()) {
                // Decrement the index before setting the flashcard
                currentFlashcardIndex = (currentFlashcardIndex - 1 + flashcards.size) % flashcards.size
                _currentFlashcard.value = flashcards[currentFlashcardIndex]
            }
        }
    }
}
