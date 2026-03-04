package com.example.healthyfoodapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthyfoodapp.domain.model.Dish
import com.example.healthyfoodapp.domain.usecase.*
import kotlinx.coroutines.*
import kotlin.random.Random

class AddDishViewModel(private val addDishUseCase: AddDishUseCase) : ViewModel() {

    private val _saveResult = MutableLiveData<String>()
    val saveResult: LiveData<String> = _saveResult

    fun saveDish(name: String, type: String, calories: String, description: String) {
        try {
            addDishUseCase(name, type, calories, description)
            _saveResult.value = "Блюдо сохранено!"
        } catch (e: IllegalArgumentException) {
            _saveResult.value = "Ошибка: ${e.message}"
        }
    }
}

class DishListViewModel(
    private val getAllDishesUseCase: GetAllDishesUseCase,
    private val deleteDishUseCase: DeleteDishUseCase,
    private val updateDishUseCase: UpdateDishUseCase
) : ViewModel() {

    private val _dishes = MutableLiveData<List<Dish>>()
    val dishes: LiveData<List<Dish>> = _dishes

    fun loadDishes() {
        _dishes.value = getAllDishesUseCase()
    }

    fun deleteDish(id: Long) {
        deleteDishUseCase(id)
        loadDishes()
    }

    fun updateDish(dish: Dish) {
        updateDishUseCase(dish)
        loadDishes()
    }
}

class DataProcessingViewModel(
    private val getAllDishesUseCase: GetAllDishesUseCase
) : ViewModel() {

    private val _threadStatus = MutableLiveData<String>()
    val threadStatus: LiveData<String> = _threadStatus

    private val _coroutineStatus = MutableLiveData<String>()
    val coroutineStatus: LiveData<String> = _coroutineStatus

    private val _coroutineCancelEnabled = MutableLiveData(false)
    val coroutineCancelEnabled: LiveData<Boolean> = _coroutineCancelEnabled

    private var workerThread: Thread? = null
    private var dataProcessingJob: Job? = null

    fun startThreadProcessing() {
        workerThread?.interrupt()
        workerThread = Thread {
            try {
                val dishes = threadPhase1()
                threadPhase2(dishes)
            } catch (e: InterruptedException) {
                postThreadStatus("Поток отменен")
            } catch (e: Exception) {
                postThreadStatus("Ошибка: ${e.message}")
            }
        }
        workerThread?.start()
    }

    fun cancelThreadProcessing() {
        workerThread?.interrupt()
        postThreadStatus("Отмена потока...")
    }

    private fun threadPhase1(): List<Dish> {
        postThreadStatus("Поток 1: Подсчет калорий...")
        Thread.sleep(2000)
        val dishes = getAllDishesUseCase()
        var totalCalories = 0
        for (dish in dishes) {
            if (Thread.currentThread().isInterrupted) throw InterruptedException()
            totalCalories += dish.calories.toIntOrNull() ?: 0
            Thread.sleep(100)
        }
        postThreadStatus("Поток 1 завершен. Калорий: $totalCalories")
        Thread.sleep(1000)
        return dishes
    }

    private fun threadPhase2(dishes: List<Dish>) {
        postThreadStatus("Поток 2: Расчет средней...")
        Thread.sleep(2000)
        val totalCalories = dishes.sumOf { it.calories.toIntOrNull() ?: 0 }
        val average = if (dishes.isNotEmpty()) totalCalories / dishes.size else 0
        postThreadStatus("Готово! Средняя калорийность: $average ккал")
    }

    private fun postThreadStatus(message: String) {
        _threadStatus.postValue("Статус потока: $message")
    }

    fun startCoroutineProcessing() {
        dataProcessingJob?.cancel()
        _coroutineCancelEnabled.value = true

        dataProcessingJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                withContext(Dispatchers.Main) {
                    _coroutineStatus.value = "Статус корутины: Корутина 1: Загрузка данных..."
                }
                if (Random.nextInt(100) < 10) {
                    throw RuntimeException("Случайная ошибка загрузки данных")
                }
                val dishes = withContext(Dispatchers.IO) {
                    delay(2000)
                    getAllDishesUseCase()
                }
                withContext(Dispatchers.Main) {
                    _coroutineStatus.value = "Статус корутины: Корутина 1 завершена. Блюд: ${dishes.size}"
                }
                delay(500)
                coroutinePhase2(dishes)
            } catch (e: CancellationException) {
                withContext(Dispatchers.Main) {
                    _coroutineStatus.value = "Статус корутины: Корутина 1 отменена"
                    _coroutineCancelEnabled.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _coroutineStatus.value = "Статус корутины: Ошибка: ${e.message}"
                    _coroutineCancelEnabled.value = false
                }
            }
        }
    }

    fun cancelCoroutineProcessing() {
        dataProcessingJob?.cancel()
        _coroutineStatus.value = "Статус корутины: Отмена корутины..."
        _coroutineCancelEnabled.value = false
    }

    private suspend fun coroutinePhase2(dishes: List<Dish>) {
        dataProcessingJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                withContext(Dispatchers.Main) {
                    _coroutineStatus.value = "Статус корутины: Корутина 2: Подсчет калорий..."
                }
                var totalCalories = 0
                var maxCalories = 0
                var maxCalorieDish = ""
                for (dish in dishes) {
                    ensureActive()
                    val calories = dish.calories.toIntOrNull() ?: 0
                    totalCalories += calories
                    if (calories > maxCalories) {
                        maxCalories = calories
                        maxCalorieDish = dish.name
                    }
                    delay(100)
                }
                val average = if (dishes.isNotEmpty()) totalCalories / dishes.size else 0
                withContext(Dispatchers.Main) {
                    _coroutineStatus.value =
                        "Статус корутины: Готово! Средняя: $average ккал, Макс: $maxCalories ($maxCalorieDish)"
                    _coroutineCancelEnabled.value = false
                }
            } catch (e: CancellationException) {
                withContext(Dispatchers.Main) {
                    _coroutineStatus.value = "Статус корутины: Корутина 2 отменена"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _coroutineStatus.value = "Статус корутины: Ошибка: ${e.message}"
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        workerThread?.interrupt()
    }
}

class MealSearchViewModel(
    private val searchMealsUseCase: SearchMealsUseCase,
    private val saveMealToCatalogUseCase: SaveMealToCatalogUseCase
) : ViewModel() {

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Results(
            val previews: List<com.example.healthyfoodapp.domain.model.MealPreview>,
            val currentIndex: Int,
            val detail: com.example.healthyfoodapp.domain.model.MealDetail?
        ) : State()
        data class Error(val message: String) : State()
        object Cancelled : State()
        object Saved : State()
    }

    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state

    private var searchJob: Job? = null
    private val previews = mutableListOf<com.example.healthyfoodapp.domain.model.MealPreview>()
    private var currentIndex = 0

    fun search(query: String) {
        searchJob?.cancel()
        previews.clear()
        currentIndex = 0
        _state.value = State.Loading

        searchJob = viewModelScope.launch {
            try {
                val results = searchMealsUseCase.search(query)
                if (results.isEmpty()) {
                    _state.value = State.Error("Ничего не найдено по запросу \"$query\"")
                    return@launch
                }
                previews.addAll(results)
                loadDetail(0)
            } catch (e: CancellationException) {
                _state.value = State.Cancelled
            } catch (e: Exception) {
                _state.value = State.Error(e.message ?: "Ошибка поиска")
            }
        }
    }

    fun navigatePrev() { if (currentIndex > 0) loadDetail(currentIndex - 1) }
    fun navigateNext() { if (currentIndex < previews.size - 1) loadDetail(currentIndex + 1) }

    fun cancelSearch() {
        searchJob?.cancel()
        _state.value = State.Cancelled
    }

    fun saveCurrentMeal() {
        val detail = (_state.value as? State.Results)?.detail ?: return
        viewModelScope.launch {
            saveMealToCatalogUseCase(detail)
            _state.value = State.Saved
        }
    }

    private fun loadDetail(index: Int) {
        searchJob?.cancel()
        currentIndex = index
        _state.value = State.Loading

        searchJob = viewModelScope.launch {
            try {
                val detail = searchMealsUseCase.getDetails(previews[index].id)
                _state.value = State.Results(
                    previews = previews.toList(),
                    currentIndex = currentIndex,
                    detail = detail
                )
            } catch (e: CancellationException) {
                _state.value = State.Cancelled
            } catch (e: Exception) {
                _state.value = State.Error(e.message ?: "Ошибка загрузки деталей")
            }
        }
    }
}