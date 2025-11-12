package com.example.healthyfoodapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import android.content.Intent
import android.content.SharedPreferences
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("dish_prefs", Context.MODE_PRIVATE)

        sharedPreferences.edit().clear().apply()

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        val etName = findViewById<EditText>(R.id.etName)
        val etCalories = findViewById<EditText>(R.id.etCalories)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val rgType = findViewById<RadioGroup>(R.id.rgType)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnMenu = findViewById<Button>(R.id.btnMenu)
        val btnGoToDishList = findViewById<Button>(R.id.btnGoToDishList)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }
        //Navigation Drawer
        navigationView.setNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.nav_add_dish) {
                drawerLayout.closeDrawers()
                true
            } else if (item.itemId == R.id.nav_my_catalog) {
                drawerLayout.closeDrawers()
                startActivity(Intent(this, DishListActivity::class.java))
                true
            } else if (item.itemId == R.id.nav_exit) {
                finish()
                true
            } else {
                false
            }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val calories = etCalories.text.toString()
            val description = etDescription.text.toString()
            val selectedTypeId = rgType.checkedRadioButtonId

            if (name == "" || calories == "") {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rb = findViewById<RadioButton>(selectedTypeId)
            val type = rb.text.toString()

            saveDish(name, type, calories, description)

            tvResult.text = "Блюдо сохранено!"

            etName.setText("")
            etCalories.setText("")
            etDescription.setText("")
        }

        btnGoToDishList.setOnClickListener {
            startActivity(Intent(this, DishListActivity::class.java))
        }
    }
    //передача и получение данных
    private fun saveDish(name: String, type: String, calories: String, description: String) {
        val editor = sharedPreferences.edit()

        val dishString = "$name,$type,$calories,$description"
        val oldDishes = sharedPreferences.getString("dishes", "")

        val finalString = if (oldDishes.isNullOrEmpty()) {
            dishString
        } else {
            oldDishes + "|" + dishString
        }

        editor.putString("dishes", finalString)
        editor.apply()
    }
}
