package com.example.powerpath.classes

data class FiltersData(
    val minPowerRange: Int,
    val maxPowerRange: Int,
    val connectorType: String,
    val networks: List<String>,
    val minRating: Int,
    val minStationCount: Int,
    val hasPaid: Boolean,
    val hasFree: Boolean
)
