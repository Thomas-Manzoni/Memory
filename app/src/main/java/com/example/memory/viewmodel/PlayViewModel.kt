package com.example.memory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.memory.data.database.DatabaseProvider
import com.example.memory.data.entity.FlashcardInsight
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

    private val _currentSectionVal = MutableLiveData(0)
    val currentSectionVal: LiveData<Int> = _currentSectionVal
    private val _currentUnitVal = MutableLiveData(0)
    val currentUnitVal: LiveData<Int> = _currentUnitVal

    private var numberOfSections = 3

    var selectedSection = false
    var selectedSectionIndex = 0
    var selectedUnit = false
    var selectedUnitIndex = 0

    // Database stuff ----------------------------------------
    private val db = DatabaseProvider.getDatabase(application.applicationContext)
    private val insightDao = db.flashcardInsightDao()

    // I think it's an example of the function
    fun updateCorrectAnswer(flashcardId: String) {
        viewModelScope.launch {
            val currentInsight = insightDao.getInsight(flashcardId) ?: FlashcardInsight(flashcardId)
            val updatedInsight = currentInsight.copy(
                timesReviewed = currentInsight.timesReviewed + 1,
                timesCorrect = currentInsight.timesCorrect + 1,
                lastReviewed = System.currentTimeMillis()
            )
            insightDao.insertInsight(updatedInsight)
        }
    }

    fun updateWrongAnswer(flashcardId: String) {
        viewModelScope.launch {
            val currentInsight = insightDao.getInsight(flashcardId) ?: FlashcardInsight(flashcardId)
            val updatedInsight = currentInsight.copy(
                timesReviewed = currentInsight.timesReviewed + 1,
                timesCorrect = currentInsight.timesCorrect - 1,
                lastReviewed = System.currentTimeMillis()
            )
            insightDao.insertInsight(updatedInsight)
        }
    }
    // -----------------------------------------------------------------------

    init {
        loadFlashcardRepo()
    }

    private fun loadFlashcardRepo() {
        val sections = repository.loadFlashcardsFromJson()
        _flashcardSections.value = sections
    }

    fun loadRandomFlashcard() {
        //val numberOfSections = _flashcardSections.value?.size ?: 0
        if (numberOfSections == 0) return

        var randomSectionIndex = Random.nextInt(0, numberOfSections)
        if (selectedSection){
            randomSectionIndex = selectedSectionIndex
        }
        _currentSectionVal.value = randomSectionIndex
        val selectedSection = _flashcardSections.value?.get(randomSectionIndex) ?: return

        val unitCount = selectedSection.units.size
        if (unitCount == 0) return

        var randomUnitIndex = Random.nextInt(0, unitCount)
        if (selectedUnit){
            randomUnitIndex = selectedUnitIndex
        }
        _currentUnitVal.value = randomUnitIndex
        val selectedUnit = selectedSection.units[randomUnitIndex]

        val flashcardCount = selectedUnit.flashcards.size
        if (flashcardCount == 0) return

        val randomFlashcardIndex = Random.nextInt(0, flashcardCount)
        val selectedFlashcard = selectedUnit.flashcards[randomFlashcardIndex]
        _currentFlashcard.value = selectedFlashcard
    }

    fun selectSection(index: Int) {
        _flashcardSections.value?.let { units ->
            if (index in units.indices) {
                selectedSection = true
                selectedSectionIndex = index
                _currentSection.value = units[index]
                showUnits()
            }
        }
    }

    fun selectUnit(index: Int) {
        _flashcardUnits.value?.let { units ->
            if (index in units.indices) {
                selectedUnit = true
                selectedUnitIndex = index
                _currentUnit.value = units[index]
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

    fun resetSectionSelection() {
        selectedSection = false
    }

    fun resetUnitSelection() {
        selectedUnit = false
    }
}
