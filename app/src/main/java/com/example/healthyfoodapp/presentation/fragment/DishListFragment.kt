package com.example.healthyfoodapp.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyfoodapp.R
import com.example.healthyfoodapp.data.repository.DishRepositoryImpl
import com.example.healthyfoodapp.domain.usecase.DeleteDishUseCase
import com.example.healthyfoodapp.domain.usecase.GetAllDishesUseCase
import com.example.healthyfoodapp.domain.usecase.UpdateDishUseCase
import com.example.healthyfoodapp.presentation.viewmodel.DishListViewModel
import com.example.healthyfoodapp.presentation.viewmodel.DishListViewModelFactory

class DishListFragment : Fragment() {

    private lateinit var viewModel: DishListViewModel
    private lateinit var adapter: DishAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_dish_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = DishRepositoryImpl(requireContext())
        viewModel = ViewModelProvider(this, DishListViewModelFactory(
            GetAllDishesUseCase(repository),
            DeleteDishUseCase(repository),
            UpdateDishUseCase(repository)
        ))[DishListViewModel::class.java]

        adapter = DishAdapter(
            onDelete = { dish -> viewModel.deleteDish(dish.id) },
            onEdit = { dish -> showEditDialog(dish) }
        )

        view.findViewById<RecyclerView>(R.id.rvDishList).apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@DishListFragment.adapter
        }

        viewModel.dishes.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.loadDishes()
    }

    private fun showEditDialog(dish: com.example.healthyfoodapp.domain.model.Dish) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_dish, null)
        val etName = dialogView.findViewById<EditText>(R.id.etEditName)
        val etCalories = dialogView.findViewById<EditText>(R.id.etEditCalories)
        val etDescription = dialogView.findViewById<EditText>(R.id.etEditDescription)

        etName.setText(dish.name)
        etCalories.setText(dish.calories)
        etDescription.setText(dish.description)

        AlertDialog.Builder(requireContext())
            .setTitle("Редактировать блюдо")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                viewModel.updateDish(dish.copy(
                    name = etName.text.toString(),
                    calories = etCalories.text.toString(),
                    description = etDescription.text.toString()
                ))
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}