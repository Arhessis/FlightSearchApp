# Flight Search - Jetpack Compose Android App

An offline-first Android application built with Jetpack Compose that allows users to search for airports, view available outbound flight destinations, and save favorite flight routes. Developed as the capstone project for Unit 6 (*Data Persistence*) of Google's **Android Basics with Compose** curriculum.

---

## ✨ Features

- **Real-Time Airport Autocomplete:** Queries a pre-populated SQLite database (`flight_search.db`) by airport name or IATA code, sorting suggestions by passenger volume.
- **Outbound Flight Destinations:** Selecting a departure airport dynamically lists all available destination routes with departure and arrival IATA codes and full airport names.
- **Persistent Favorite Routes:** Users can bookmark/unbookmark flight routes to a local Room `favorite` table, displayed automatically whenever the search bar is empty.
- **Search Query Persistence:** Uses **Preferences DataStore** to save the user's active search text so it is automatically restored across app restarts and process death.
- **Material Design 3 UI:** Built entirely with declarative Jetpack Compose components (`Scaffold`, `TopAppBar`, `OutlinedTextField`, `LazyColumn`, and `Card`).

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin (`2.2.10`)
- **UI Framework:** Jetpack Compose (Material 3 + Extended Material Icons)
- **Local Database:** Room (`2.7.1`) with KSP (`2.2.10-2.0.2`) and pre-populated SQLite asset (`createFromAsset`)
- **Key-Value Storage:** Jetpack Preferences DataStore
- **Architecture:** Model-View-ViewModel (MVVM) + Unidirectional Data Flow (UDF)
- **Asynchronous Streams:** Kotlin Coroutines & `StateFlow` (`combine`, `flatMapLatest`)
- **Build System:** Gradle (Kotlin DSL `.kts`, AGP 9.x)

---

## 📂 Project Structure

```text
com.example.flightsearchapp/
│
├── data/
│   ├── Airport.kt                     # Room @Entity mapping the pre-populated 'airport' table
│   ├── Favorite.kt                    # Room @Entity mapping user-saved 'favorite' routes
│   ├── FlightDao.kt                   # Data Access Object with reactive Flow queries
│   ├── FlightDatabase.kt              # RoomDatabase singleton loading assets/database/flight_search.db
│   └── UserPreferencesRepository.kt   # Preferences DataStore wrapper persisting search queries
│
├── ui/
│   ├── theme/                         # Material 3 colors, shapes, and typography
│   ├── FlightSearchScreen.kt          # Main Compose UI (Search bar, Suggestions, Destinations, Favorites)
│   └── FlightViewModel.kt             # ViewModel combining Room & DataStore flows into FlightUiState
│
├── FlightSearchApplication.kt         # Custom Application class initializing Room & DataStore
└── MainActivity.kt                    # Entry point hoisting FlightViewModel via ViewModelProvider.Factory
