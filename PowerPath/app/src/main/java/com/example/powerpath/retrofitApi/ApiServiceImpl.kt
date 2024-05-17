package com.example.powerpath.retrofitApi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import com.example.powerpath.retrofitApi.dataClasses.FiltersRequest
import com.example.powerpath.retrofitApi.dataClasses.LoginRequest
import com.example.powerpath.retrofitApi.dataClasses.SignupRequest
import com.example.powerpath.retrofitApi.dataClasses.UserFilterRetrofit

class ApiServiceImpl {
    fun getFilters(email: String, onSuccess: (userFilter: UserFilterRetrofit) -> Unit, onFailure: (e: Exception) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://power-path-backend-3e6dc9fdeee0.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val call = service.getFilters(email)

        call.enqueue(object : Callback<UserFilterRetrofit> {
            override fun onFailure(call: Call<UserFilterRetrofit>, t: Throwable) {
                Log.d("===", "get filters error: ${t.message}")
                onFailure.invoke(Exception(t))
            }

            override fun onResponse(call: Call<UserFilterRetrofit>, response: Response<UserFilterRetrofit>) {
                if (response.isSuccessful) {
                    response.body()?.let { userFilter ->
                        onSuccess.invoke(userFilter)
                    } ?: run {
                        Log.d("===", "get filters error: response body is null")
                        onFailure.invoke(Exception("Response body is null"))
                    }
                } else {
                    Log.d("===", "get filters error: ${response.message()}")
                    onFailure.invoke(Exception("Response error: ${response.message()}"))
                }
            }
        })
    }

    fun saveFilters(
        email: String,
        powerRange: Pair<Int, Int>,
        connectorType: String,
        networks: List<String>,
        minRating: Int,
        minStationCount: Int,
        paid: Boolean,
        free: Boolean,
        durability: Int
    ) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://power-path-backend-3e6dc9fdeee0.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        val type = when (connectorType) {
            "CCS Combo Type 1" -> "CCS (Type 1)"
            "CCS Combo Type 2" -> "CCS (Type 2)"
            "CHAdeMO" -> "CHAdeMO"
            "GB/T" -> "GB-T DC - GB/T 20234"
            "Supercharger" -> "NACS / Tesla Supercharger"
            "Type 1 J1772" -> "Type 1 (J1772)"
            "Type 2 Mennekes" -> "Type 2"
            else -> ""
        }

        val filtersRequest = FiltersRequest(
            email = email,
            powerRange = listOf(powerRange.first, powerRange.second),
            connectorType = type,
            networks = networks,
            minimalRating = minRating,
            stationCount = minStationCount,
            paid = paid,
            free = free,
            durability = durability * 1000
        )

        val call = service.saveFilters(filtersRequest)
        call.enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("===", "save filters error: ${t.message}")
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (!response.isSuccessful) {
                    Log.d("===", "save filters error: ${response.message()}")
                } else {
                    Log.d("===", "save filters successful: ${response.message()}")
                }
            }
        })
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://power-path-backend-3e6dc9fdeee0.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val loginRequest = LoginRequest(email, password)

        val call = service.login(loginRequest)
        call.enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("===", "login error: ${t.message}")
                onError()
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("===", "login successful")
                    onSuccess()
                } else {
                    Log.d("===", "login error: ${response.message()}")
                    onError()
                }
            }
        })
    }

    fun signup(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://power-path-backend-3e6dc9fdeee0.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val signupRequest = SignupRequest(email, password)

        val call = service.signup(signupRequest)
        call.enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("===", "signup error: ${t.message}")
                onError()
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("===", "signup successful: ${response.message()}")
                    onSuccess()
                } else {
                    Log.d("===", "signup error: ${response.message()}")
                    onError()
                }
            }
        })
    }
}