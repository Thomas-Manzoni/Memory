package com.example.memory.repository

import android.content.Context
import com.example.memory.model.Flashcard
import com.example.memory.model.FlashcardUnit
import com.example.memory.model.FlashcardSection
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class FlashcardRepository(
    private val context: Context,
    private val defaultCourse: String = "Swedish"
) {
    private fun getFileNameFromPack(course: String): String {
        return when (course) {
            "Swedish" -> "flashcards_with_ids.json"
            "Spanish" -> "flashcardsEs_with_ids.json"
            "German" -> "flashcardsDe_with_ids.json"
            else -> "flashcards_with_ids.json"
        }
    }

    fun loadFlashcardsFromJson(course: String = defaultCourse): List<FlashcardSection> {
        return try {
            val fileName = getFileNameFromPack(course)
            val jsonString = context.assets.open(fileName)
                .bufferedReader().use { it.readText() }

            val type = object : TypeToken<Map<String, Map<String, List<Flashcard>>>>() {}.type
            val parsedData: Map<String, Map<String, List<Flashcard>>> = Gson().fromJson(jsonString, type) ?: emptyMap()

            // Convert the parsed map into a list of FlashcardSection
            parsedData.map { (sectionName, unitsMap) ->
                FlashcardSection(
                    sectionName = sectionName,
                    units = unitsMap.map { (unitName, flashcards) ->
                        FlashcardUnit(unitName, flashcards)
                    }
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }
}
