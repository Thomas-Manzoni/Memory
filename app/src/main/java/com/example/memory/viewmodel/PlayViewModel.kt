package com.example.memory.viewmodel

import android.app.Application
import android.text.format.DateUtils
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.memory.data.dao.CategoryDao
import com.example.memory.data.dao.FlashcardCategoryDao
import com.example.memory.data.dao.FlashcardInsightDao
import com.example.memory.data.dao.InsightPosition
import com.example.memory.data.dao.ProgressInsightDao
import com.example.memory.data.database.AppDatabase
import kotlinx.coroutines.launch
import com.example.memory.data.database.DatabaseProvider
import com.example.memory.data.entity.FlashcardCategoryCrossRef
import com.example.memory.data.entity.FlashcardInsight
import com.example.memory.data.entity.FlashcardWithCategories
import com.example.memory.data.entity.LanguageProgress
import com.example.memory.data.entity.LearnStatus
import com.example.memory.model.Flashcard
import com.example.memory.model.FlashcardUnit
import com.example.memory.model.FlashcardSection
import com.example.memory.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import java.util.TimeZone
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

    // Reading TTS
    private val _sourceLang = MutableStateFlow("sv-SE")
    val sourceLang: StateFlow<String> = _sourceLang
    private val _sourceVoice = MutableStateFlow("sv-SE")
    val sourceVoice: StateFlow<String> = _sourceVoice

    // To be able to load cards by category (mostly for exercise section)
    private val _displayCards = MutableStateFlow<List<Flashcard>>(emptyList())
    val displayCards: StateFlow<List<Flashcard>> = _displayCards
    enum class CardDisplayType {
        CATEGORY,
        FAVORITE,
        FORGOTTEN
    }

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

    // CategoryMode
    var categoryMode = false
    var categorySelected = ""

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
    private var categoriesRefDaoVariable: FlashcardCategoryDao? = null
    val categoriesRefDao: FlashcardCategoryDao
        get() = categoriesRefDaoVariable ?: throw IllegalStateException("DAO not initialized. Call switchCourse() first.")
    private var categoriesDaoVariable:  CategoryDao? = null
    val categoriesDao: CategoryDao
        get() = categoriesDaoVariable ?: throw IllegalStateException("DAO not initialized. Call switchCourse() first.")

    private val progressDatabase = DatabaseProvider.getProgressDatabase(getApplication())
    private val progressDao = progressDatabase.progressInsightDao()
    private var currentCourse = ""

    private val _newEntriesAlert = MutableLiveData<String?>()
    val newEntriesAlert: LiveData<String?> get() = _newEntriesAlert
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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
        categoriesRefDaoVariable = db?.flashcardCategoryDao()
        categoriesDaoVariable = db?.categoryDao()

        setSourceLang(courseId)

        viewModelScope.launch {
            _isLoading.value = true
            if(progressDao.hasProgressForLanguage(languageId = courseId)) {
                Log.d("PlayCardViewModel", "Skipped card populating")
//                populateFlashcardInsightsIfNeeded()
//                populateCrossRefCategoryDatabase()
            } else {

                populateFlashcardInsightsIfNeeded()
                populateCrossRefCategoryDatabase()
            }
            _isLoading.value = false
            val deletedCount = categoriesRefDao.deleteDuplicateCrossRefs()
            Log.d("Database", "Deleted $deletedCount duplicate category cross-references")
            val deletedCount2 = insightDao.deleteDuplicateInsights()
            Log.d("Database", "Deleted $deletedCount2 duplicate flashcard insights")
            populateAndLoadProgressDatabase(course = courseId)
        }
    }

    fun loadFlashcardRepo(course: String? = null) {
        val sections = if (course != null)
            repository.loadFlashcardsFromJson(course)
        else
            repository.loadFlashcardsFromJson()

        _flashcardSections.value = sections
    }

    private suspend fun populateCrossRefCategoryDatabase() {
        // 1) Load your master category list once
        val masterCats: Set<String> = categoriesDao.getAllCategories()
            .map { it.name }
            .toSet()

        var newLinks = 0

        _flashcardSections.value?.forEach { section ->
            section.units.forEach { unit ->
                unit.flashcards.forEach { flashcard ->
                    val fcId = flashcard.wordId
                    val semCats  = flashcard.categories ?: emptyList() // → emptyList()
                    val gramCats = flashcard.grammar    ?: emptyList() // → emptyList()
                    val desired  = (semCats + gramCats)               // → emptyList()
                        .filter { it in masterCats }                   // → still emptyList()
                        .toSet()

                    // 2) Load existing links for this card
                    val existingLinks: Set<String> = categoriesRefDao.loadWithCategories(fcId).categories.map { it.name }.toSet()

                    // 3) For each desired category that’s in the master list, ensure a link
                    (desired intersect masterCats).forEach { catName ->
                        if (catName !in existingLinks) {
                            categoriesRefDao.insertCrossRef(
                                FlashcardCategoryCrossRef(
                                    flashcardId  = fcId,
                                    categoryName = catName
                                )
                            )
                            newLinks++
                        }
                    }

                    // 4) (Optional) Remove links for categories no longer desired
                    // (existingLinks - desired).forEach { staleCat ->
                    //   flashcardCategoryDao.removeCrossRef(fcId, staleCat)
                    // }
                }
            }
        }

        Log.d("CrossRefInit", "Inserted $newLinks new cross‑refs")

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

        Log.d("PlayCardViewModel", "Finished card populating")
    }

    private suspend fun populateAndLoadProgressDatabase(course: String) {
        try {
            Log.d("PlayCardViewModel", "Start trying to load progress")

            val nowMs = System.currentTimeMillis()
            val tzOffset = TimeZone.getDefault().rawOffset
            val todayEpochDay = ((nowMs + tzOffset) / DateUtils.DAY_IN_MILLIS)

            // Try to get the existing progress for this language
            val existingProgress = progressDao.getProgress(course)

            if (existingProgress == null) {
                // No progress entry exists for this language, create a new one…

                Log.d("PlayCardViewModel", "No progress record found for $course; creating default entry with lastChecked=$todayEpochDay")
                val newProgress = LanguageProgress(
                    languageId = course,
                    // all your other fields get their defaults,
                    lastCheckedEpochDay = todayEpochDay
                )
                progressDao.insertProgress(newProgress)

                _progressedSection = 0
                _progressedUnit = 0
                // Reset any other progress tracking variables to defaults
            } else {
                // Progress entry exists, update our UI or ViewModel state with the saved values
                Log.d("PlayCardViewModel", "Found existing progress for $course: Section ${existingProgress.progressedSection}, Unit ${existingProgress.progressedUnit}")
                _progressedSection = existingProgress.progressedSection
                _progressedUnit = existingProgress.progressedUnit

                val deltaDays: Int = (todayEpochDay - existingProgress.lastCheckedEpochDay)
                    .coerceAtLeast(0L)
                    .toInt()

                shiftProgressDay(offsetDay = deltaDays, todayEpochDay = todayEpochDay)

            }
        } catch (e: Exception) {
            Log.e("PlayCardViewModel", "Error loading progress data for $course", e)
            // Fallback to default values if database operation fails
            _progressedSection = 0
            _progressedUnit = 0
        }
    }

    private suspend fun shiftProgressDay(offsetDay: Int, todayEpochDay: Long) {
        if (offsetDay <= 0) return

        val allProgress = progressDao.getAllProgress()
        val windowSize = 7

        allProgress.forEach { progress ->
            val old = listOf(
                progress.swipesD1,
                progress.swipesD2,
                progress.swipesD3,
                progress.swipesD4,
                progress.swipesD5,
                progress.swipesD6,
                progress.swipesD7,
            )

            val newWindow: List<Int> = if (offsetDay >= windowSize) {
                List(windowSize) { 0 }
            } else {
                List(windowSize) { idx ->
                    if (idx < offsetDay) 0
                    else              old[idx - offsetDay]
                }
            }

            val updated = progress.copy(
                swipesD1 = newWindow[0],
                swipesD2 = newWindow[1],
                swipesD3 = newWindow[2],
                swipesD4 = newWindow[3],
                swipesD5 = newWindow[4],
                swipesD6 = newWindow[5],
                swipesD7 = newWindow[6],
                lastCheckedEpochDay = todayEpochDay
            )

            progressDao.updateProgress(updated)
        }
    }


    suspend fun loadCardsToDisplay(cardType: CardDisplayType) {
        // 1) Load appropriate flashcard IDs based on type
        val cardIds: Set<String> = when (cardType) {
            CardDisplayType.CATEGORY ->
                categoriesRefDao.loadByCategory(categorySelected)
                    .map { it.flashcard.flashcardId }
                    .toSet()

            CardDisplayType.FAVORITE ->
                insightDao.loadFavoriteCards()
                    .map { it.flashcardId }
                    .toSet()

            CardDisplayType.FORGOTTEN ->
                insightDao.loadForgottenCards()
                    .map { it.flashcardId }
                    .toSet()
        }

        // 2) Filter in-memory flashcards using the IDs
        val sections = _flashcardSections.value ?: emptyList()
        val result: List<Flashcard> = sections
            .flatMap { sec ->
                sec.units.flatMap { unit ->
                    unit.flashcards.filter { it.wordId in cardIds }
                }
            }

        // 3) Publish to UI
        _displayCards.value = result
    }

    private fun languageNameToTag(name: String): String =
        when (name) {
            "Swedish"  -> "sv-SE"
            "Spanish"  -> "es-ES"
            "French"   -> "fr-FR"
            "English"  -> "en-US"
            // …add your other courses here
            else       -> Locale.getDefault().toLanguageTag()
        }

    private fun languageNameToVoice(name: String): String =
        when (name) {
            "Swedish"  -> "sv-se-x-cmh-network"
            "Spanish"  -> "es-es-x-eed-network"
            "French"   -> "fr-fr-x-frd-network"
            "English"  -> "en-US"
            // …add your other courses here
            else       -> Locale.getDefault().toLanguageTag()
        }

    /** Exposed so you can update the source-text language from anywhere */
    fun setSourceLang(languageName: String) {
        _sourceLang.value = languageNameToTag(languageName)
        _sourceVoice.value = languageNameToVoice(languageName)
    }

    suspend fun fetchDescription(flashcardId: String): String {
        val currentInsight = insightDao.getInsight(flashcardId)
        if (currentInsight == null) {
            Log.d("PlayCardViewModel", "No FlashcardInsight found for $flashcardId; inserting default record")
            return "Not found"
        } else {
            return currentInsight.description
       }
    }

    suspend fun fetchIsFavorite(flashcardId: String): Boolean {
        val currentInsight = insightDao.getInsight(flashcardId)
        if (currentInsight == null) {
            Log.d("PlayCardViewModel", "No FlashcardInsight found for $flashcardId; inserting default record")
            return false
        } else {
            return currentInsight.isFavorite
        }
    }

    suspend fun fetchLearnStatus(flashcardId: String): LearnStatus {
        val currentInsight = insightDao.getInsight(flashcardId)
        return currentInsight?.learnStatus ?: LearnStatus.UNKNOWN
    }

    suspend fun updateFavoriteStatus(flashcardId: String, isFavoriteInput: Boolean) {
        val currentInsight = insightDao.getInsight(flashcardId)
        if (currentInsight == null) {
            Log.d("PlayCardViewModel", "No FlashcardInsight found for $flashcardId; inserting default record")
        } else {
            val updatedInsight = currentInsight.copy(
                isFavorite = isFavoriteInput
            )
            insightDao.updateInsight(updatedInsight)  // Use update instead of insert
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
            insightDao.updateInsight(updatedInsight)
        }
    }

    suspend fun updateCorrectAnswer(flashcardId: String) {
        val currentInsight = insightDao.getInsight(flashcardId)
        val currentProgress = progressDao.getProgress(languageId = currentCourse)
        val currentTimeMillis = System.currentTimeMillis()
        if (currentInsight != null) {
            val newLearnStatus = when (currentInsight.learnStatus) {
                LearnStatus.UNKNOWN -> LearnStatus.LEARNING
                LearnStatus.FORGOTTEN -> LearnStatus.LEARNING
                LearnStatus.LEARNING -> {
                    val timeSinceLastChange = currentTimeMillis - (currentInsight.lastStatusChange ?: 0)
                    val thirtyMinutesInMillis = 30 * 60 * 1000

                    if (timeSinceLastChange > thirtyMinutesInMillis) {
                        LearnStatus.KNOWN
                    } else {
                        LearnStatus.LEARNING // Stay in learning state if not enough time has passed
                    }
                }
                LearnStatus.KNOWN -> LearnStatus.KNOWN
                else -> LearnStatus.LEARNING
            }

            val lastStatusChangeTime = if (newLearnStatus != currentInsight.learnStatus) {
                currentTimeMillis // Update timestamp only when status changes
            } else {
                currentInsight.lastStatusChange // Keep existing timestamp if no change
            }

            val updatedInsight = currentInsight.copy(
                timesReviewed = currentInsight.timesReviewed + 1,
                timesCorrect = currentInsight.timesCorrect + 1,
                lastReviewed = currentTimeMillis,
                learnStatus = newLearnStatus,
                lastStatusChange = lastStatusChangeTime
            )
            insightDao.updateInsight(updatedInsight)
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
        val currentTimeMillis = System.currentTimeMillis()

        if (currentInsight != null) {
            // For wrong answers, we always set status to FORGOTTEN
            // We should update lastStatusChange only when the status actually changes
            val lastStatusChangeTime = if (currentInsight.learnStatus != LearnStatus.FORGOTTEN) {
                currentTimeMillis // Update timestamp only when status changes
            } else {
                currentInsight.lastStatusChange // Keep existing timestamp if already FORGOTTEN
            }

            val updatedInsight = currentInsight.copy(
                timesReviewed = currentInsight.timesReviewed + 1,
                timesWrong = currentInsight.timesWrong + 1,
                lastReviewed = currentTimeMillis,
                learnStatus = LearnStatus.FORGOTTEN,
                lastStatusChange = lastStatusChangeTime
            )
            insightDao.updateInsight(updatedInsight)
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
                learnStatus = LearnStatus.UNKNOWN,
                lastReviewed = 0
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

    suspend fun getRecentlySwipedCards(limit: Int = 7): List<String> {
        // 1) grab the raw insight rows
        val positions: List<InsightPosition> =
            insightDao.getRecentInsightPositions(limit)

        // 2) snapshot your in‑memory sections (backed by LiveData)
        val sections: List<FlashcardSection> =
            _flashcardSections.value.orEmpty()

        val result = mutableListOf<String>()

        // 3) for each insight, pull out the indices & id, then find the exact card
        for (pos in positions) {
            // bounds‑safe get the section
            val section = sections.getOrNull(pos.sectionIndex) ?: continue
            // bounds‑safe get the unit
            val unit = section.units.getOrNull(pos.unitIndex) ?: continue

            // find the one flashcard in that unit whose id matches
            val cardText = unit.flashcards
                .firstOrNull { it.wordId == pos.flashcardId }
                ?.text
                ?: continue

            result += cardText
        }

        return result
    }

    suspend fun getRecentlyMissSwipedCards(limit: Int = 7): List<String> {
        // 1) grab the raw insight rows
        val positions: List<InsightPosition> =
            insightDao.getRecentMissSwipeInsightPositions(limit)

        // 2) snapshot your in‑memory sections (backed by LiveData)
        val sections: List<FlashcardSection> =
            _flashcardSections.value.orEmpty()

        val result = mutableListOf<String>()

        // 3) for each insight, pull out the indices & id, then find the exact card
        for (pos in positions) {
            // bounds‑safe get the section
            val section = sections.getOrNull(pos.sectionIndex) ?: continue
            // bounds‑safe get the unit
            val unit = section.units.getOrNull(pos.unitIndex) ?: continue

            // find the one flashcard in that unit whose id matches
            val cardText = unit.flashcards
                .firstOrNull { it.wordId == pos.flashcardId }
                ?.text
                ?: continue

            result += cardText
        }

        return result
    }


    suspend fun getWeekSwipes(): List<Int> {
        val progress = progressDao.getProgress(languageId = currentCourse)
        return if (progress != null) {
            listOf(
                progress.swipesD1,
                progress.swipesD2,
                progress.swipesD3,
                progress.swipesD4,
                progress.swipesD5,
                progress.swipesD6,
                progress.swipesD7
            )
        } else {
            List(7) { 0 }
        }
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









    // ---------------------------------------------------------------------------



    suspend fun loadRandomFlashcard() {
        var selectedUnit: FlashcardUnit? = null
        val numberOfSections = _flashcardSections.value?.size

        if (numberOfSections == 0) return

//            val time1 = System.currentTimeMillis()
        val triple = selectWeightedRandomCard() ?: return
//            val time2 = System.currentTimeMillis()
//            Log.d("Perf", "load random card took ${time2 - time1} ms")
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

    private suspend fun selectWeightedRandomCard(): Triple<Int, Int, Int>? {
        val flashcardPicks = if (untilProgressedUnit) {
            insightDao.debugWeightedPicksUntilProgress(maxSection = progressedSection, maxUnit = progressedUnit)
        } else if (randomWeightedMode){
            insightDao.debugWeightedPicks(learnStatus = LearnStatus.UNKNOWN.ordinal)
        } else if (preSelectionMode){
            insightDao.debugWeightedPicksFromUnit(slctSection = preSelectedSectionIndex, slctUnit = preSelectedUnitIndex)
        } else {
            categoriesRefDao.debugWeightedPicksCategory(category = categorySelected)
        }
        flashcardPicks.forEach { pick ->
            Log.d("WeightedDebug", "ID: ${pick.flashcardId}, rand: ${pick.score - pick.mistakeWeight}, mistakes: ${pick.mistakeWeight}, score: ${pick.score}")
        }

        //val randomFlashcardId = insightDao.getWeightedRandomCardId()
        val randomFlashcardId = flashcardPicks.firstOrNull()?.flashcardId ?: return null

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

    private fun showUnits() {
        _currentSection.value?.units?.let { units ->
            if (units.isNotEmpty()) {
                // Assigns the values of all the units
                _flashcardUnits.value = units
            }
        }
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

    private fun showFlashcard() {
        _currentUnit.value?.flashcards?.let { flashcards ->
            if (flashcards.isNotEmpty()) {
                _currentFlashcard.value = flashcards[currentFlashcardIndex]
            }
        }
    }
}
