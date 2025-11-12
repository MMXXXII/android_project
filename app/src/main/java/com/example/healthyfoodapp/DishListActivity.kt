package com.example.healthyfoodapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

data class Dish(val name: String, val type: String, val calories: String, val description: String)

class DishListActivity : AppCompatActivity() {

    private val dishes = mutableListOf<Dish>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: DishAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dish_list)

        sharedPreferences = getSharedPreferences("dish_prefs", Context.MODE_PRIVATE)

        val recyclerView = findViewById<RecyclerView>(R.id.rvDishList)
        val btnBack = findViewById<Button>(R.id.btnBack)

        loadDishesFromPreferences()

        adapter = DishAdapter(dishes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadDishesFromPreferences() {
        val saved = sharedPreferences.getString("dishes", "")

        if (!saved.isNullOrEmpty()) {
            val items = saved.split("|")
            for (dish in items) {
                val parts = dish.split(",")
                if (parts.size == 4) {
                    dishes.add(Dish(parts[0], parts[1], parts[2], parts[3]))
                }
            }
        }
    }
}


class DishAdapter(
    private val dishes: List<Dish>
) : RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    inner class DishViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        fun bind(dish: Dish) {
            val tvName = itemView.findViewById<TextView>(R.id.tvDishName)
            val tvType = itemView.findViewById<TextView>(R.id.tvDishType)
            val tvCalories = itemView.findViewById<TextView>(R.id.tvDishCalories)
            val tvDescription = itemView.findViewById<TextView>(R.id.tvDishDescription)

            tvName.text = dish.name
            tvType.text = "Тип: ${dish.type}"
            tvCalories.text = "Калории: ${dish.calories} ккал"
            tvDescription.text = "Описание: ${dish.description}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dish, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        holder.bind(dishes[position])
    }

    override fun getItemCount(): Int {
        return dishes.size
    }
}

