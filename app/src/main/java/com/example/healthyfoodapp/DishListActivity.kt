package com.example.healthyfoodapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class Dish(
    var name: String,
    var type: String,
    var calories: String,
    var description: String
) : java.io.Serializable

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

        adapter = DishAdapter(dishes, sharedPreferences)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
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

    // ============================================================
    //   EDIT DISH
    // ============================================================
    fun editDish(position: Int) {
        val dish = dishes[position]
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_dish, null)

        val etName = dialogView.findViewById<EditText>(R.id.etEditName)
        val etCalories = dialogView.findViewById<EditText>(R.id.etEditCalories)
        val etDescription = dialogView.findViewById<EditText>(R.id.etEditDescription)

        etName.setText(dish.name)
        etCalories.setText(dish.calories)
        etDescription.setText(dish.description)

        AlertDialog.Builder(this)
            .setTitle("Редактировать блюдо")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                dish.name = etName.text.toString()
                dish.calories = etCalories.text.toString()
                dish.description = etDescription.text.toString()

                adapter.notifyItemChanged(position)
                adapter.saveDishesToPreferences()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // ============================================================
    //   ADAPTER
    // ============================================================
    class DishAdapter(
        private val dishes: MutableList<Dish>,
        private val sharedPreferences: SharedPreferences
    ) : RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

        inner class DishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bind(dish: Dish) {
                val tvName = itemView.findViewById<TextView>(R.id.tvDishName)
                val tvType = itemView.findViewById<TextView>(R.id.tvDishType)
                val tvCalories = itemView.findViewById<TextView>(R.id.tvDishCalories)
                val tvDescription = itemView.findViewById<TextView>(R.id.tvDishDescription)
                val btnDelete = itemView.findViewById<Button>(R.id.btnDeleteDish)

                tvName.text = dish.name
                tvType.text = "Тип: ${dish.type}"
                tvCalories.text = "Калории: ${dish.calories} ккал"
                tvDescription.text = "Описание: ${dish.description}"

                btnDelete.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        dishes.removeAt(position)
                        notifyItemRemoved(position)
                        saveDishesToPreferences()
                    }
                }

                itemView.setOnCreateContextMenuListener { menu, _, _ ->
                    menu.add("Редактировать").setOnMenuItemClickListener {
                        (itemView.context as DishListActivity).editDish(adapterPosition)
                        true
                    }
                }
            }
        }

        fun saveDishesToPreferences() {
            val editor = sharedPreferences.edit()
            val dishString = dishes.joinToString("|") {
                "${it.name},${it.type},${it.calories},${it.description}"
            }
            editor.putString("dishes", dishString)
            editor.apply()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_dish, parent, false)
            return DishViewHolder(view)
        }

        override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
            holder.bind(dishes[position])
        }

        override fun getItemCount(): Int = dishes.size
    }
}
