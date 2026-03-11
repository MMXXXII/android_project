package com.example.healthyfoodapp.domain.usecase

import com.example.healthyfoodapp.domain.model.Dish
import com.example.healthyfoodapp.domain.model.MealDetail
import com.example.healthyfoodapp.domain.model.MealPreview
import com.example.healthyfoodapp.domain.repository.DishRepository
import com.example.healthyfoodapp.domain.repository.MealRepository
import javax.inject.Inject

class GetAllDishesUseCase @Inject constructor(private val repository: DishRepository) {
    operator fun invoke(): List<Dish> = repository.getAllDishes()
}

class AddDishUseCase @Inject constructor(private val repository: DishRepository) {
    operator fun invoke(name: String, type: String, calories: String, description: String): Long {
        require(name.isNotBlank()) { "Заполните все поля" }
        require(calories.isNotBlank()) { "Заполните все поля" }
        val categoryId = repository.getOrCreateCategoryId(type)
        return repository.addDish(
            Dish(
                name = name,
                type = type,
                calories = calories,
                description = description,
                categoryId = categoryId
            )
        )
    }
}

class UpdateDishUseCase @Inject constructor(private val repository: DishRepository) {
    operator fun invoke(dish: Dish): Int = repository.updateDish(dish)
}

class DeleteDishUseCase @Inject constructor(private val repository: DishRepository) {
    operator fun invoke(id: Long): Int = repository.deleteDish(id)
}

class SearchMealsUseCase @Inject constructor(private val repository: MealRepository) {

    companion object {
        private val FORBIDDEN_WORDS = setOf("error", "test", "fail", "crash", "break")
        private const val MIN_QUERY_LENGTH = 3
    }

    suspend fun search(query: String): List<MealPreview> {
        if (query.length < MIN_QUERY_LENGTH)
            throw IllegalArgumentException("Запрос слишком короткий — минимум $MIN_QUERY_LENGTH символа")
        val lower = query.lowercase()
        FORBIDDEN_WORDS.firstOrNull { lower.contains(it) }?.let {
            throw IllegalArgumentException("Недопустимое слово в запросе: \"$it\"")
        }
        return repository.searchMeals(query)
    }

    suspend fun getDetails(id: String): MealDetail? = repository.getMealDetails(id)
}

class SaveMealToCatalogUseCase @Inject constructor(private val repository: DishRepository) {
    operator fun invoke(detail: MealDetail): Long {
        val categoryId = repository.getOrCreateCategoryId(detail.category)
        val description = detail.ingredients.joinToString("; ")
            .ifBlank { detail.instructions.take(200) }
        return repository.addDish(
            Dish(
                name = detail.name,
                type = detail.category,
                calories = "0",
                description = description,
                categoryId = categoryId
            )
        )
    }
}