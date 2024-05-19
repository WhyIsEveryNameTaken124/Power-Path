package com.example.powerpath.userData

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewDataManager @Inject constructor() {
    var email: String = ""
    var connectorType: String = ""
    var selectedNetworks: MutableList<String> = mutableListOf()
}
