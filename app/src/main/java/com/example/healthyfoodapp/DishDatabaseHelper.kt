
package com.example.healthyfoodapp

import DBHelper
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class DishDatabaseHelper(private val context: Context) {
    private val dbHelper: DBHelper = DBHelper(context)
    private var database: SQLiteDatabase? = null

    fun open() {
        database = dbHelper.writableDatabase
    }

    fun addCategory(name: String): Long {
        val values = ContentValues().apply {
            put(DBHelper.COLUMN_CATEGORY_NAME, name)
        }
        return database?.insert(DBHelper.TABLE_CATEGORIES, null, values) ?: -1L
    }

    fun getCategoryIdByName(name: String): Long? {
        val cursor = database?.query(
            DBHelper.TABLE_CATEGORIES,
            arrayOf(DBHelper.COLUMN_CATEGORY_ID),
            "${DBHelper.COLUMN_CATEGORY_NAME} = ?",
            arrayOf(name),
            null, null, null
        ) ?: return null

        return try {
            if (cursor.moveToFirst()) cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CATEGORY_ID))
            else null
        } finally {
            cursor.close()
        }
    }


    // Добавление блюда
    fun addDish(name: String, type: String, calories: String, description: String, categoryId: Long? = null): Long {
        val values = ContentValues().apply {
            put(DBHelper.COLUMN_DISH_NAME, name)
            put(DBHelper.COLUMN_DISH_TYPE, type)
            put(DBHelper.COLUMN_DISH_CALORIES, calories)
            put(DBHelper.COLUMN_DISH_DESCRIPTION, description)
            categoryId?.let { put(DBHelper.COLUMN_DISH_CATEGORY_ID, it) }
        }
        return database?.insert(DBHelper.TABLE_DISHES, null, values) ?: -1L
    }

    // Получение всех блюд
    fun getAllDishes(): List<Dish> {
        val dishes = mutableListOf<Dish>()
        val columns = arrayOf(
            DBHelper.COLUMN_DISH_ID,
            DBHelper.COLUMN_DISH_NAME,
            DBHelper.COLUMN_DISH_TYPE,
            DBHelper.COLUMN_DISH_CALORIES,
            DBHelper.COLUMN_DISH_DESCRIPTION,
            DBHelper.COLUMN_DISH_CATEGORY_ID
        )

        val cursor: Cursor = database?.query(
            DBHelper.TABLE_DISHES,
            columns,
            null, null, null, null, null
        ) ?: return dishes

        try {
            val idIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DISH_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DISH_NAME)
            val typeIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DISH_TYPE)
            val caloriesIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DISH_CALORIES)
            val descriptionIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DISH_DESCRIPTION)
            val categoryIdIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DISH_CATEGORY_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex)
                val type = cursor.getString(typeIndex)
                val calories = cursor.getString(caloriesIndex)
                val description = cursor.getString(descriptionIndex)
                val categoryId = if (cursor.isNull(categoryIdIndex)) null else cursor.getLong(categoryIdIndex)

                dishes.add(Dish(id, name, type, calories, description, categoryId))
            }
        } finally {
            cursor.close()
        }
        return dishes
    }

    // Удаление блюда
    fun deleteDish(id: Long): Int {
        return database?.delete(
            DBHelper.TABLE_DISHES,
            "${DBHelper.COLUMN_DISH_ID} = ?",
            arrayOf(id.toString())
        ) ?: 0
    }

    // Обновление блюда
    fun updateDish(id: Long, name: String, type: String, calories: String, description: String, categoryId: Long? = null): Int {
        val values = ContentValues().apply {
            put(DBHelper.COLUMN_DISH_NAME, name)
            put(DBHelper.COLUMN_DISH_TYPE, type)
            put(DBHelper.COLUMN_DISH_CALORIES, calories)
            put(DBHelper.COLUMN_DISH_DESCRIPTION, description)
            categoryId?.let { put(DBHelper.COLUMN_DISH_CATEGORY_ID, it) }
        }
        return database?.update(
            DBHelper.TABLE_DISHES,
            values,
            "${DBHelper.COLUMN_DISH_ID} = ?",
            arrayOf(id.toString())
        ) ?: 0
    }
}
