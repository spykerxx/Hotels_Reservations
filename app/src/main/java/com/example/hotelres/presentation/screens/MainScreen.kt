package com.example.hotelres.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.hotelres.R
import com.example.hotelres.presentation.viewmodel.HotelViewModel

@Composable
fun MainScreen(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    viewModel: HotelViewModel
) {
    val items = listOf("hotels", "favorites", "profile")

    // Collect states from ViewModel
    val hotels by viewModel.hotels.collectAsState()
    val favoriteHotels by viewModel.favoriteHotels.collectAsState()

    // Get current route reactively
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                "hotels" -> Icon(
                                    painter = painterResource(id = R.drawable.hotel),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                "favorites" -> Icon(Icons.Default.Favorite, contentDescription = null)
                                "profile" -> Icon(Icons.Default.Person, contentDescription = null)
                            }
                        },
                        label = { Text(screen.replaceFirstChar { it.uppercase() }) },
                        selected = currentRoute == screen,
                        onClick = {
                            navController.navigate(screen) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "hotels",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("hotels") {
                HotelListScreen(
                    hotels = viewModel.hotels.collectAsState().value,
                    searchQuery = viewModel.searchQuery.collectAsState().value,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onHotelClick = { hotel ->
                        navController.navigate("hotelDetails/${hotel.hotel_id}")
                    }
                )
            }
            composable("favorites") {
                FavoritesScreen(favoriteHotels = favoriteHotels, onHotelClick = { hotel ->
                    navController.navigate("hotelDetails/${hotel.hotel_id}")
                })
            }
            composable("profile") {
                val bookings by viewModel.bookings.collectAsState()

                ProfileScreen(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    bookings = bookings,
                    savedProfileImageUri = viewModel.profileImageUri.collectAsState().value,  // pass saved URI from ViewModel
                    onProfileImageChange = { uri ->
                        viewModel.saveProfileImageUri(uri?.toString()) // save updated URI to ViewModel/DataStore
                    },
                    onEditProfile = {
                        // Handle edit profile action here
                        // e.g., navigate to an edit profile screen or show a dialog
                    },
                    onLogout = {
                        // Handle logout action here
                        // e.g., clear user session, navigate to login screen
                    }
                )
            }
            composable(
                route = "hotelDetails/{hotelId}",
                arguments = listOf(navArgument("hotelId") { type = NavType.IntType })
            ) { backStackEntry ->
                val hotelId = backStackEntry.arguments?.getInt("hotelId") ?: 0
                val hotel = hotels.find { it.hotel_id == hotelId }
                if (hotel != null) {
                    val isFavorite = favoriteHotels.contains(hotel)
                    HotelDetailsScreen(
                        hotel = hotel,
                        navController = navController,
                        isFavorite = isFavorite,
                        onToggleFavorite = { viewModel.toggleFavorite(hotel) }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hotel not found", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            composable(
                route = "booking/{hotelId}",
                arguments = listOf(navArgument("hotelId") { type = NavType.IntType })
            ) { backStackEntry ->
                val hotelId = backStackEntry.arguments?.getInt("hotelId") ?: 0
                val hotel = hotels.find { it.hotel_id == hotelId }
                if (hotel != null) {
                    BookingScreen(
                        viewModel = viewModel, // ✅ pass the viewModel
                        hotelName = hotel.hotel_name,
                        onBookingConfirmed = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hotel not found", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }


        }
    }
}
