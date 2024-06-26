package com.example.powerpath.retrofitApi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import com.example.powerpath.retrofitApi.dataClasses.DeletePinRequest
import com.example.powerpath.retrofitApi.dataClasses.FiltersRequest
import com.example.powerpath.retrofitApi.dataClasses.LoginRequest
import com.example.powerpath.retrofitApi.dataClasses.PinRequest
import com.example.powerpath.retrofitApi.dataClasses.SignupRequest
import com.example.powerpath.retrofitApi.dataClasses.UserFilterRetrofit
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Converter
import java.lang.reflect.Type


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

    fun savePin(email: String, name: String, latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://power-path-backend-3e6dc9fdeee0.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val pinRequest = PinRequest(
            email = email,
            name = name,
            latitude = String.format("%.6f", latitude).toDouble(),
            longitude = String.format("%.6f", longitude).toDouble()
        )

        val call = service.savePin(pinRequest)
        call.enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("===", "save pin error: ${t.message}")
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("===", "save pin successful: ${response.message()}")
                } else {
                    Log.d("===", "save pin error: ${response.message()}")
                }
            }
        })
    }

    class StringConverterFactory : Converter.Factory() {
        override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
            return if (type == String::class.java) {
                Converter<ResponseBody, String> { it.string() }
            } else {
                null
            }
        }
    }

    fun getPins(email: String, onSuccess: (String) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://power-path-backend-3e6dc9fdeee0.herokuapp.com/")
            .addConverterFactory(StringConverterFactory())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val call = service.getPins(email)

        call.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("===", "get pins error: ${t.message}")
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseData ->
                        onSuccess(responseData)
                    } ?: run {
                        Log.d("===", "get pins error: response body is null")
                    }
                } else {
                    Log.d("===", "get pins error: ${response.message()}")
                }
            }
        })
    }

    fun getClosestStation(location: LatLng, email: String, onSuccess: (String) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://power-path-backend-3e6dc9fdeee0.herokuapp.com/")
            .addConverterFactory(StringConverterFactory())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val call = service.getClosestStation(
            latitude = location.latitude,
            longitude = location.longitude,
            email = email
        )

        call.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("===", "closest station error: ${t.message}")
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseData ->
                        Log.d("===", "closest station success: $responseData")
                        onSuccess(responseData)
                    } ?: run {
                        Log.d("===", "closest station error: response body is null")
                    }
                } else {
                    Log.d("===", "closest station error: ${response.message()}")
                }
            }
        })
    }

    suspend fun getPath(start: String, destination: String): String? {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://power-path-backend-3e6dc9fdeee0.herokuapp.com/")
            .addConverterFactory(StringConverterFactory())
            .build()

        val service = retrofit.create(ApiService::class.java)

        return withContext(Dispatchers.IO) {
            try {
                val response = service.getPath(start, destination)
                Log.d("===", "getPath success")
                response
            } catch (e: Exception) {
                Log.d("===", "getPath error: $e")
                null
            }
        }
    }

    fun deletePin(email: String, name: String, latitude: Double, longitude: Double, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://power-path-backend-3e6dc9fdeee0.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        val pinRequest = DeletePinRequest(
            email = email,
            name = name,
            latitude = String.format("%.6f", latitude),
            longitude = String.format("%.6f", longitude)
        )

        val call = service.deletePin(pinRequest)

        call.enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("===", "Network error on delete pin: ${t.message}")
                onFailure("Network error: ${t.message}")
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("===", "Pin deleted successfully")
                    onSuccess()
                } else {
                    val message = response.errorBody()?.string() ?: "Unknown error"
                    Log.d("===", "Failed to delete pin: $message")
                }
            }
        })
    }
}