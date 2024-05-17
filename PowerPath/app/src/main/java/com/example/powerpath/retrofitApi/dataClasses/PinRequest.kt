package com.example.powerpath.retrofitApi.dataClasses

data class PinRequest(
    val email: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

