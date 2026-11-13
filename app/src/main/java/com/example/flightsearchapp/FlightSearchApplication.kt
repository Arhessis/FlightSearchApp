package com.example.flightsearchapp

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.flightsearchapp.data.FlightDatabase
import com.example.flightsearchapp.data.UserPreferencesRepository

private const val FLIGHT_PREFERENCE_NAME = "flight_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = FLIGHT_PREFERENCE_NAME
)

class FlightSearchApplication : Application() {
    val database: FlightDatabase by lazy { FlightDatabase.getDatabase(this) }
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(dataStore)
    }
}