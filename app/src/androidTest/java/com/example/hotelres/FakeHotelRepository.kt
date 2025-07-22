package com.example.hotelres

import com.example.hotelres.data.model.Booking
import com.example.hotelres.data.model.Hotel
import com.example.hotelres.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class FakeHotelRepository : HotelRepository {

    private val _hotels = MutableStateFlow<List<Hotel>>(emptyList())
    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    private val _profileImageUri = MutableStateFlow<String?>(null)

    override fun getHotels(): Flow<List<Hotel>> = _hotels

    override fun getFavoriteHotelIds(): Flow<Set<Int>> = _favoriteIds

    override fun getBookings(): Flow<List<Booking>> = _bookings

    override fun getProfileImageUri(): Flow<String?> = _profileImageUri

    override suspend fun saveFavoriteHotelIds(ids: Set<Int>) {
        _favoriteIds.value = ids
    }

    override suspend fun saveBookings(bookings: List<Booking>) {
        _bookings.value = bookings
    }

    override suspend fun saveProfileImageUri(uri: String?) {
        _profileImageUri.value = uri
    }

    // Helper functions to set initial data for tests

    fun setDummyHotels(hotels: List<Hotel>) {
        _hotels.value = hotels
    }

    fun setFavoriteIds(ids: Set<Int>) {
        _favoriteIds.value = ids
    }

    fun setDummyBookings(bookings: List<Booking>) {
        _bookings.value = bookings
    }

    fun setProfileImageUri(uri: String?) {
        _profileImageUri.value = uri
    }
}
