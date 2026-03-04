package com.example.healthyfoodapp.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.healthyfoodapp.data.local.DBHelper
import com.example.healthyfoodapp.domain.model.Dish
import com.example.healthyfoodapp.domain.repository.DishRepository

class DishRepositoryImpl(context: Context) : DishRepository {

    private val db by lazy { DBHelper(context).writableDatabase }

    override fun getAllDishes(): List<Dish> {
        val dishes = mutableListOf<Dish>()
        val cursor = db.query(
            DBHelper.TABLE_DISHES,
            arrayOf(DBHelper.COLUMN_DISH_ID, DBHelper.COLUMN_DISH_NAME, DBHelper.COLUMN_DISH_TYPE,
                DBHelper.COLUMN_DISH_CALORIES, DBHelper.COLUMN_DISH_DESCRIPTION, DBHelper.COLUMN_DISH_CATEGORY_ID),
            null, null, null, null, null
        )
        cursor.use {
            while (it.moveToNext()) {
                val catIdx = it.getColumnIndexOrThrow(DBHelper.COLUMN_DISH_CATEGORY_ID)
                dishes.add(Dish(
                    id = it.getLong(it.getColumnIndexOrThrow(DBHelper.COLUMN_DISH_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(DBHelper.COLUMN_DISH_NAME)),
                    type = it.getString(it.getColumnIndexOrThrow(DBHelper.COLUMN_DISH_TYPE)),
                    calories = it.getString(it.getColumnIndexOrThrow(DBHelper.COLUMN_DISH_CALORIES)),
                    description = it.getString(it.getColumnIndexOrThrow(DBHelper.COLUMN_DISH_DESCRIPTION)) ?: "",
                    categoryId = if (it.isNull(catIdx)) null else it.getLong(catIdx)
                ))
            }
        }
        return dishes
    }

    override fun addDish(dish: Dish): Long {
        val values = ContentValues().apply {
            put(DBHelper.COLUMN_DISH_NAME, dish.name)
            put(DBHelper.COLUMN_DISH_TYPE, dish.type)
            put(DBHelper.COLUMN_DISH_CALORIES, dish.calories)
            put(DBHelper.COLUMN_DISH_DESCRIPTION, dish.description)
            dish.categoryId?.let { put(DBHelper.COLUMN_DISH_CATEGORY_ID, it) }
        }
        return db.insert(DBHelper.TABLE_DISHES, null, values)
    }

    override fun updateDish(dish: Dish): Int {
        val values = ContentValues().apply {
            put(DBHelper.COLUMN_DISH_NAME, dish.name)
            put(DBHelper.COLUMN_DISH_TYPE, dish.type)
            put(DBHelper.COLUMN_DISH_CALORIES, dish.calories)
            put(DBHelper.COLUMN_DISH_DESCRIPTION, dish.description)
            dish.categoryId?.let { put(DBHelper.COLUMN_DISH_CATEGORY_ID, it) }
        }
        return db.update(DBHelper.TABLE_DISHES, values,
            "${DBHelper.COLUMN_DISH_ID} = ?", arrayOf(dish.id.toString()))
    }

    override fun deleteDish(id: Long): Int =
        db.delete(DBHelper.TABLE_DISHES, "${DBHelper.COLUMN_DISH_ID} = ?", arrayOf(id.toString()))

    override fun getOrCreateCategoryId(name: String): Long {
        val cursor = db.query(DBHelper.TABLE_CATEGORIES,
            arrayOf(DBHelper.COLUMN_CATEGORY_ID),
            "${DBHelper.COLUMN_CATEGORY_NAME} = ?", arrayOf(name),
            null, null, null)
        cursor.use {
            if (it.moveToFirst())
                return it.getLong(it.getColumnIndexOrThrow(DBHelper.COLUMN_CATEGORY_ID))
        }
        val values = ContentValues().apply { put(DBHelper.COLUMN_CATEGORY_NAME, name) }
        return db.insert(DBHelper.TABLE_CATEGORIES, null, values)
    }
}