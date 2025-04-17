package com.example.memory.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.memory.data.dao.FlashcardInsightDao
import com.example.memory.data.dao.ProgressInsightDao
import com.example.memory.data.database.AppDatabase
import kotlinx.coroutines.launch
import com.example.memory.data.database.DatabaseProvider
import com.example.memory.data.entity.FlashcardInsight
import com.example.memory.data.entity.LanguageProgress
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

    // For random selection
    private val _currentSectionVal = MutableLiveData(0)
    val currentSectionVal: LiveData<Int> = _currentSectionVal
    private val _currentUnitVal = MutableLiveData(0)
    val currentUnitVal: LiveData<Int> = _currentUnitVal

    // Pre-selection
    var preSelectionMode = false
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
    private var db: AppDatabase? = null
    private var insightDaoVariable: FlashcardInsightDao? = null
    val insightDao: FlashcardInsightDao
        get() = insightDaoVariable ?: throw IllegalStateException("DAO not initialized. Call switchCourse() first.")

    private val progressDatabase = DatabaseProvider.getProgressDatabase(getApplication())
    private val progressDao = progressDatabase.progressInsightDao()
    private var currentCourse = ""




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

    suspend fun updateDescription(flashcardId: String, newDescription: String) {
        val currentInsight = insightDao.getInsight(flashcardId)

        if (currentInsight != null) {
            val updatedInsight = currentInsight.copy(
                lastReviewed = System.currentTimeMillis(),
                description = newDescription
            )
            insightDao.insertInsight(updatedInsight)
        }
    }

    suspend fun updateCorrectAnswer(flashcardId: String) {
        val currentInsight = insightDao.getInsight(flashcardId)
        val currentProgress = progressDao.getProgress(languageId = currentCourse)

        if (currentInsight != null) {
            val updatedInsight = currentInsight.copy(
                timesReviewed = currentInsight.timesReviewed + 1,
                timesCorrect = currentInsight.timesCorrect + 1,
                lastReviewed = System.currentTimeMillis(),
                lastSwipe = -1
            )
            insightDao.insertInsight(updatedInsight)
        }

        if (currentProgress != null) {
            val updatedProgress = currentProgress.copy(
                swipesD1 = currentProgress.swipesD1 + 1,
                totalSwipes = currentProgress.totalSwipes + 1
            )
            progressDao.insertProgress(updatedProgress)
        }
    }

    suspend fun updateWrongAnswer(flashcardId: String) {
        val currentInsight = insightDao.getInsight(flashcardId)
        val currentProgress = progressDao.getProgress(languageId = currentCourse)

        if (currentInsight != null) {
            val updatedInsight = currentInsight.copy(
                timesReviewed = currentInsight.timesReviewed + 1,
                timesWrong = currentInsight.timesWrong + 1,
                lastReviewed = System.currentTimeMillis(),
                lastSwipe = 1
            )
            insightDao.insertInsight(updatedInsight)
        }

        if (currentProgress != null) {
            val updatedProgress = currentProgress.copy(
                swipesD1 = currentProgress.swipesD1 + 1,
                totalSwipes = currentProgress.totalSwipes + 1
            )
            progressDao.insertProgress(updatedProgress)
        }
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

    suspend fun resetAllFlashcard() {
        insightDao.deleteAllInsights()
    }

    suspend fun getTotalSwipes(): Int {
        val totalReviewed = progressDao.getTotalSwipes(languageId = currentCourse) ?: 0
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
        switchCourse(courseId = "Swedish")
    }


    fun switchCourse(courseId: String) {
        loadFlashcardRepo(course = courseId)
        currentCourse = courseId
        val dbName = "flashcard_${courseId}.db"
        db = DatabaseProvider.getDatabase(getApplication(), dbName)
        insightDaoVariable = db?.flashcardInsightDao()

        viewModelScope.launch {
            populateFlashcardInsightsIfNeeded()
            populateAndLoadProgressDatabase(course = courseId)
        }
    }

    private suspend fun populateAndLoadProgressDatabase(course: String) {
        try {
            // Try to get the existing progress for this language
            val existingProgress = progressDao.getProgress(course)

            if (existingProgress == null) {
                // No progress entry exists for this language, create a new one with default values
                Log.d("PlayCardViewModel", "No progress record found for $course; creating default entry")
                val newProgress = LanguageProgress(
                    languageId = course,
                    // All other fields will use the default values from the entity class
                )
                progressDao.insertProgress(newProgress)

                // Now we can update our UI or ViewModel state with the default values
                _progressedSection = 1
                _progressedUnit = 1
                // Reset any other progress tracking variables to defaults
            } else {
                // Progress entry exists, update our UI or ViewModel state with the saved values
                Log.d("PlayCardViewModel", "Found existing progress for $course: Section ${existingProgress.progressedSection}, Unit ${existingProgress.progressedUnit}")
                _progressedSection = existingProgress.progressedSection
                _progressedUnit = existingProgress.progressedUnit
                // Update any other progress tracking variables
            }
        } catch (e: Exception) {
            Log.e("PlayCardViewModel", "Error loading progress data for $course", e)
            // Fallback to default values if database operation fails
            _progressedSection = 1
            _progressedUnit = 1
        }
    }

    private suspend fun populateFlashcardInsightsIfNeeded() {
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
                    } else if (
                        existingInsight.sectionIndex != sectionIdx ||
                        existingInsight.unitIndex != unitIdx
                    ) {
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

        if (newCount > 0 || updatedCount > 0) {
            _newEntriesAlert.value = buildString {
                if (newCount > 0) append("$newCount new flashcard insights were inserted. ")
                if (updatedCount > 0) append("$updatedCount flashcard insights were updated.")
            }
        }
    }


    // ---------------------------------------------------------------------------

    fun loadFlashcardRepo(course: String? = null) {
        val sections = if (course != null)
            repository.loadFlashcardsFromJson(course)
        else
            repository.loadFlashcardsFromJson()

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
                _currentSectionVal.value = section
                _currentUnitVal.value = unit
                val selectedSection = _flashcardSections.value?.get(section) ?: return
                selectedUnit = selectedSection.units[unit]
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
        for (i in 0..progressedSection) {
            if (i == progressedSection) {
                totUnitsProgressed += progressedUnit
            } else {
                val momentarilySelectedSection = _flashcardSections.value?.get(i) ?: return
                val unitCount = momentarilySelectedSection.units.size
                totUnitsProgressed += unitCount
            }
        }
    }

    private fun selectRandomUnitFromProgress(): Pair<Int, Int>? {
        var randomUnitIndex = if (totUnitsProgressed > 0) {
            Random.nextInt(0, totUnitsProgressed + 1) // The +1 is not because of wrongly indexed units/sections but on how the random works 0,1 will return only 0
        } else {
            // Handle the edge case — maybe return 0 or throw a custom exception
            0
        }
        for (i in 0..progressedSection) {
            val momentarilySelectedSection = _flashcardSections.value?.get(i)
            val unitCount = momentarilySelectedSection?.units?.size
            if (randomUnitIndex < unitCount!!) {
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

    suspend fun updateProgress(newSection: Int, newUnit: Int) {
        val currentProgress = progressDao.getProgress(languageId = currentCourse)
        if (currentProgress != null) {
            val updatedProgress = currentProgress.copy(
                progressedSection = newSection,
                progressedUnit = newUnit
            )
            progressDao.insertProgress(updatedProgress)
        }
        _progressedSection = newSection
        _progressedUnit = newUnit
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
