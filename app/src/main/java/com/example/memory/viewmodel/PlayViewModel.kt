package com.example.memory.viewmodel

import android.app.Application
import android.util.Log
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
import com.example.memory.repository.PreferencesRepository

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
    private var currentFlashcardIndex = 0

    // For random selection
    private val _currentSectionVal = MutableLiveData(0)
    val currentSectionVal: LiveData<Int> = _currentSectionVal
    private val _currentUnitVal = MutableLiveData(0)
    val currentUnitVal: LiveData<Int> = _currentUnitVal

    // Pre-selection
    var preSelectedSection = false
    var preSelectedSectionIndex = 0
    var preSelectedUnit = false
    var preSelectedUnitIndex = 0
    var preSelectedCard = false // for random weighted
    var preSelectedCardIndex = 0

    // From progress
    var untilProgressedUnit = false
    private var totUnitsProgressed = 0

    // Random weighted mode
    var randomWeightedMode = false

    // To do the load from repo only when necessary
    private var totUnitsLoaded = false

    // Database  ----------------------------------------
    private val db = DatabaseProvider.getDatabase(application.applicationContext)
    private val insightDao = db.flashcardInsightDao()

    private val _newEntriesAlert = MutableLiveData<String?>()
    val newEntriesAlert: LiveData<String?> get() = _newEntriesAlert

    suspend fun fetchDescription(flashcardId: String): String {
        val currentInsight = insightDao.getInsight(flashcardId)
        if (currentInsight == null) {
            Log.d("PlayCardViewModel", "No FlashcardInsight found for $flashcardId; inserting default record")
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

    suspend fun updateCorrectAnswer(flashcardId: String) {
        val currentInsight = insightDao.getInsight(flashcardId) ?: FlashcardInsight(flashcardId)
        val updatedInsight = currentInsight.copy(
            timesReviewed = currentInsight.timesReviewed + 1,
            timesCorrect = currentInsight.timesCorrect + 1,
            lastReviewed = System.currentTimeMillis(),
            lastSwipe = -1
        )
        insightDao.insertInsight(updatedInsight)
    }

    suspend fun updateWrongAnswer(flashcardId: String) {
        val currentInsight = insightDao.getInsight(flashcardId) ?: FlashcardInsight(flashcardId)
        val updatedInsight = currentInsight.copy(
            timesReviewed = currentInsight.timesReviewed + 1,
            timesWrong = currentInsight.timesWrong + 1,
            lastReviewed = System.currentTimeMillis(),
            lastSwipe = 1
        )
        insightDao.insertInsight(updatedInsight)
    }

    // New Function: Reset all flashcard insights
    suspend fun resetAllFlashcardSwipes() {
        val allInsights = insightDao.getAllInsights()
        val resetInsights = allInsights.map { insight ->
            insight.copy(
                timesCorrect = 0,
                timesWrong = 0,
                lastSwipe = 0
            )
        }
        insightDao.insertInsights(resetInsights)
    }

    suspend fun getTotalSwipes(): Int {
        val totalReviewed = insightDao.getTotalTimesReviewed() ?: 0
        return totalReviewed
    }



    suspend fun fetchReviewStats(flashcardId: String): Pair<Int, Int> {
        val currentInsight = insightDao.getInsight(flashcardId)
        if (currentInsight == null) {
            Log.d("PlayCardViewModel", "No FlashcardInsight found for $flashcardId; inserting default record")
            return Pair(0, 0)
        } else {
            return Pair(currentInsight.timesReviewed, currentInsight.timesCorrect)
        }
    }
    // -----------------------------------------------------------------------
    // Loading stored progress -----------------------------------------------

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
            var updatedCount = 0

            _flashcardSections.value?.forEachIndexed { sectionIdx, section ->
                section.units.forEachIndexed { unitIdx, unit ->
                    unit.flashcards.forEach { flashcard ->
                        val existingInsight = insightDao.getInsight(flashcard.wordId)
                        if (existingInsight == null) {
                            insightDao.insertInsight(
                                FlashcardInsight(
                                    flashcardId = flashcard.wordId,
                                    sectionIndex = sectionIdx,
                                    unitIndex = unitIdx
                                )
                            )
                            newCount++
                        } else {
                            // If sectionIndex or unitIndex is -1 (or outdated), update them
                            if (existingInsight.sectionIndex != sectionIdx || existingInsight.unitIndex != unitIdx) {
                                val updatedInsight = existingInsight.copy(
                                    sectionIndex = sectionIdx,
                                    unitIndex = unitIdx
                                )
                                insightDao.updateInsight(updatedInsight)
                                updatedCount++
                            }
                        }
                    }
                }
            }

            if (newCount > 0 || updatedCount > 0) {
                _newEntriesAlert.value = buildString {
                    if (newCount > 0) append("$newCount new flashcard insights were inserted. ")
                    if (updatedCount > 0) append("$updatedCount flashcard insights were updated.")
                }
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

    // ---------------------------------------------------------------------------

    private fun loadFlashcardRepo() {
        val sections = repository.loadFlashcardsFromJson()
        _flashcardSections.value = sections
    }

    suspend fun loadRandomFlashcard() {
        var selectedUnit: FlashcardUnit? = null
        val numberOfSections = _flashcardSections.value?.size

        if (numberOfSections == 0) return

        if (randomWeightedMode){
            val time1 = System.currentTimeMillis()
            val triple = selectWeightedRandomCard() ?: return
            val time2 = System.currentTimeMillis()
            Log.d("Perf", "load random card took ${time2 - time1} ms")
            val (sectionIndex, unitIndex, cardIndex) = triple

            // Just like in your full random logic
            preSelectedSectionIndex = sectionIndex
            preSelectedUnitIndex = unitIndex
            preSelectedCardIndex = cardIndex

            _currentSectionVal.value = sectionIndex
            _currentUnitVal.value = unitIndex

            val selectedSemiRandomSection = _flashcardSections.value?.get(sectionIndex) ?: return
            val selectedSemiRandomUnit = selectedSemiRandomSection.units.getOrNull(unitIndex) ?: return
            val selectedSemiRandomFlashcard = selectedSemiRandomUnit.flashcards.getOrNull(cardIndex) ?: return

            _currentFlashcard.value = selectedSemiRandomFlashcard
            return
        }

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
            var randomSectionIndex = numberOfSections?.let { Random.nextInt(0, it) }
            if (preSelectedSection) {
                randomSectionIndex = preSelectedSectionIndex
            }
            _currentSectionVal.value = randomSectionIndex
            val selectedSection = randomSectionIndex?.let { _flashcardSections.value?.get(it) } ?: return

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

    private fun loadTotUnitProgressed() {
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

    // Weighted-random-selection ---------------------------------------------------------

    suspend fun selectWeightedRandomCard(): Triple<Int, Int, Int>? {
        val picks = insightDao.debugWeightedPicks()
        picks.forEach {
            Log.d("WeightedDebug", "ID: ${it.flashcardId}, rand: ${it.score - it.mistakeWeight}, mistakes: ${it.mistakeWeight}, score: ${it.score}")
        }

        //val randomFlashcardId = insightDao.getWeightedRandomCardId()
        val randomFlashcardId = picks.firstOrNull()?.flashcardId ?: return null

        val insight = insightDao.getInsight(randomFlashcardId) ?: return null

        val sectionIndex = insight.sectionIndex
        val unitIndex = insight.unitIndex

        val section = _flashcardSections.value?.getOrNull(sectionIndex) ?: return null
        val unit = section.units.getOrNull(unitIndex) ?: return null
        val flashcardIndex = unit.flashcards.indexOfFirst { it.wordId == randomFlashcardId }

        if (flashcardIndex == -1) return null

        return Triple(sectionIndex, unitIndex, flashcardIndex)
    }


    // -----------------------------------------------------------------------------------

    // Pre-selection functions -----------------------------------------------------------
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
    // -------------------------------------------------------------------------------------

    fun updateProgress(newSection: Int, newUnit: Int) {
        viewModelScope.launch {
            preferencesRepository.updateSection(newSection)
            preferencesRepository.updateUnit(newUnit)
        }
        totUnitsLoaded = false
    }

    // Exercise functions ------------------------------------------------------------------
    fun selectUnitExercise(index: Int) {
        _flashcardUnits.value?.let { units ->
            if (index in units.indices) {
                _currentUnit.value = units[index]
                currentFlashcardIndex = 0
                showFlashcard()
            }
        }
    }

    fun selectSectionExercise(index: Int) {
        _flashcardSections.value?.let { units ->
            if (index in units.indices) {
                _currentSection.value = units[index]
                showUnits()
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
