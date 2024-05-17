package com.example.powerpath.retrofitApi.dataClasses

import com.google.gson.annotations.SerializedName

data class UserFilterRetrofit(
    val id: Int,
    @SerializedName("power_range_min") val powerRangeMin: Int,
    @SerializedName("power_range_max") val powerRangeMax: Int,
    @SerializedName("connector_type") val connectorType: String,
    val networks: List<String>,
    @SerializedName("minimal_rating") val minimalRating: Int,
    @SerializedName("station_count") val stationCount: Int,
    val paid: Boolean,
    val free: Boolean,
    val durability: Int
)