package com.example.healthyfoodapp

data class Dish(
    var id: Long = 0,  // Добавьте это поле
    var name: String,
    var type: String,
    var calories: String,
    var description: String
) : java.io.Serializable
