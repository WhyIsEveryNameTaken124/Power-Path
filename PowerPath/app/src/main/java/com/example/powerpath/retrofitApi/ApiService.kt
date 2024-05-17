package com.example.powerpath.retrofitApi

import com.example.powerpath.retrofitApi.dataClasses.FiltersRequest
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
}
