package com.example.hotelres.data.remote

import com.example.hotelres.data.model.Hotel
import retrofit2.http.GET
import retrofit2.http.Header

interface HotelApiService {
    @GET("hotels_mock.json")  // Updated to your new Mockaroo endpoint
    suspend fun getHotels(
        @Header("X-API-Key") apiKey: String
    ): List<Hotel>
}
