package com.example.memory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.memory.model.Flashcard
import com.example.memory.model.FlashcardUnit
import com.example.memory.model.FlashcardSection
import com.example.memory.repository.FlashcardRepository
import kotlin.random.Random

class PlayCardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FlashcardRepository(application.applicationContext)

    private val _flashcardSections = MutableLiveData<List<FlashcardSection>>()
    val flashcardSections: LiveData<List<FlashcardSection>> get() = _flashcardSections

    private val _flashcardUnits = MutableLiveData<List<FlashcardUnit>>()
    val flashcardUnits: LiveData<List<FlashcardUnit>> get() = _flashcardUnits

    private val _currentSection = MutableLiveData<FlashcardSection?>()
    val currentSection: LiveData<FlashcardSection?> get() = _currentSection

    private val _currentUnit = MutableLiveData<FlashcardUnit?>()
    val currentUnit: LiveData<FlashcardUnit?> get() = _currentUnit

    private val _currentFlashcard = MutableLiveData<Flashcard?>()
    val currentFlashcard: LiveData<Flashcard?> get() = _currentFlashcard

    private var currentFlashcardIndex = 0
    private var numberOfSections = 2

    init {
        loadFlashcardRepo()
        loadRandomFlashcard()
    }

    private fun loadFlashcardRepo() {
        val sections = repository.loadFlashcardsFromJson()
        _flashcardSections.value = sections
    }

    fun loadRandomFlashcard() {
        val numberOfSections = _flashcardSections.value?.size ?: 0
        if (numberOfSections == 0) return // Prevent crash if no sections exist

        val randomSectionIndex = Random.nextInt(0, numberOfSections) // Ensure valid index
        val selectedSection = _flashcardSections.value?.get(randomSectionIndex) ?: return

        val unitCount = selectedSection.units.size
        if (unitCount == 0) return // Prevent crash if no units exist

        val randomUnitIndex = Random.nextInt(0, unitCount) // Ensure valid index
        val selectedUnit = selectedSection.units[randomUnitIndex]

        // Now, count the flashcards (words) inside the selected unit
        val flashcardCount = selectedUnit.flashcards.size // This gives the number of flashcards
        if (flashcardCount == 0) return

        val randomFlashcardIndex = Random.nextInt(0, flashcardCount)
        val selectedFlashcard = selectedUnit.flashcards[randomFlashcardIndex]
        _currentFlashcard.value = selectedFlashcard
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

    fun selectSection(index: Int) {
        _flashcardSections.value?.let { units ->
            if (index in units.indices) {
                _currentSection.value = units[index]
                showUnits()
            }
        }
    }

    private fun showUnits() {
        _currentSection.value?.units?.let { units ->
            if (units.isNotEmpty()) {
                // Assigns the values of all the units
                _flashcardUnits.value = units
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
