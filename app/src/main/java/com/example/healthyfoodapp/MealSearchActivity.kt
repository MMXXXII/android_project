package com.example.healthyfoodapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.healthyfoodapp.network.MealDetail
import com.example.healthyfoodapp.network.MealPreview
import com.example.healthyfoodapp.network.MealRepository
import com.example.healthyfoodapp.network.NetworkModule
import kotlinx.coroutines.*

class MealSearchActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnCancel: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvCounter: TextView

    private lateinit var repository: MealRepository
    private lateinit var dbHelper: DishDatabaseHelper
    private var searchJob: Job? = null

    private val previews = mutableListOf<MealPreview>()
    private var currentIndex = 0
    private var currentDetail: MealDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_search)

        etSearch = findViewById(R.id.etMealSearch)
        btnSearch = findViewById(R.id.btnMealSearch)
        btnCancel = findViewById(R.id.btnCancelSearch)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        progressBar = findViewById(R.id.progressBarSearch)
        tvStatus = findViewById(R.id.tvSearchStatus)
        tvResult = findViewById(R.id.tvSearchResult)
        tvCounter = findViewById(R.id.tvCounter)

        repository = MealRepository(NetworkModule.provideMealApiService(this))
        dbHelper = DishDatabaseHelper(this)
        dbHelper.open()

        btnCancel.isEnabled = false
        setNavigationVisible(false)

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(this, "Введите название блюда", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startSearch(query)
        }

        btnCancel.setOnClickListener {
            searchJob?.cancel()
            updateUI(searching = false)
            tvStatus.text = "Поиск отменён"
        }

        btnPrev.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                loadDetail(previews[currentIndex])
            }
        }

        btnNext.setOnClickListener {
            if (currentIndex < previews.size - 1) {
                currentIndex++
                loadDetail(previews[currentIndex])
            }
        }

        findViewById<Button>(R.id.btnSaveMealToDB).setOnClickListener {
            currentDetail?.let { saveToDB(it) }
        }

        findViewById<Button>(R.id.btnBackFromSearch).setOnClickListener { finish() }
    }

    private fun startSearch(query: String) {
        searchJob?.cancel()
        previews.clear()
        currentIndex = 0
        currentDetail = null
        tvResult.text = ""
        setNavigationVisible(false)
        updateUI(searching = true)
        tvStatus.text = "Запрос 1: Поиск \"$query\"..."

        searchJob = lifecycleScope.launch {
            try {
                val result = repository.searchMeals(query)

                if (result.isEmpty()) {
                    tvStatus.text = "Ничего не найдено по запросу \"$query\""
                    updateUI(searching = false)
                    return@launch
                }

                previews.addAll(result)
                tvStatus.text = "Запрос 2: Загрузка деталей..."
                loadDetail(previews[0])

            } catch (e: CancellationException) {
                tvStatus.text = "Поиск отменён"
                updateUI(searching = false)
            } catch (e: Exception) {
                tvStatus.text = "Ошибка: ${e.message}"
                updateUI(searching = false)
            }
        }
    }

    private fun loadDetail(preview: MealPreview) {
        searchJob?.cancel()
        updateUI(searching = true)
        tvStatus.text = "Запрос 2: Загрузка деталей \"${preview.name}\"..."

        searchJob = lifecycleScope.launch {
            try {
                val detail = repository.getMealDetails(preview.id)

                if (detail == null) {
                    tvStatus.text = "Не удалось получить детали"
                    updateUI(searching = false)
                    return@launch
                }

                currentDetail = detail
                tvResult.text = buildResultText(detail)
                tvStatus.text = "Найдено: ${previews.size} блюд"
                tvCounter.text = "${currentIndex + 1} / ${previews.size}"
                setNavigationVisible(true)
                updateNavigationButtons()
                updateUI(searching = false)

            } catch (e: CancellationException) {
                tvStatus.text = "Отменено"
                updateUI(searching = false)
            } catch (e: Exception) {
                tvStatus.text = "Ошибка: ${e.message}"
                updateUI(searching = false)
            }
        }
    }

    private fun buildResultText(detail: MealDetail): String {
        val ingredients = detail.getIngredientsList()
        return buildString {
            appendLine("Название: ${detail.name}")
            appendLine("Категория: ${detail.category}")
            appendLine()
            if (ingredients.isNotEmpty()) {
                appendLine("Ингредиенты:")
                ingredients.forEach { appendLine("  • $it") }
                appendLine()
            }
            appendLine("Инструкция:")
            append(detail.instructions)
        }
    }

    private fun saveToDB(detail: MealDetail) {
        val categoryId = dbHelper.getCategoryIdByName(detail.category)
            ?: dbHelper.addCategory(detail.category)
        val ingredients = detail.getIngredientsList().joinToString("; ")
        dbHelper.addDish(
            name = detail.name,
            type = detail.category,
            calories = "0",
            description = ingredients.ifBlank { detail.instructions.take(200) },
            categoryId = categoryId
        )
        Toast.makeText(this, "\"${detail.name}\" сохранено!", Toast.LENGTH_SHORT).show()
    }

    private fun updateUI(searching: Boolean) {
        progressBar.visibility = if (searching) View.VISIBLE else View.GONE
        btnSearch.isEnabled = !searching
        btnCancel.isEnabled = searching
    }

    private fun setNavigationVisible(visible: Boolean) {
        val v = if (visible) View.VISIBLE else View.GONE
        btnPrev.visibility = v
        btnNext.visibility = v
        tvCounter.visibility = v
        findViewById<Button>(R.id.btnSaveMealToDB).visibility = v
    }

    private fun updateNavigationButtons() {
        btnPrev.isEnabled = currentIndex > 0
        btnNext.isEnabled = currentIndex < previews.size - 1
    }
}