package com.example.flightsearchapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightsearchapp.ui.FlightSearchApp
import com.example.flightsearchapp.ui.FlightViewModel
import com.example.flightsearchapp.ui.theme.FlightSearchAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlightSearchAppTheme {
                val viewModel: FlightViewModel = viewModel(factory = FlightViewModel.Factory)
                FlightSearchApp(viewModel = viewModel)
            }
        }
    }
}