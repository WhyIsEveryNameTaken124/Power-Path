package com.example.powerpath.api

import android.util.Log
import com.example.powerpath.DataManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class Network {
    fun getFilters(email: String, onSuccess: (userFilter: UserFilter) -> Unit, onFailure: (e: Exception) -> Unit) {
        val url = "https://power-path-backend-3e6dc9fdeee0.herokuapp.com/get_filters?email=$email"

        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("===", "get filters error: $e")
                onFailure.invoke(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d("===", "get filters error: ${response.message}")
                } else {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val jsonResponse = JSONObject(responseData)

                        if (jsonResponse.has("error")) {
                            Log.d("===", "Error: ${jsonResponse.getString("error")}")
                            return
                        }

                        val userFilter = UserFilter(
                            id = jsonResponse.getInt("id"),
                            power_range_min = jsonResponse.getInt("power_range_min"),
                            power_range_max = jsonResponse.getInt("power_range_max"),
                            connector_type = jsonResponse.getString("connector_type"),
                            networks = jsonResponse.getJSONArray("networks")
                                .let { 0.until(it.length()).map { idx -> it.getString(idx) } },
                            minimal_rating = jsonResponse.getInt("minimal_rating"),
                            station_count = jsonResponse.getInt("station_count"),
                            paid = jsonResponse.getBoolean("paid"),
                            free = jsonResponse.getBoolean("free"),
                            durability = jsonResponse.getInt("durability")
                        )

                        onSuccess.invoke(userFilter)
                    }
                }
            }
        })
    }
}