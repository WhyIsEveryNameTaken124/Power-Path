package com.example.powerpath

import com.example.powerpath.classes.FiltersData

object DataManager {
    const val url = "https://power-path-backend-3e6dc9fdeee0.herokuapp.com"
    var email = "user@example.com"
    var connectorType = ""
    var selectedNetworks = mutableListOf<String>()
    var filtersData: FiltersData = FiltersData(0, 0, "", mutableListOf(String()), 0, 0, hasPaid = false, hasFree = false)
}