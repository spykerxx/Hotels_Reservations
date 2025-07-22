package com.example.hotelres.data.repository

import com.example.hotelres.domain.repository.HotelRepository
import com.example.hotelres.data.local.FavoriteHotelsDataStore
import com.example.hotelres.data.model.Booking
import com.example.hotelres.data.model.Hotel
import com.example.hotelres.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HotelRepositoryImpl(
    private val apiKey: String,
    private val dataStore: FavoriteHotelsDataStore
) : HotelRepository {

    override fun getHotels(): Flow<List<Hotel>> = flow {
        try {
            val hotels = RetrofitInstance.api.getHotels(apiKey)
            emit(hotels)
        } catch (e: Exception) {
            emit(emptyList()) // Could emit error states in a more advanced version
        }
    }

    override fun getFavoriteHotelIds(): Flow<Set<Int>> = dataStore.favoriteHotelIdsFlow

    override fun getBookings(): Flow<List<Booking>> = dataStore.bookingsFlow

    override fun getProfileImageUri(): Flow<String?> = dataStore.profileImageUriFlow

    override suspend fun saveFavoriteHotelIds(ids: Set<Int>) {
        dataStore.saveFavoriteHotelIds(ids)
    }

    override suspend fun saveBookings(bookings: List<Booking>) {
        dataStore.saveBookings(bookings)
    }

    override suspend fun saveProfileImageUri(uri: String?) {
        dataStore.saveProfileImageUri(uri)
    }
}