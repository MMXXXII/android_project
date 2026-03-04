package com.example.healthyfoodapp.domain.repository

import com.example.healthyfoodapp.domain.model.Dish
import com.example.healthyfoodapp.domain.model.MealDetail
import com.example.healthyfoodapp.domain.model.MealPreview

interface DishRepository {
    fun getAllDishes(): List<Dish>
    fun addDish(dish: Dish): Long
    fun updateDish(dish: Dish): Int
    fun deleteDish(id: Long): Int
    fun getOrCreateCategoryId(name: String): Long
}

interface MealRepository {
    suspend fun searchMeals(query: String): List<MealPreview>
    suspend fun getMealDetails(id: String): MealDetail?
}