package com.example.healthyfoodapp.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MealRepository(private val apiService: MealApiService) {

    suspend fun searchAndGetFirstMealDetails(query: String): MealDetail? {
        return withContext(Dispatchers.IO) {
            val searchResponse = apiService.searchMeals(query)
            val firstMeal = searchResponse.meals?.firstOrNull() ?: return@withContext null
            val detailResponse = apiService.getMealById(firstMeal.id)
            detailResponse.meals?.firstOrNull()
        }
    }

    suspend fun searchMeals(query: String): List<MealPreview> {
        return withContext(Dispatchers.IO) {
            apiService.searchMeals(query).meals ?: emptyList()
        }
    }

    suspend fun getMealDetails(id: String): MealDetail? {
        return withContext(Dispatchers.IO) {
            apiService.getMealById(id).meals?.firstOrNull()
        }
    }
}