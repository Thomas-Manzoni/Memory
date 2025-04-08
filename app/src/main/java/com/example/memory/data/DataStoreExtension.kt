package com.example.memory.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

// This creates an extension property on Context that lazily instantiates a DataStore<Preferences> instance.
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

