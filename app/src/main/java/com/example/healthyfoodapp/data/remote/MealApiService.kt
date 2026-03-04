package com.example.healthyfoodapp.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class MealSearchResponse(
    @SerializedName("meals") val meals: List<MealPreviewDto>?
)

data class MealPreviewDto(
    @SerializedName("idMeal") val id: String,
    @SerializedName("strMeal") val name: String,
    @SerializedName("strCategory") val category: String,
    @SerializedName("strMealThumb") val thumbnail: String
)

data class MealDetailResponse(
    @SerializedName("meals") val meals: List<MealDetailDto>?
)

data class MealDetailDto(
    @SerializedName("idMeal") val id: String,
    @SerializedName("strMeal") val name: String,
    @SerializedName("strCategory") val category: String,
    @SerializedName("strInstructions") val instructions: String,
    @SerializedName("strMealThumb") val thumbnail: String,
    @SerializedName("strIngredient1") val ingredient1: String?,
    @SerializedName("strIngredient2") val ingredient2: String?,
    @SerializedName("strIngredient3") val ingredient3: String?,
    @SerializedName("strIngredient4") val ingredient4: String?,
    @SerializedName("strIngredient5") val ingredient5: String?,
    @SerializedName("strMeasure1") val measure1: String?,
    @SerializedName("strMeasure2") val measure2: String?,
    @SerializedName("strMeasure3") val measure3: String?,
    @SerializedName("strMeasure4") val measure4: String?,
    @SerializedName("strMeasure5") val measure5: String?
) {
    fun getIngredientsList(): List<String> = listOfNotNull(
        ingredient1?.takeIf { it.isNotBlank() }?.let { "$it - ${measure1.orEmpty().trim()}" },
        ingredient2?.takeIf { it.isNotBlank() }?.let { "$it - ${measure2.orEmpty().trim()}" },
        ingredient3?.takeIf { it.isNotBlank() }?.let { "$it - ${measure3.orEmpty().trim()}" },
        ingredient4?.takeIf { it.isNotBlank() }?.let { "$it - ${measure4.orEmpty().trim()}" },
        ingredient5?.takeIf { it.isNotBlank() }?.let { "$it - ${measure5.orEmpty().trim()}" }
    )
}

interface MealApiService {
    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): MealSearchResponse

    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): MealDetailResponse
}