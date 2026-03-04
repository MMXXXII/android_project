package com.example.healthyfoodapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthyfoodapp.domain.usecase.*

class AddDishViewModelFactory(
    private val addDishUseCase: AddDishUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AddDishViewModel(addDishUseCase) as T
    }
}

class DishListViewModelFactory(
    private val getAllDishesUseCase: GetAllDishesUseCase,
    private val deleteDishUseCase: DeleteDishUseCase,
    private val updateDishUseCase: UpdateDishUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DishListViewModel(getAllDishesUseCase, deleteDishUseCase, updateDishUseCase) as T
    }
}

class DataProcessingViewModelFactory(
    private val getAllDishesUseCase: GetAllDishesUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DataProcessingViewModel(getAllDishesUseCase) as T
    }
}

class MealSearchViewModelFactory(
    private val searchMealsUseCase: SearchMealsUseCase,
    private val saveMealToCatalogUseCase: SaveMealToCatalogUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MealSearchViewModel(searchMealsUseCase, saveMealToCatalogUseCase) as T
    }
}