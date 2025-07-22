package com.example.hotelres.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotelres.domain.repository.HotelRepository
import com.example.hotelres.data.local.FavoriteHotelsDataStore
import com.example.hotelres.data.model.Booking
import com.example.hotelres.data.model.Hotel
import com.example.hotelres.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HotelViewModel(
    application: Application,
    private val repository: HotelRepository
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allHotels = MutableStateFlow<List<Hotel>>(emptyList())
    private val _favoriteHotelIds = MutableStateFlow<Set<Int>>(emptySet())
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings

    private val _profileImageUri = MutableStateFlow<String?>(null)
    val profileImageUri: StateFlow<String?> = _profileImageUri

    init {
        viewModelScope.launch {
            repository.getHotels().collect { _allHotels.value = it }
        }

        viewModelScope.launch {
            repository.getFavoriteHotelIds().collect { _favoriteHotelIds.value = it }
        }

        viewModelScope.launch {
            repository.getBookings().collect { _bookings.value = it }
        }

        viewModelScope.launch {
            repository.getProfileImageUri().collect { _profileImageUri.value = it }
        }
    }

    val hotels: StateFlow<List<Hotel>> = combine(_allHotels, _searchQuery) { hotels, query ->
        if (query.isBlank()) hotels
        else hotels.filter {
            it.hotel_name.contains(query, ignoreCase = true) ||
                    it.location.city.contains(query, ignoreCase = true) ||
                    it.location.country.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favoriteHotels: StateFlow<List<Hotel>> = combine(
        _allHotels,
        _favoriteHotelIds
    ) { hotels, favIds ->
        hotels.filter { favIds.contains(it.hotel_id) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(hotel: Hotel) {
        val current = _favoriteHotelIds.value.toMutableSet()
        if (current.contains(hotel.hotel_id)) current.remove(hotel.hotel_id)
        else current.add(hotel.hotel_id)

        _favoriteHotelIds.value = current

        viewModelScope.launch {
            repository.saveFavoriteHotelIds(current)
        }
    }

    fun addBooking(booking: Booking) {
        val updated = _bookings.value + booking
        _bookings.value = updated

        viewModelScope.launch {
            repository.saveBookings(updated)
        }
    }

    fun saveProfileImageUri(uri: String?) {
        _profileImageUri.value = uri
        viewModelScope.launch {
            repository.saveProfileImageUri(uri)
        }
    }
}
