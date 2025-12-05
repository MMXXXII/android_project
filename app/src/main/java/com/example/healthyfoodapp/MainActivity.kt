package com.example.healthyfoodapp

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.io.*

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DishDatabaseHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DishDatabaseHelper(this)
        dbHelper.open()

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        val etName = findViewById<EditText>(R.id.etName)
        val etCalories = findViewById<EditText>(R.id.etCalories)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val rgCategory = findViewById<RadioGroup>(R.id.rgCategory)  // ✅ Правильный ID
        val tvResult = findViewById<TextView>(R.id.tvResult)

        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnMenu = findViewById<Button>(R.id.btnMenu)
        val btnGoToDishList = findViewById<Button>(R.id.btnGoToDishList)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add_dish -> { drawerLayout.closeDrawers(); true }
                R.id.nav_my_catalog -> {
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this, DishListActivity::class.java))
                    true
                }
                R.id.nav_save_csv -> { saveCSV(); true }
                R.id.nav_load_csv -> { loadCSV(); true }
                R.id.nav_save_bin -> { saveBinary(); true }
                R.id.nav_load_bin -> { loadBinary(); true }
                R.id.nav_save_media -> { checkPermissionAndSaveMedia(); true }
                R.id.nav_exit -> { finish(); true }
                else -> false
            }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val calories = etCalories.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (name.isEmpty() || calories.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedCategoryId = rgCategory.checkedRadioButtonId
            val rb = findViewById<RadioButton>(selectedCategoryId)
            val categoryName = rb.text.toString()

            // Получаем или создаём categoryId
            val categoryId = dbHelper.getCategoryIdByName(categoryName) ?: dbHelper.addCategory(categoryName)

            // Сохраняем блюдо (type = categoryName)
            dbHelper.addDish(name, categoryName, calories, description, categoryId)

            tvResult.text = "Блюдо сохранено!"

            etName.text.clear()
            etCalories.text.clear()
            etDescription.text.clear()
        }

        btnGoToDishList.setOnClickListener {
            startActivity(Intent(this, DishListActivity::class.java))
        }
    }

    private fun saveCSV() {
        val data = dbHelper.getAllDishes().joinToString("\n") {
            "${it.name},${it.type},${it.calories},${it.description}"
        }
        val file = File(getExternalFilesDir(null), "dishes.csv")
        file.writeText(data)
        Toast.makeText(this, "CSV сохранён!", Toast.LENGTH_SHORT).show()
    }

    private fun loadCSV() {
        val file = File(getExternalFilesDir(null), "dishes.csv")
        if (!file.exists()) {
            Toast.makeText(this, "CSV не найден", Toast.LENGTH_SHORT).show()
            return
        }
        val text = file.readText()
        val lines = text.split("\n").filter { it.isNotBlank() }
        for (line in lines) {
            val parts = line.split(",")
            if (parts.size >= 4) {
                dbHelper.addDish(parts[0], parts[1], parts[2], parts.getOrNull(3) ?: "", null)
            }
        }
        Toast.makeText(this, "CSV загружен!", Toast.LENGTH_SHORT).show()
    }

    private fun saveBinary() {
        val data = dbHelper.getAllDishes().joinToString("|") {
            "${it.name},${it.type},${it.calories},${it.description}"
        }
        val file = File(getExternalFilesDir(null), "dishes.bin")
        ObjectOutputStream(FileOutputStream(file)).use { it.writeObject(data) }
        Toast.makeText(this, "Бинарный файл сохранён", Toast.LENGTH_SHORT).show()
    }

    private fun loadBinary() {
        val file = File(getExternalFilesDir(null), "dishes.bin")
        if (!file.exists()) {
            Toast.makeText(this, "Бинарный файл не найден", Toast.LENGTH_SHORT).show()
            return
        }
        val data = ObjectInputStream(FileInputStream(file)).use { it.readObject() as String }
        val items = data.split("|").filter { it.isNotBlank() }
        for (item in items) {
            val parts = item.split(",")
            if (parts.size >= 4) {
                dbHelper.addDish(parts[0], parts[1], parts[2], parts.getOrNull(3) ?: "", null)
            }
        }
        Toast.makeText(this, "Загружено из бинарного файла!", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissionAndSaveMedia() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
                return
            }
        }
        saveToMediaStore()
    }

    private fun saveToMediaStore() {
        val data = dbHelper.getAllDishes().joinToString("\n") { "${it.name},${it.type},${it.calories},${it.description}" }
        val filename = "healthy_dishes_${System.currentTimeMillis()}.txt"
        val resolver = contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        uri?.let { resolver.openOutputStream(it)?.use { it.write(data.toByteArray()) } }
        Toast.makeText(this, "Файл сохранён в Загрузки", Toast.LENGTH_SHORT).show()
    }
}
