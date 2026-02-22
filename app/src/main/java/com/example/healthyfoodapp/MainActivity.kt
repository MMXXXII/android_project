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
import kotlinx.coroutines.*
import java.io.*
import androidx.lifecycle.lifecycleScope
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DishDatabaseHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var tvThreadStatus: TextView
    private lateinit var tvCoroutineStatus: TextView
    private lateinit var btnCancelCoroutine: Button

    private var workerThread: Thread? = null
    private var dataProcessingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DishDatabaseHelper(this)
        dbHelper.open()

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        tvThreadStatus = findViewById(R.id.tvThreadStatus)
        tvCoroutineStatus = findViewById(R.id.tvCoroutineStatus)

        val etName = findViewById<EditText>(R.id.etName)
        val etCalories = findViewById<EditText>(R.id.etCalories)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val rgCategory = findViewById<RadioGroup>(R.id.rgCategory)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnMenu = findViewById<Button>(R.id.btnMenu)
        val btnGoToDishList = findViewById<Button>(R.id.btnGoToDishList)
        val btnProcessDataThread = findViewById<Button>(R.id.btnProcessDataThread)
        val btnProcessDataCoroutine = findViewById<Button>(R.id.btnProcessDataCoroutine)
        val btnCancelThread = findViewById<Button>(R.id.btnCancelThread)

        btnCancelCoroutine = findViewById<Button>(R.id.btnCancelCoroutine)
        btnCancelCoroutine.isEnabled = false

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
                R.id.nav_search_api -> {
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this, MealSearchActivity::class.java))
                    true
                }
                R.id.nav_save_csv -> { processInThread { saveCSV() }; true }
                R.id.nav_load_csv -> { processWithCoroutine { loadCSV() }; true }
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

            val categoryId = dbHelper.getCategoryIdByName(categoryName) ?: dbHelper.addCategory(categoryName)
            dbHelper.addDish(name, categoryName, calories, description, categoryId)

            tvResult.text = "Блюдо сохранено!"
            etName.text.clear()
            etCalories.text.clear()
            etDescription.text.clear()
        }

        btnGoToDishList.setOnClickListener {
            startActivity(Intent(this, DishListActivity::class.java))
        }

        btnProcessDataThread.setOnClickListener {
            startThreadProcessing()
        }

        btnProcessDataCoroutine.setOnClickListener {
            coroutinePhase1()
        }

        btnCancelThread.setOnClickListener {
            cancelThreadProcessing()
        }

        btnCancelCoroutine.setOnClickListener {
            cancelCoroutineProcessing()
        }
    }

    private fun startThreadProcessing() {
        workerThread?.interrupt()
        workerThread = Thread {
            try {
                val dishes = threadPhase1()
                threadPhase2(dishes)
            } catch (e: InterruptedException) {
                updateThreadStatus("Поток отменен")
            } catch (e: Exception) {
                updateThreadStatus("Ошибка: ${e.message}")
            }
        }
        workerThread?.start()
    }

    private fun threadPhase1(): List<Dish> {
        updateThreadStatus("Поток 1: Подсчет калорий...")
        Thread.sleep(2000)

        val dishes = dbHelper.getAllDishes()
        var totalCalories = 0
        for (dish in dishes) {
            if (Thread.currentThread().isInterrupted) throw InterruptedException()
            totalCalories += dish.calories.toIntOrNull() ?: 0
            Thread.sleep(100)
        }

        updateThreadStatus("Поток 1 завершен. Калорий: $totalCalories")
        Thread.sleep(1000)
        return dishes
    }

    private fun threadPhase2(dishes: List<Dish>) {
        updateThreadStatus("Поток 2: Расчет средней...")
        Thread.sleep(2000)

        val totalCalories = dishes.sumOf { it.calories.toIntOrNull() ?: 0 }
        val average = if (dishes.isNotEmpty()) totalCalories / dishes.size else 0

        updateThreadStatus("Готово! Средняя калорийность: $average ккал")
    }

    private fun cancelThreadProcessing() {
        workerThread?.interrupt()
        updateThreadStatus("Отмена потока...")
    }

    private fun updateThreadStatus(message: String) {
        runOnUiThread {
            tvThreadStatus.text = "Статус потока: $message"
        }
    }

    private fun processInThread(action: () -> Unit) {
        Thread {
            try {
                action()
                runOnUiThread {
                    Toast.makeText(this, "Операция завершена", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun coroutinePhase1() {
        dataProcessingJob?.cancel()
        btnCancelCoroutine.isEnabled = true

        dataProcessingJob = lifecycleScope.launch(Dispatchers.Default) {
            try {
                withContext(Dispatchers.Main) {
                    updateCoroutineStatusUI("Корутина 1: Загрузка данных...")
                }

                if (Random.nextInt(100) < 10) {
                    throw RuntimeException("Случайная ошибка загрузки данных")
                }

                val dishes = withContext(Dispatchers.IO) {
                    delay(2000)
                    dbHelper.getAllDishes()
                }

                withContext(Dispatchers.Main) {
                    updateCoroutineStatusUI("Корутина 1 завершена. Блюд: ${dishes.size}")
                }

                delay(500)

                coroutinePhase2(dishes)

            } catch (e: CancellationException) {
                withContext(Dispatchers.Main) { updateCoroutineStatusUI("Корутина 1 отменена") }
                btnCancelCoroutine.isEnabled = false
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { updateCoroutineStatusUI("Ошибка: ${e.message}") }
                btnCancelCoroutine.isEnabled = false
            }
        }
    }

    private fun coroutinePhase2(dishes: List<Dish>) {
        dataProcessingJob = lifecycleScope.launch(Dispatchers.Default) {
            try {
                withContext(Dispatchers.Main) {
                    updateCoroutineStatusUI("Корутина 2: Подсчет калорий...")
                }

                var totalCalories = 0
                var maxCalories = 0
                var maxCalorieDish = ""

                for (dish in dishes) {
                    ensureActive()
                    val calories = dish.calories.toIntOrNull() ?: 0
                    totalCalories += calories

                    if (calories > maxCalories) {
                        maxCalories = calories
                        maxCalorieDish = dish.name
                    }

                    delay(100)
                }

                val average = if (dishes.isNotEmpty()) totalCalories / dishes.size else 0

                withContext(Dispatchers.Main) {
                    updateCoroutineStatusUI(
                        "Готово! Средняя: $average ккал, Макс: $maxCalories ($maxCalorieDish)"
                    )
                    btnCancelCoroutine.isEnabled = false
                }

            } catch (e: CancellationException) {
                withContext(Dispatchers.Main) { updateCoroutineStatusUI("Корутина 2 отменена") }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { updateCoroutineStatusUI("Ошибка: ${e.message}") }
            }
        }
    }

    private fun cancelCoroutineProcessing() {
        dataProcessingJob?.cancel()
        updateCoroutineStatusUI("Отмена корутины...")
    }

    private fun updateCoroutineStatusUI(message: String) {
        tvCoroutineStatus.text = "Статус корутины: $message"
    }

    private fun processWithCoroutine(action: suspend () -> Unit) {
        lifecycleScope.launch {
            try {
                action()
                Toast.makeText(this@MainActivity, "Операция завершена", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveCSV() {
        val data = dbHelper.getAllDishes().joinToString("\n") {
            "${it.name},${it.type},${it.calories},${it.description}"
        }
        val file = java.io.File(getExternalFilesDir(null), "dishes.csv")
        file.writeText(data)
    }

    private suspend fun loadCSV() {
        withContext(Dispatchers.IO) {
            val file = java.io.File(getExternalFilesDir(null), "dishes.csv")
            if (!file.exists()) throw FileNotFoundException("CSV не найден")

            val text = file.readText()
            val lines = text.split("\n").filter { it.isNotBlank() }

            for (line in lines) {
                val parts = line.split(",")
                if (parts.size >= 4) {
                    dbHelper.addDish(parts[0], parts[1], parts[2], parts.getOrNull(3) ?: "", null)
                }
            }
        }
    }

    private fun saveBinary() {
        val data = dbHelper.getAllDishes().joinToString("|") {
            "${it.name},${it.type},${it.calories},${it.description}"
        }
        val file = java.io.File(getExternalFilesDir(null), "dishes.bin")
        ObjectOutputStream(FileOutputStream(file)).use { it.writeObject(data) }
        Toast.makeText(this, "Бинарный файл сохранён", Toast.LENGTH_SHORT).show()
    }

    private fun loadBinary() {
        val file = java.io.File(getExternalFilesDir(null), "dishes.bin")
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
        val data = dbHelper.getAllDishes().joinToString("\n") {
            "${it.name},${it.type},${it.calories},${it.description}"
        }
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

    override fun onDestroy() {
        super.onDestroy()
        workerThread?.interrupt()
    }
}