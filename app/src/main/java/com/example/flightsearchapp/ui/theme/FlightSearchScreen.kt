package com.example.flightsearchapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.flightsearchapp.data.Airport
import com.example.flightsearchapp.data.Favorite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchApp(
    viewModel: FlightViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flight Search") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Input Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Enter departure airport") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Switch between Favorites, Autocomplete Suggestions, and Destination Flights
            when {
                uiState.searchQuery.isBlank() -> {
                    FavoritesSection(
                        favorites = uiState.favoriteFlights,
                        allAirports = uiState.allAirports,
                        onToggleFavorite = { dep, dest ->
                            viewModel.toggleFavorite(dep, dest)
                        }
                    )
                }
                uiState.selectedAirport != null -> {
                    val departureAirport = uiState.selectedAirport!!
                    DestinationsSection(
                        departureAirport = departureAirport,
                        destinations = uiState.destinationAirports,
                        favorites = uiState.favoriteFlights,
                        onToggleFavorite = { dep, dest ->
                            viewModel.toggleFavorite(dep, dest)
                        }
                    )
                }
                else -> {
                    SuggestionsList(
                        airports = uiState.airportSuggestions,
                        onSelectAirport = { airport ->
                            viewModel.selectAirport(airport)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionsList(
    airports: List<Airport>,
    onSelectAirport: (Airport) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(airports, key = { it.id }) { airport ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectAirport(airport) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = airport.iataCode,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(56.dp)
                )
                Text(
                    text = airport.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun DestinationsSection(
    departureAirport: Airport,
    destinations: List<Airport>,
    favorites: List<Favorite>,
    onToggleFavorite: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Flights from ${departureAirport.iataCode}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(destinations, key = { it.id }) { destination ->
                val isFavorite = favorites.any {
                    it.departureCode == departureAirport.iataCode &&
                            it.destinationCode == destination.iataCode
                }
                FlightCard(
                    departureCode = departureAirport.iataCode,
                    departureName = departureAirport.name,
                    destinationCode = destination.iataCode,
                    destinationName = destination.name,
                    isFavorite = isFavorite,
                    onFavoriteClick = {
                        onToggleFavorite(departureAirport.iataCode, destination.iataCode)
                    }
                )
            }
        }
    }
}

@Composable
fun FavoritesSection(
    favorites: List<Favorite>,
    allAirports: List<Airport>,
    onToggleFavorite: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Favorite routes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (favorites.isEmpty()) {
            Text(
                text = "No favorite routes saved yet. Search for an airport above to add routes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(favorites, key = { it.id }) { favorite ->
                    val departureName = allAirports.find { it.iataCode == favorite.departureCode }?.name ?: ""
                    val destinationName = allAirports.find { it.iataCode == favorite.destinationCode }?.name ?: ""

                    FlightCard(
                        departureCode = favorite.departureCode,
                        departureName = departureName,
                        destinationCode = favorite.destinationCode,
                        destinationName = destinationName,
                        isFavorite = true,
                        onFavoriteClick = {
                            onToggleFavorite(favorite.departureCode, favorite.destinationCode)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FlightCard(
    departureCode: String,
    departureName: String,
    destinationCode: String,
    destinationName: String,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DEPART",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = departureCode,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = departureName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ARRIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = destinationCode,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = destinationName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}