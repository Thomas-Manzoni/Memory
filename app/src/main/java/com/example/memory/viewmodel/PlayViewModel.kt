package com.example.memory.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.memory.data.database.DatabaseProvider
import com.example.memory.data.entity.FlashcardInsight
import com.example.memory.model.Flashcard
import com.example.memory.model.FlashcardUnit
import com.example.memory.model.FlashcardSection
import com.example.memory.repository.FlashcardRepository
import kotlin.random.Random
import com.example.memory.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class PlayCardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FlashcardRepository(application.applicationContext)
    private val preferencesRepository = PreferencesRepository(application)

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

    var preSelectedSection = false
    var preSelectedSectionIndex = 0
    var preSelectedUnit = false
    var preSelectedUnitIndex = 0

    var untilProgressedUnit = false
    var totUnitsProgressed = 0

    var totUnitsLoaded = false



    // Database stuff ----------------------------------------
    private val db = DatabaseProvider.getDatabase(application.applicationContext)
    private val insightDao = db.flashcardInsightDao()

    private val _newEntriesAlert = MutableLiveData<String?>()
    val newEntriesAlert: LiveData<String?> get() = _newEntriesAlert



    suspend fun fetchDescription(flashcardId: String): String {
            var currentInsight = insightDao.getInsight(flashcardId)
            if (currentInsight == null) {
                Log.d("PlayCardViewModel", "No FlashcardInsight found for $flashcardId; inserting default record")
                // Create a new record with the default description ("Init")
                return "Not found"
            } else {
                return currentInsight.description
            }
        }

    fun clearNewEntriesAlert() {
        _newEntriesAlert.value = null
    }

    fun updateDescription(flashcardId: String, newDescription: String) {
        viewModelScope.launch {
            val currentInsight = insightDao.getInsight(flashcardId) ?: FlashcardInsight(flashcardId)
            val updatedInsight = currentInsight.copy(
                lastReviewed = System.currentTimeMillis(),
                description = newDescription
            )
            insightDao.insertInsight(updatedInsight)
        }
    }



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

    private var _progressedSection: Int = 1
    val progressedSection: Int
        get() = _progressedSection

    private var _progressedUnit: Int = 1
    val progressedUnit: Int
        get() = _progressedUnit

    init {
        loadFlashcardRepo() // Loads flashcard sections into _flashcardSections

        viewModelScope.launch {
            var newCount = 0
            _flashcardSections.value?.forEach { section ->
                section.units.forEach { unit ->
                    unit.flashcards.forEach { flashcard ->
                        val existingInsight = insightDao.getInsight(flashcard.wordId)
                        if (existingInsight == null) {
                            // Insert a default insight record (default description "Init")
                            insightDao.insertInsight(FlashcardInsight(flashcardId = flashcard.wordId))
                            newCount++
                        }
                    }
                }
            }
            if (newCount > 0) {
                _newEntriesAlert.value = "$newCount new flashcard insights were inserted."
            }
        }

        viewModelScope.launch {
            preferencesRepository.progressedSectionFlow.collect { section ->
                _progressedSection = section
            }
        }
        // Collect the progressed unit Flow continuously.
        viewModelScope.launch {
            preferencesRepository.progressedUnitFlow.collect { unit ->
                _progressedUnit = unit
            }
        }
    }


    private fun loadFlashcardRepo() {
        val sections = repository.loadFlashcardsFromJson()
        _flashcardSections.value = sections
    }

    fun loadRandomFlashcard() {
        var selectedUnit: FlashcardUnit? = null

        if (numberOfSections == 0) return

        if (untilProgressedUnit){
            if(!totUnitsLoaded){
                loadTotUnitProgressed()
                totUnitsLoaded = true
            }
            selectRandomUnitFromProgress()?.let { (section, unit) ->
                _currentSectionVal.value = section - 1
                _currentUnitVal.value = unit - 1
                val selectedSection = _flashcardSections.value?.get(section - 1) ?: return
                selectedUnit = selectedSection.units[unit - 1]
            }
        } else {
            var randomSectionIndex = Random.nextInt(0, numberOfSections)
            if (preSelectedSection) {
                randomSectionIndex = preSelectedSectionIndex
            }
            _currentSectionVal.value = randomSectionIndex
            val selectedSection = _flashcardSections.value?.get(randomSectionIndex) ?: return

            val unitCount = selectedSection.units.size
            if (unitCount == 0) return

            var randomUnitIndex = Random.nextInt(0, unitCount)
            if (preSelectedUnit) {
                randomUnitIndex = preSelectedUnitIndex
            }
            _currentUnitVal.value = randomUnitIndex
            selectedUnit = selectedSection.units[randomUnitIndex]
        }

        val flashcardCount = selectedUnit?.flashcards?.size
        if (flashcardCount == 0) return

        val randomFlashcardIndex = flashcardCount?.let { Random.nextInt(0, it) }
        val selectedFlashcard = randomFlashcardIndex?.let { selectedUnit?.flashcards?.get(it) }
        _currentFlashcard.value = selectedFlashcard
    }

    fun loadTotUnitProgressed() {
        totUnitsProgressed = 0
        for (i in 1..progressedSection) {
            if (i == progressedSection) {
                totUnitsProgressed += progressedUnit
            } else {
                val momentarilySelectedSection = _flashcardSections.value?.get(i - 1) ?: return
                val unitCount = momentarilySelectedSection.units.size
                totUnitsProgressed += unitCount
            }
        }
    }


    private fun selectRandomUnitFromProgress(): Pair<Int, Int>? {
        var randomUnitIndex = Random.nextInt(1, totUnitsProgressed + 1)
        for (i in 1..progressedSection) {
            val momentarilySelectedSection = _flashcardSections.value?.get(i-1)
            val unitCount = momentarilySelectedSection?.units?.size
            if (randomUnitIndex <= unitCount!!) {
                return Pair(i, randomUnitIndex)
            } else {
                randomUnitIndex -= unitCount
            }
        }
        return null
    }


    fun selectSection(index: Int) {
        _flashcardSections.value?.let { units ->
            if (index in units.indices) {
                preSelectedSection = true
                preSelectedSectionIndex = index
                _currentSection.value = units[index]
                showUnits()
            }
        }
    }

    fun selectUnit(index: Int) {
        _flashcardUnits.value?.let { units ->
            if (index in units.indices) {
                preSelectedUnit = true
                preSelectedUnitIndex = index
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
        preSelectedSection = false
    }

    fun resetUnitSelection() {
        preSelectedUnit = false
    }

    fun updateProgress(newSection: Int, newUnit: Int) {
        viewModelScope.launch {
            preferencesRepository.updateSection(newSection)
            preferencesRepository.updateUnit(newUnit)
        }
        totUnitsLoaded = false
    }
}
