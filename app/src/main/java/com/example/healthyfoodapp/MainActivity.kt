package com.example.healthyfoodapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.result.contract.ActivityResultContracts
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private val dishListLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val selectedDishName = data?.getStringExtra("selected_dish_name") ?: ""
            val selectedDishType = data?.getStringExtra("selected_dish_type") ?: ""
            val selectedDishCalories = data?.getStringExtra("selected_dish_calories") ?: ""
            val selectedDishDescription = data?.getStringExtra("selected_dish_description") ?: ""

            if (selectedDishName.isNotEmpty()) {
                findViewById<EditText>(R.id.etName).setText(selectedDishName)
                findViewById<EditText>(R.id.etCalories).setText(selectedDishCalories)
                findViewById<EditText>(R.id.etDescription).setText(selectedDishDescription)

                // Выбираем тип в RadioGroup
                when (selectedDishType) {
                    "Завтрак" -> findViewById<RadioButton>(R.id.rbBreakfast).isChecked = true
                    "Обед" -> findViewById<RadioButton>(R.id.rbLunch).isChecked = true
                    "Ужин" -> findViewById<RadioButton>(R.id.rbDinner).isChecked = true
                }

                findViewById<TextView>(R.id.tvResult).text = "Блюдо выбрано: $selectedDishName"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("dish_prefs", Context.MODE_PRIVATE)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        val etName: EditText = findViewById(R.id.etName)
        val etCalories: EditText = findViewById(R.id.etCalories)
        val etDescription: EditText = findViewById(R.id.etDescription)
        val rgType: RadioGroup = findViewById(R.id.rgType)
        val btnSave: Button = findViewById(R.id.btnSave)
        val btnMenu: Button = findViewById(R.id.btnMenu)
        val tvResult: TextView = findViewById(R.id.tvResult)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_add_dish -> {
                    drawerLayout.closeDrawers()
                    // Остаемся на текущем экране (добавление блюда)
                    true
                }
                R.id.nav_my_catalog -> {
                    drawerLayout.closeDrawers()
                    val intent = Intent(this, DishListActivity::class.java)
                    dishListLauncher.launch(intent)
                    true
                }
                R.id.nav_exit -> {
                    finish()
                    true
                }
                else -> false
            }
        }

        // Обработчик для кнопки "Сохранить блюдо"
        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val calories = etCalories.text.toString()
            val description = etDescription.text.toString()
            val selectedId = rgType.checkedRadioButtonId

            if (name.isEmpty() || calories.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedType: RadioButton = findViewById(selectedId)
            val type = selectedType.text.toString()

            saveDish(name, type, calories, description)
            tvResult.text = "Блюдо сохранено!"

            // Очищаем поля
            etName.text.clear()
            etCalories.text.clear()
            etDescription.text.clear()
        }

        findViewById<Button>(R.id.btnGoToDishList).setOnClickListener {
            val intent = Intent(this, DishListActivity::class.java)
            dishListLauncher.launch(intent)
        }
    }

    private fun saveDish(name: String, type: String, calories: String, description: String) {
        val editor = sharedPreferences.edit()
        val dishString = "$name,$type,$calories,$description"
        val currentDishes = sharedPreferences.getString("dishes", "")
        val newDishes = if (currentDishes.isNullOrEmpty()) {
            dishString
        } else {
            "$currentDishes|$dishString"
        }
        editor.putString("dishes", newDishes)
        editor.apply()
    }
}
