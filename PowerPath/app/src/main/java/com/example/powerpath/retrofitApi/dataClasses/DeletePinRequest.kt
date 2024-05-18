package com.example.powerpath.retrofitApi.dataClasses

data class DeletePinRequest(
    val email: String,
    val name: String,
    val latitude: String,
    val longitude: String
)
