package com.example.healthyfoodapp.data.repository

import com.example.healthyfoodapp.data.remote.MealApiService
import com.example.healthyfoodapp.data.remote.MealDetailDto
import com.example.healthyfoodapp.data.remote.MealPreviewDto
import com.example.healthyfoodapp.domain.model.MealDetail
import com.example.healthyfoodapp.domain.model.MealPreview
import com.example.healthyfoodapp.domain.repository.MealRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MealRepositoryImpl(private val apiService: MealApiService) : MealRepository {

    override suspend fun searchMeals(query: String): List<MealPreview> =
        withContext(Dispatchers.IO) {
            apiService.searchMeals(query).meals?.map { it.toDomain() } ?: emptyList()
        }

    override suspend fun getMealDetails(id: String): MealDetail? =
        withContext(Dispatchers.IO) {
            apiService.getMealById(id).meals?.firstOrNull()?.toDomain()
        }

    private fun MealPreviewDto.toDomain() = MealPreview(id, name, category, thumbnail)

    private fun MealDetailDto.toDomain() = MealDetail(
        id = id, name = name, category = category,
        instructions = instructions, thumbnail = thumbnail,
        ingredients = getIngredientsList()
    )
}