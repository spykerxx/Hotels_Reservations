package com.example.hotelres.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.hotelres.data.repository.HotelRepositoryImpl
import com.example.hotelres.data.local.FavoriteHotelsDataStore
import com.example.hotelres.presentation.screens.MainScreen
import com.example.hotelres.presentation.theme.HotelResTheme
import com.example.hotelres.presentation.viewmodel.HotelViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            HotelResTheme(useDarkTheme = isDarkTheme) {
                val navController = rememberNavController()

                // Instantiate repository and ViewModel factory
                val repository = remember {
                    HotelRepositoryImpl(
                        apiKey = "71551580",
                        dataStore = FavoriteHotelsDataStore(applicationContext)
                    )
                }

                val viewModelFactory = remember {
                    object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return HotelViewModel(application, repository) as T
                        }
                    }
                }

                val viewModel: HotelViewModel = viewModel(factory = viewModelFactory)

                MainScreen(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = it },
                    viewModel = viewModel
                )
            }
        }
    }
}
