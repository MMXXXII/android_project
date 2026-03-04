package com.example.healthyfoodapp.domain.model

data class MealPreview(
    val id: String,
    val name: String,
    val category: String,
    val thumbnail: String
)

data class MealDetail(
    val id: String,
    val name: String,
    val category: String,
    val instructions: String,
    val thumbnail: String,
    val ingredients: List<String>
)