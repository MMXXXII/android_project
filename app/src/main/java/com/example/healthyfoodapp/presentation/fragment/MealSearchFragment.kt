package com.example.healthyfoodapp.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.healthyfoodapp.R
import com.example.healthyfoodapp.data.remote.NetworkModule
import com.example.healthyfoodapp.data.repository.DishRepositoryImpl
import com.example.healthyfoodapp.data.repository.MealRepositoryImpl
import com.example.healthyfoodapp.domain.model.MealDetail
import com.example.healthyfoodapp.domain.usecase.SaveMealToCatalogUseCase
import com.example.healthyfoodapp.domain.usecase.SearchMealsUseCase
import com.example.healthyfoodapp.presentation.viewmodel.MealSearchViewModel
import com.example.healthyfoodapp.presentation.viewmodel.MealSearchViewModelFactory

class MealSearchFragment : Fragment() {

    private lateinit var viewModel: MealSearchViewModel

    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnCancel: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvCounter: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_meal_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mealRepository = MealRepositoryImpl(NetworkModule.provideMealApiService(requireContext()))
        val dishRepository = DishRepositoryImpl(requireContext())

        viewModel = ViewModelProvider(this, MealSearchViewModelFactory(
            SearchMealsUseCase(mealRepository),
            SaveMealToCatalogUseCase(dishRepository)
        ))[MealSearchViewModel::class.java]

        etSearch = view.findViewById(R.id.etMealSearch)
        btnSearch = view.findViewById(R.id.btnMealSearch)
        btnCancel = view.findViewById(R.id.btnCancelSearch)
        btnPrev = view.findViewById(R.id.btnPrev)
        btnNext = view.findViewById(R.id.btnNext)
        btnSave = view.findViewById(R.id.btnSaveMealToDB)
        progressBar = view.findViewById(R.id.progressBarSearch)
        tvStatus = view.findViewById(R.id.tvSearchStatus)
        tvResult = view.findViewById(R.id.tvSearchResult)
        tvCounter = view.findViewById(R.id.tvCounter)

        setNavigationVisible(false)
        btnCancel.isEnabled = false

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(requireContext(), "Введите название блюда", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.search(query)
        }
        btnCancel.setOnClickListener { viewModel.cancelSearch() }
        btnPrev.setOnClickListener { viewModel.navigatePrev() }
        btnNext.setOnClickListener { viewModel.navigateNext() }
        btnSave.setOnClickListener { viewModel.saveCurrentMeal() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MealSearchViewModel.State.Idle -> {
                    showLoading(false); setNavigationVisible(false); btnCancel.isEnabled = false
                }
                is MealSearchViewModel.State.Loading -> {
                    showLoading(true); setNavigationVisible(false)
                    btnCancel.isEnabled = true; tvStatus.text = "Загрузка..."
                }
                is MealSearchViewModel.State.Results -> {
                    showLoading(false); btnCancel.isEnabled = false
                    setNavigationVisible(true)
                    tvStatus.text = "Найдено: ${state.previews.size} блюд"
                    tvCounter.text = "${state.currentIndex + 1} / ${state.previews.size}"
                    tvResult.text = state.detail?.let { buildResultText(it) } ?: ""
                    btnPrev.isEnabled = state.currentIndex > 0
                    btnNext.isEnabled = state.currentIndex < state.previews.size - 1
                }
                is MealSearchViewModel.State.Error -> {
                    showLoading(false); setNavigationVisible(false)
                    btnCancel.isEnabled = false; tvStatus.text = state.message
                }
                is MealSearchViewModel.State.Cancelled -> {
                    showLoading(false); btnCancel.isEnabled = false
                    tvStatus.text = "Поиск отменён"
                }
                is MealSearchViewModel.State.Saved -> {
                    Toast.makeText(requireContext(), "Блюдо сохранено!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun buildResultText(detail: MealDetail): String = buildString {
        appendLine("Название: ${detail.name}")
        appendLine("Категория: ${detail.category}")
        appendLine()
        if (detail.ingredients.isNotEmpty()) {
            appendLine("Ингредиенты:")
            detail.ingredients.forEach { appendLine("  • $it") }
            appendLine()
        }
        appendLine("Инструкция:")
        append(detail.instructions)
    }

    private fun showLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnSearch.isEnabled = !loading
    }

    private fun setNavigationVisible(visible: Boolean) {
        val v = if (visible) View.VISIBLE else View.GONE
        btnPrev.visibility = v; btnNext.visibility = v
        tvCounter.visibility = v; btnSave.visibility = v
    }
}