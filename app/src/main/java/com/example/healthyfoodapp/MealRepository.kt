package com.example.healthyfoodapp.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MealRepository(private val apiService: MealApiService) {

    companion object {
        private val FORBIDDEN_WORDS = setOf("error", "test", "fail", "crash", "break")
        private const val MIN_QUERY_LENGTH = 3
    }

    suspend fun searchMeals(query: String): List<MealPreview> {
        validateQuery(query)
        return withContext(Dispatchers.IO) {
            apiService.searchMeals(query).meals ?: emptyList()
        }
    }

    suspend fun getMealDetails(id: String): MealDetail? {
        return withContext(Dispatchers.IO) {
            apiService.getMealById(id).meals?.firstOrNull()
        }
    }

    private fun validateQuery(query: String) {
        if (query.length < MIN_QUERY_LENGTH) {
            throw IllegalArgumentException("Запрос слишком короткий — минимум $MIN_QUERY_LENGTH символа")
        }

        val lowerQuery = query.lowercase()
        val foundWord = FORBIDDEN_WORDS.firstOrNull { lowerQuery.contains(it) }
        if (foundWord != null) {
            throw IllegalArgumentException("Недопустимое слово в запросе: \"$foundWord\"")
        }
    }
}