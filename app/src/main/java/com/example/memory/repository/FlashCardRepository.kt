package com.example.memory.repository

import android.content.Context
import com.example.memory.model.Flashcard
import com.example.memory.model.FlashcardUnit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class FlashcardRepository(private val context: Context) {

    private var cachedData: Map<String, List<Flashcard>>? = null

    fun loadFlashcardsFromJson(): List<FlashcardUnit> {
        return try {
            val jsonString = context.assets.open("flashcards.json")
                .bufferedReader().use { it.readText() }

            val type = object : TypeToken<Map<String, List<Flashcard>>>() {}.type
            val parsedData: Map<String, List<Flashcard>> = Gson().fromJson(jsonString, type) ?: emptyMap()

            // Convert the parsed map into a list of FlashcardUnit
            parsedData.map { (unitName, flashcards) ->
                FlashcardUnit(unitName, flashcards)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getNumberOfUnits(): Int {
        if (cachedData == null) {
            loadFlashcardsFromJson()
        }
        return cachedData?.size ?: 0
    }
}
