package com.example.healthyfoodapp.presentation.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyfoodapp.R
import com.example.healthyfoodapp.domain.model.Dish

class DishAdapter(
    private val onDelete: (Dish) -> Unit,
    private val onEdit: (Dish) -> Unit
) : ListAdapter<Dish, DishAdapter.DishViewHolder>(DishDiffCallback()) {

    inner class DishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(dish: Dish) {
            itemView.findViewById<TextView>(R.id.tvDishName).text = dish.name
            itemView.findViewById<TextView>(R.id.tvDishType).text = "Тип: ${dish.type}"
            itemView.findViewById<TextView>(R.id.tvDishCalories).text = "Калории: ${dish.calories} ккал"
            itemView.findViewById<TextView>(R.id.tvDishDescription).text = "Описание: ${dish.description}"

            itemView.findViewById<Button>(R.id.btnDeleteDish).setOnClickListener {
                onDelete(dish)
            }

            itemView.setOnCreateContextMenuListener { menu, _, _ ->
                menu.add("Редактировать").setOnMenuItemClickListener {
                    onEdit(dish)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dish, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) = holder.bind(getItem(position))

    class DishDiffCallback : DiffUtil.ItemCallback<Dish>() {
        override fun areItemsTheSame(oldItem: Dish, newItem: Dish) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Dish, newItem: Dish) = oldItem == newItem
    }
}