package com.example.hotelres.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.google.gson.GsonBuilder

object RetrofitInstance {
    private const val BASE_URL = "https://my.api.mockaroo.com/"

    private val gson = GsonBuilder()
        .setLenient()  // <-- This enables lenient parsing
        .create()

    val api: HotelApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(HotelApiService::class.java)
    }
}


