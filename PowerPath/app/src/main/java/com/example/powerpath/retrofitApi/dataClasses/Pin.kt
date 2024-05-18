package com.example.powerpath.retrofitApi.dataClasses

data class Pin(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val user_id: Int
)

