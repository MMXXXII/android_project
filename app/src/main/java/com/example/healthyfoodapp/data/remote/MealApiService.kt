package com.example.healthyfoodapp.data.remote

import com.example.healthyfoodapp.domain.model.MealDetailResponse
import com.example.healthyfoodapp.domain.model.MealSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {
    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): MealSearchResponse

    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): MealDetailResponse
}