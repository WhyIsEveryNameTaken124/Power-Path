package com.example.powerpath.api

data class Pin(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val user_id: Int
)

data class UserFilter(
    val id: Int,
    val power_range_min: Int,
    val power_range_max: Int,
    val connector_type: String,
    val networks: List<String>,
    val minimal_rating: Int,
    val station_count: Int,
    val paid: Boolean,
    val free: Boolean,
    val durability: Int
)
