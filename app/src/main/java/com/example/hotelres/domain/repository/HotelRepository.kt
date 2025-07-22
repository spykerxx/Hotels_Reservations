package com.example.hotelres.domain.repository

import com.example.hotelres.data.model.Booking
import com.example.hotelres.data.model.Hotel
import kotlinx.coroutines.flow.Flow

interface HotelRepository {
    fun getHotels(): Flow<List<Hotel>>
    fun getFavoriteHotelIds(): Flow<Set<Int>>
    fun getBookings(): Flow<List<Booking>>
    fun getProfileImageUri(): Flow<String?>

    suspend fun saveFavoriteHotelIds(ids: Set<Int>)
    suspend fun saveBookings(bookings: List<Booking>)
    suspend fun saveProfileImageUri(uri: String?)
}