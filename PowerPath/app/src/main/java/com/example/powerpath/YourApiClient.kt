package com.example.powerpath

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class YourApiClient {

    private val baseUrl = "https://power-path-backend-3e6dc9fdeee0.herokuapp.com/"

    private val client = OkHttpClient()

    fun getChargingStations(latitude: String, longitude: String) {
        val url = baseUrl.toHttpUrlOrNull()
            ?.newBuilder()
            ?.addPathSegment("charging_stations")
            ?.addQueryParameter("latitude", latitude)
            ?.addQueryParameter("longitude", longitude)
            ?.build()

        val request = Request.Builder()
            .url(url!!)
            .get()
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                // Handle the response here
                println(responseBody)
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Handle the error here
                e.printStackTrace()
            }
        })
    }
}
