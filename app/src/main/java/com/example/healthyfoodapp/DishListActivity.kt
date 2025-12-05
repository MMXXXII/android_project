package com.example.healthyfoodapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DishListActivity : AppCompatActivity() {

    private val dishes = mutableListOf<Dish>()
    private lateinit var dbHelper: DishDatabaseHelper
    private lateinit var adapter: DishAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dish_list)

        // Инициализация dbHelper и открытие базы данных
        dbHelper = DishDatabaseHelper(this)
        dbHelper.open()

        val recyclerView = findViewById<RecyclerView>(R.id.rvDishList)
        val btnBack = findViewById<Button>(R.id.btnBack)

        loadDishesFromDatabase()

        adapter = DishAdapter(dishes, dbHelper)  // Передайте dbHelper
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter


        btnBack.setOnClickListener { finish() }
    }

    private fun loadDishesFromDatabase() {
        dishes.clear()
        dishes.addAll(dbHelper.getAllDishes())
    }

    // Редактирование блюда
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
                dbHelper.updateDish(dish.id, dish.name, dish.type, dish.calories, dish.description, dish.categoryId)

                adapter.notifyItemChanged(position)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // Адаптер для отображения блюд в RecyclerView
    class DishAdapter(
        private val dishes: MutableList<Dish>,
        private val dbHelper: DishDatabaseHelper  // Добавлен параметр
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
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        dbHelper.deleteDish(dishes[pos].id)
                        dishes.removeAt(pos)
                        notifyItemRemoved(pos)
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