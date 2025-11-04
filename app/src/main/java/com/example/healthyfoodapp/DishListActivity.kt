package com.example.healthyfoodapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.content.Context
import android.content.SharedPreferences
import android.content.Intent

data class Dish(val name: String, val type: String, val calories: String, val description: String)

class DishListActivity : AppCompatActivity() {
    private val dishes = mutableListOf<Dish>()
    private lateinit var adapter: DishAdapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dish_list)

        sharedPreferences = getSharedPreferences("dish_prefs", Context.MODE_PRIVATE)

        val recyclerView: RecyclerView = findViewById(R.id.rvDishList)
        val btnBack: Button = findViewById(R.id.btnBack)

        loadDishesFromPreferences()

        adapter = DishAdapter(dishes) { selectedDish ->
            val resultIntent = Intent()
            resultIntent.putExtra("selected_dish_name", selectedDish.name)
            resultIntent.putExtra("selected_dish_type", selectedDish.type)
            resultIntent.putExtra("selected_dish_calories", selectedDish.calories)
            resultIntent.putExtra("selected_dish_description", selectedDish.description)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun loadDishesFromPreferences() {
        val dishesString = sharedPreferences.getString("dishes", "")
        if (!dishesString.isNullOrEmpty()) {
            val dishList = dishesString.split("|")
            for (dishData in dishList) {
                val parts = dishData.split(",")
                if (parts.size == 4) {
                    dishes.add(Dish(parts[0], parts[1], parts[2], parts[3]))
                }
            }
        }
    }
}

class DishAdapter(
    private val dishes: List<Dish>,
    private val onClick: (Dish) -> Unit
) : RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    inner class DishViewHolder(private val itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        fun bind(dish: Dish) {
            val tvName = itemView.findViewById<TextView>(R.id.tvDishName)
            val tvType = itemView.findViewById<TextView>(R.id.tvDishType)
            val tvCalories = itemView.findViewById<TextView>(R.id.tvDishCalories)

            tvName.text = dish.name
            tvType.text = "Тип: ${dish.type}"
            tvCalories.text = "Калории: ${dish.calories} ккал"
            itemView.setOnClickListener { onClick(dish) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dish, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        holder.bind(dishes[position])
    }

    override fun getItemCount() = dishes.size
}
