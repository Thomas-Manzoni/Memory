package com.example.memory.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.memory.data.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesRepository(private val context: Context) {

    companion object {
        // Define keys for your settings
        private val PROGRESSED_SECTION_KEY = intPreferencesKey("progressed_section")
        private val PROGRESSED_UNIT_KEY = intPreferencesKey("progressed_unit")

        // Default values (adjust as needed)
        private const val DEFAULT_SECTION = 1
        private const val DEFAULT_UNIT = 1
    }

    // Flow to observe the progressed section; emits the default if not yet set.
    val progressedSectionFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PROGRESSED_SECTION_KEY] ?: DEFAULT_SECTION
        }

    // Flow to observe the progressed unit; emits the default if not yet set.
    val progressedUnitFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PROGRESSED_UNIT_KEY] ?: DEFAULT_UNIT
        }

    // Function to update the progressed section.
    suspend fun updateSection(section: Int) {
        context.dataStore.edit { preferences ->
            preferences[PROGRESSED_SECTION_KEY] = section + 1
        }
    }

    // Function to update the progressed unit.
    suspend fun updateUnit(unit: Int) {
        context.dataStore.edit { preferences ->
            preferences[PROGRESSED_UNIT_KEY] = unit + 1
        }
    }
}
