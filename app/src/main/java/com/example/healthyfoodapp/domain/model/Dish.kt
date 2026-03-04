package com.example.healthyfoodapp.domain.model

import java.io.Serializable

data class Dish(
    val id: Long = 0,
    val name: String,
    val type: String,
    val calories: String,
    val description: String,
    val categoryId: Long? = null
) : Serializable