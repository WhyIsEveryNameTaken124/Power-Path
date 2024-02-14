package com.example.powerpath.api

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
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

    fun saveFilters(email :String, powerRange: Pair<Int, Int>, connectorType: String, networks: List<String>, minRating: Int, minStationCount: Int, paid: Boolean, free: Boolean, durability: Int) {
        val url = "https://power-path-backend-3e6dc9fdeee0.herokuapp.com/save_filters"

        val type = when(connectorType) {
            "CCS Combo Type 1" -> "CCS (Type 1)"
            "CCS Combo Type 2" -> "CCS (Type 2)"
            "CHAdeMO" -> "CHAdeMO"
            "GB/T" -> "GB-T DC - GB/T 20234"
            "Supercharger" -> "NACS / Tesla Supercharger"
            "Type 1 J1772" -> "Type 1 (J1772)"
            "Type 2 Mennekes" -> "Type 2"
            else -> ""
        }

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("power_range", JSONArray().apply {
                put(powerRange.first)
                put(powerRange.second)
            })
            put("connector_type", type)
            put("networks", JSONArray(networks))
            put("minimal_rating", minRating)
            put("station_count", minStationCount)
            put("paid", paid)
            put("free", free)
            put("durability", durability*1000)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("===", "save filters error: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d("===", "save filters error: ${response.message}")
                } else {
                    Log.d("===", "save filters successful: ${response.message}")
                }
            }
        })
    }

    fun login(email :String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val url = "https://power-path-backend-3e6dc9fdeee0.herokuapp.com/login"
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError()
                }
            }
        })
    }

    fun signup(email :String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val url = "https://power-path-backend-3e6dc9fdeee0.herokuapp.com/signup"
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("===", "signup error: $e")
                onError.invoke()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d("===", "signup error: ${response.message}")
                    onError.invoke()
                } else {
                    Log.d("===", "signup successful: ${response.message}")
                    onSuccess.invoke()
                }
            }
        })
    }
    fun savePin(email: String, name: String, latitude: Double, longitude: Double) {
        val url = "https://power-path-backend-3e6dc9fdeee0.herokuapp.com/save_pin"
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("name", name)
            put("latitude", String.format("%.6f", latitude).toDouble())
            put("longitude", String.format("%.6f", longitude).toDouble())
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("===", "save pin error: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d("===", "save pin error: ${response.message}")
                } else {
                    Log.d("===", "save pin successful: ${response.message}")
                }
            }
        })
    }
}