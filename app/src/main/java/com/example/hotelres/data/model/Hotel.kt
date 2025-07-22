package com.example.hotelres.data.model

data class Hotel(
    val hotel_id: Int,
    val hotel_name: String,
    val location: Location,
    val address: String,
    val image_url: String,
    val price_per_night: Double,
    val rating: Float,
    val num_reviews: Int
)

data class Location(
    val city: String,
    val country: String
)
