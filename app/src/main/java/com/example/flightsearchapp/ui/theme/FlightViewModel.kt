package com.example.flightsearchapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearchapp.FlightSearchApplication
import com.example.flightsearchapp.data.Airport
import com.example.flightsearchapp.data.Favorite
import com.example.flightsearchapp.data.FlightDao
import com.example.flightsearchapp.data.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FlightUiState(
    val searchQuery: String = "",
    val selectedAirport: Airport? = null,
    val airportSuggestions: List<Airport> = emptyList(),
    val destinationAirports: List<Airport> = emptyList(),
    val favoriteFlights: List<Favorite> = emptyList(),
    val allAirports: List<Airport> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class FlightViewModel(
    private val flightDao: FlightDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedAirport = MutableStateFlow<Airport?>(null)

    private val searchQueryFlow: StateFlow<String> = userPreferencesRepository.searchQuery
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    // Autocomplete suggestions when typing in the search bar
    private val airportSuggestionsFlow = searchQueryFlow.flatMapLatest { query ->
        if (query.isBlank()) {
            flowOf(emptyList())
        } else {
            flightDao.searchAirports(query)
        }
    }

    // All destinations when a user taps a specific departure airport
    private val destinationAirportsFlow = _selectedAirport.flatMapLatest { airport ->
        if (airport == null) {
            flowOf(emptyList())
        } else {
            flightDao.getAllDestinations(airport.iataCode)
        }
    }

    // Load all airports once so we can look up full airport names for saved favorite codes
    private val allAirportsFlow = flightDao.searchAirports("")

    val uiState: StateFlow<FlightUiState> = combine(
        searchQueryFlow,
        _selectedAirport,
        airportSuggestionsFlow,
        destinationAirportsFlow,
        flightDao.getFavorites(),
        allAirportsFlow
    ) { flows ->
        val query = flows[0] as String
        val selected = flows[1] as Airport?
        @Suppress("UNCHECKED_CAST")
        val suggestions = flows[2] as List<Airport>
        @Suppress("UNCHECKED_CAST")
        val destinations = flows[3] as List<Airport>
        @Suppress("UNCHECKED_CAST")
        val favorites = flows[4] as List<Favorite>
        @Suppress("UNCHECKED_CAST")
        val allAirports = flows[5] as List<Airport>

        FlightUiState(
            searchQuery = query,
            selectedAirport = selected,
            airportSuggestions = suggestions,
            destinationAirports = destinations,
            favoriteFlights = favorites,
            allAirports = allAirports
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FlightUiState()
    )

    fun updateSearchQuery(query: String) {
        // Clear any selected departure airport when typing a new search
        if (_selectedAirport.value != null) {
            _selectedAirport.value = null
        }
        viewModelScope.launch {
            userPreferencesRepository.saveSearchQuery(query)
        }
    }

    fun selectAirport(airport: Airport) {
        _selectedAirport.value = airport
        viewModelScope.launch {
            userPreferencesRepository.saveSearchQuery(airport.iataCode)
        }
    }

    fun toggleFavorite(departureCode: String, destinationCode: String) {
        viewModelScope.launch {
            val existingFavorite = uiState.value.favoriteFlights.find {
                it.departureCode == departureCode && it.destinationCode == destinationCode
            }
            if (existingFavorite != null) {
                flightDao.deleteFavorite(existingFavorite)
            } else {
                flightDao.insertFavorite(
                    Favorite(
                        departureCode = departureCode,
                        destinationCode = destinationCode
                    )
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FlightSearchApplication)
                val flightDao = application.database.flightDao()
                val userPreferencesRepository = application.userPreferencesRepository
                FlightViewModel(
                    flightDao = flightDao,
                    userPreferencesRepository = userPreferencesRepository
                )
            }
        }
    }
}