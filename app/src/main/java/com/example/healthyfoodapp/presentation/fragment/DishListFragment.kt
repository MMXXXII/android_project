package com.example.healthyfoodapp.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.healthyfoodapp.R
import com.example.healthyfoodapp.databinding.FragmentDishListBinding
import com.example.healthyfoodapp.domain.model.Dish
import com.example.healthyfoodapp.presentation.viewmodel.DishListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DishListFragment : Fragment() {

    private val viewModel: DishListViewModel by viewModels()

    private var _binding: FragmentDishListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DishAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DishAdapter(
            onDelete = { dish -> viewModel.deleteDish(dish.id) },
            onEdit = { dish -> showEditDialog(dish) }
        )

        binding.rvDishList.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@DishListFragment.adapter
        }

        viewModel.dishes.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.loadDishes()
    }

    private fun showEditDialog(dish: Dish) {
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
                viewModel.updateDish(
                    dish.copy(
                        name = etName.text.toString(),
                        calories = etCalories.text.toString(),
                        description = etDescription.text.toString()
                    )
                )
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}