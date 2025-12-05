package com.example.healthyfoodapp

data class Dish(
    var id: Long = 0,
    var name: String,
    var type: String,
    var calories: String,
    var description: String,
    var categoryId: Long? = null,
    var migrationDate: String? = null
) : java.io.Serializable
