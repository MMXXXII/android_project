package com.example.healthyfoodapp.presentation

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.healthyfoodapp.R
import com.example.healthyfoodapp.data.repository.DishRepositoryImpl
import com.example.healthyfoodapp.domain.model.Dish
import com.example.healthyfoodapp.domain.usecase.GetAllDishesUseCase
import com.example.healthyfoodapp.presentation.fragment.AddDishFragment
import com.example.healthyfoodapp.presentation.fragment.DishListFragment
import com.example.healthyfoodapp.presentation.fragment.MealSearchFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*
import java.io.*

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private val dishRepository by lazy { DishRepositoryImpl(this) }
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        if (savedInstanceState == null) {
            navigateTo(AddDishFragment(), addToBackStack = false)
        }

        findViewById<Button>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }

        navigationView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawers()
            when (item.itemId) {
                R.id.nav_add_dish      -> navigateTo(AddDishFragment(), addToBackStack = false)
                R.id.nav_my_catalog    -> navigateTo(DishListFragment())
                R.id.nav_search_api    -> navigateTo(MealSearchFragment())
                R.id.nav_save_csv      -> Thread { saveCSV() }.start()
                R.id.nav_load_csv      -> mainScope.launch { loadCSV() }
                R.id.nav_save_bin      -> saveBinary()
                R.id.nav_load_bin      -> loadBinary()
                R.id.nav_save_media    -> checkPermissionAndSaveMedia()
                R.id.nav_exit          -> finish()
            }
            true
        }
    }

    private fun navigateTo(fragment: Fragment, addToBackStack: Boolean = true) {
        val tx = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
        if (addToBackStack) tx.addToBackStack(null)
        tx.commit()
    }


    private fun saveCSV() {
        try {
            val data = GetAllDishesUseCase(dishRepository)().joinToString("\n") {
                "${it.name},${it.type},${it.calories},${it.description}"
            }
            File(getExternalFilesDir(null), "dishes.csv").writeText(data)
            runOnUiThread { Toast.makeText(this, "CSV сохранён", Toast.LENGTH_SHORT).show() }
        } catch (e: Exception) {
            runOnUiThread { Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show() }
        }
    }

    private suspend fun loadCSV() {
        withContext(Dispatchers.IO) {
            val file = File(getExternalFilesDir(null), "dishes.csv")
            if (!file.exists()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "CSV не найден", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }
            file.readText().split("\n").filter { it.isNotBlank() }.forEach { line ->
                val parts = line.split(",")
                if (parts.size >= 4) {
                    val categoryId = dishRepository.getOrCreateCategoryId(parts[1])
                    dishRepository.addDish(Dish(
                        name = parts[0], type = parts[1],
                        calories = parts[2], description = parts.getOrNull(3) ?: "",
                        categoryId = categoryId
                    ))
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "CSV загружен", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBinary() {
        try {
            val data = GetAllDishesUseCase(dishRepository)().joinToString("|") {
                "${it.name},${it.type},${it.calories},${it.description}"
            }
            val file = File(getExternalFilesDir(null), "dishes.bin")
            ObjectOutputStream(FileOutputStream(file)).use { it.writeObject(data) }
            Toast.makeText(this, "Бинарный файл сохранён", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBinary() {
        try {
            val file = File(getExternalFilesDir(null), "dishes.bin")
            if (!file.exists()) {
                Toast.makeText(this, "Бинарный файл не найден", Toast.LENGTH_SHORT).show()
                return
            }
            val data = ObjectInputStream(FileInputStream(file)).use { it.readObject() as String }
            data.split("|").filter { it.isNotBlank() }.forEach { item ->
                val parts = item.split(",")
                if (parts.size >= 4) {
                    val categoryId = dishRepository.getOrCreateCategoryId(parts[1])
                    dishRepository.addDish(Dish(
                        name = parts[0], type = parts[1],
                        calories = parts[2], description = parts.getOrNull(3) ?: "",
                        categoryId = categoryId
                    ))
                }
            }
            Toast.makeText(this, "Загружено из бинарного файла!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionAndSaveMedia() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
            return
        }
        saveToMediaStore()
    }

    private fun saveToMediaStore() {
        try {
            val data = GetAllDishesUseCase(dishRepository)().joinToString("\n") {
                "${it.name},${it.type},${it.calories},${it.description}"
            }
            val filename = "healthy_dishes_${System.currentTimeMillis()}.txt"
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            }
            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let { contentResolver.openOutputStream(it)?.use { os -> os.write(data.toByteArray()) } }
            Toast.makeText(this, "Файл сохранён в Загрузки", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}