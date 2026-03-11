package com.example.healthyfoodapp.data.repository

import com.example.healthyfoodapp.data.remote.MealApiService
import com.example.healthyfoodapp.domain.model.MealDetail
import com.example.healthyfoodapp.domain.model.MealPreview
import com.example.healthyfoodapp.domain.repository.MealRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MealRepositoryImpl(private val apiService: MealApiService) : MealRepository {

    override suspend fun searchMeals(query: String): List<MealPreview> =
        withContext(Dispatchers.IO) {
            apiService.searchMeals(query).meals ?: emptyList()
        }

    override suspend fun getMealDetails(id: String): MealDetail? =
        withContext(Dispatchers.IO) {
            apiService.getMealById(id).meals?.firstOrNull()
        }
}