package com.example.healthyfoodapp.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.healthyfoodapp.databinding.FragmentMealSearchBinding
import com.example.healthyfoodapp.domain.model.MealDetail
import com.example.healthyfoodapp.presentation.viewmodel.MealSearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MealSearchFragment : Fragment() {

    private val viewModel: MealSearchViewModel by viewModels()

    private var _binding: FragmentMealSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setNavigationVisible(false)
        binding.btnCancelSearch.isEnabled = false

        binding.btnMealSearch.setOnClickListener {
            val query = binding.etMealSearch.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(requireContext(), "Введите название блюда", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.search(query)
        }

        binding.btnCancelSearch.setOnClickListener { viewModel.cancelSearch() }
        binding.btnPrev.setOnClickListener { viewModel.navigatePrev() }
        binding.btnNext.setOnClickListener { viewModel.navigateNext() }
        binding.btnSaveMealToDB.setOnClickListener { viewModel.saveCurrentMeal() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MealSearchViewModel.State.Idle -> {
                    showLoading(false)
                    setNavigationVisible(false)
                    binding.btnCancelSearch.isEnabled = false
                }
                is MealSearchViewModel.State.Loading -> {
                    showLoading(true)
                    setNavigationVisible(false)
                    binding.btnCancelSearch.isEnabled = true
                    binding.tvSearchStatus.text = "Загрузка..."
                }
                is MealSearchViewModel.State.Results -> {
                    showLoading(false)
                    binding.btnCancelSearch.isEnabled = false
                    setNavigationVisible(true)
                    binding.tvSearchStatus.text = "Найдено: ${state.previews.size} блюд"
                    binding.tvCounter.text = "${state.currentIndex + 1} / ${state.previews.size}"
                    binding.tvSearchResult.text = state.detail?.let { buildResultText(it) } ?: ""
                    binding.btnPrev.isEnabled = state.currentIndex > 0
                    binding.btnNext.isEnabled = state.currentIndex < state.previews.size - 1
                }
                is MealSearchViewModel.State.Error -> {
                    showLoading(false)
                    setNavigationVisible(false)
                    binding.btnCancelSearch.isEnabled = false
                    binding.tvSearchStatus.text = state.message
                }
                is MealSearchViewModel.State.Cancelled -> {
                    showLoading(false)
                    binding.btnCancelSearch.isEnabled = false
                    binding.tvSearchStatus.text = "Поиск отменён"
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
        binding.progressBarSearch.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnMealSearch.isEnabled = !loading
    }

    private fun setNavigationVisible(visible: Boolean) {
        val v = if (visible) View.VISIBLE else View.GONE
        binding.btnPrev.visibility = v
        binding.btnNext.visibility = v
        binding.tvCounter.visibility = v
        binding.btnSaveMealToDB.visibility = v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}