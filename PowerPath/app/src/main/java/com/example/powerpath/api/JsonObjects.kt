package com.example.powerpath.api

data class Pin(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val user_id: Int
)