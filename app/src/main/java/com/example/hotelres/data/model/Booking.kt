package com.example.hotelres.data.model

data class Booking(
    val hotelName: String,
    val roomType: String,
    val guests: Int,
    val checkIn: String,
    val checkOut: String
)