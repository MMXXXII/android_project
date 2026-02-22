package com.example.healthyfoodapp.network

import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {

    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): MealSearchResponse

    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): MealDetailResponse
}