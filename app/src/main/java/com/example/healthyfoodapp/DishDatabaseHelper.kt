// Файл: DishDatabaseHelper.kt
package com.example.healthyfoodapp

import DBHelper
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class DishDatabaseHelper(context: Context) {

    private val dbHelper: DBHelper = DBHelper(context)
    private var database: SQLiteDatabase? = null

    fun open() {
        database = dbHelper.writableDatabase
    }

    fun close() {
        dbHelper.close()
    }

    fun addDish(name: String, type: String, calories: String, description: String): Long {
        val values = ContentValues().apply {
            put(DBHelper.COLUMN_DISH_NAME, name)
            put(DBHelper.COLUMN_DISH_TYPE, type)
            put(DBHelper.COLUMN_DISH_CALORIES, calories)
            put(DBHelper.COLUMN_DISH_DESCRIPTION, description)
        }
        return database?.insert(DBHelper.TABLE_DISHES, null, values) ?: -1
    }

    fun getAllDishes(): List<Dish> {
        val dishes = mutableListOf<Dish>()
        val columns = arrayOf(
            DBHelper.COLUMN_DISH_ID,
            DBHelper.COLUMN_DISH_NAME,
            DBHelper.COLUMN_DISH_TYPE,
            DBHelper.COLUMN_DISH_CALORIES,
            DBHelper.COLUMN_DISH_DESCRIPTION
        )
        val cursor: Cursor = database?.query(
            DBHelper.TABLE_DISHES,
            columns,
            null, null, null, null, null
        ) ?: return dishes

        val idIndex = cursor.getColumnIndex(DBHelper.COLUMN_DISH_ID)
        val nameIndex = cursor.getColumnIndex(DBHelper.COLUMN_DISH_NAME)
        val typeIndex = cursor.getColumnIndex(DBHelper.COLUMN_DISH_TYPE)
        val caloriesIndex = cursor.getColumnIndex(DBHelper.COLUMN_DISH_CALORIES)
        val descriptionIndex = cursor.getColumnIndex(DBHelper.COLUMN_DISH_DESCRIPTION)

        while (cursor.moveToNext()) {
            val id = if (idIndex != -1) cursor.getLong(idIndex) else 0L
            val name = if (nameIndex != -1) cursor.getString(nameIndex) else ""
            val type = if (typeIndex != -1) cursor.getString(typeIndex) else ""
            val calories = if (caloriesIndex != -1) cursor.getString(caloriesIndex) else ""
            val description = if (descriptionIndex != -1) cursor.getString(descriptionIndex) else ""
            dishes.add(Dish(id, name, type, calories, description))
        }
        cursor.close()
        return dishes
    }



    fun deleteDish(id: Long): Int {
        return database?.delete(
            DBHelper.TABLE_DISHES,
            "${DBHelper.COLUMN_DISH_ID} = ?",
            arrayOf(id.toString())
        ) ?: 0
    }

    fun updateDish(id: Long, name: String, type: String, calories: String, description: String): Int {
        val values = ContentValues().apply {
            put(DBHelper.COLUMN_DISH_NAME, name)
            put(DBHelper.COLUMN_DISH_TYPE, type)
            put(DBHelper.COLUMN_DISH_CALORIES, calories)
            put(DBHelper.COLUMN_DISH_DESCRIPTION, description)
        }

        return database?.update(
            DBHelper.TABLE_DISHES,
            values,
            "${DBHelper.COLUMN_DISH_ID} = ?",
            arrayOf(id.toString())
        ) ?: 0
    }
}
