package com.example.healthyfoodapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "healthyfood.db"
        private const val DATABASE_VERSION = 20

        const val TABLE_DISHES = "dishes"
        const val COLUMN_DISH_ID = "id"
        const val COLUMN_DISH_NAME = "name"
        const val COLUMN_DISH_TYPE = "type"
        const val COLUMN_DISH_CALORIES = "calories"
        const val COLUMN_DISH_DESCRIPTION = "description"
        const val COLUMN_DISH_CATEGORY_ID = "category_id"

        const val TABLE_CATEGORIES = "categories"
        const val COLUMN_CATEGORY_ID = "id"
        const val COLUMN_CATEGORY_NAME = "name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY_NAME TEXT NOT NULL UNIQUE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_DISHES (
                $COLUMN_DISH_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DISH_NAME TEXT NOT NULL,
                $COLUMN_DISH_TYPE TEXT NOT NULL,
                $COLUMN_DISH_CALORIES TEXT NOT NULL,
                $COLUMN_DISH_DESCRIPTION TEXT,
                $COLUMN_DISH_CATEGORY_ID INTEGER,
                FOREIGN KEY($COLUMN_DISH_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COLUMN_CATEGORY_ID)
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DISHES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }
}