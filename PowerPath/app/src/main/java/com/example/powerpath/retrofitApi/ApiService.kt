package com.example.powerpath.retrofitApi

import com.example.powerpath.retrofitApi.dataClasses.DeletePinRequest
import com.example.powerpath.retrofitApi.dataClasses.FiltersRequest
import com.example.powerpath.retrofitApi.dataClasses.LoginRequest
import com.example.powerpath.retrofitApi.dataClasses.PinRequest
import com.example.powerpath.retrofitApi.dataClasses.SignupRequest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.powerpath.retrofitApi.dataClasses.UserFilterRetrofit
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @GET("get_filters")
    fun getFilters(@Query("email") email: String): Call<UserFilterRetrofit>

    @POST("save_filters")
    fun saveFilters(@Body filters: FiltersRequest): Call<Void>

    @POST("login")
    fun login(@Body loginRequest: LoginRequest): Call<Void>

    @POST("signup")
    fun signup(@Body signupRequest: SignupRequest): Call<Void>

    @POST("save_pin")
    fun savePin(@Body pinRequest: PinRequest): Call<Void>

    @GET("get_pins")
    fun getPins(@Query("email") email: String): Call<String>

    @GET("closest_charging_station")
    fun getClosestStation(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("range") range: Int = 10,
        @Query("email") email: String
    ): Call<String>

    @GET("get_path")
    suspend fun getPath(
        @Query("start") start: String,
        @Query("destination") destination: String
    ): String

    @POST("delete_pin")
    fun deletePin(@Body deletePinRequest: DeletePinRequest): Call<Void>
}
