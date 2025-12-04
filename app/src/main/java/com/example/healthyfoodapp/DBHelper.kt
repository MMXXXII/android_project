import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "healthyfood.db"
        private const val DATABASE_VERSION = 1

        // Таблица "dishes"
        const val TABLE_DISHES = "dishes"
        const val COLUMN_DISH_ID = "id"
        const val COLUMN_DISH_NAME = "name"
        const val COLUMN_DISH_TYPE = "type"
        const val COLUMN_DISH_CALORIES = "calories"
        const val COLUMN_DISH_DESCRIPTION = "description"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createDishesTable = """
            CREATE TABLE $TABLE_DISHES (
                $COLUMN_DISH_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DISH_NAME TEXT NOT NULL,
                $COLUMN_DISH_TYPE TEXT NOT NULL,
                $COLUMN_DISH_CALORIES TEXT NOT NULL,
                $COLUMN_DISH_DESCRIPTION TEXT
            )
        """.trimIndent()

        db.execSQL(createDishesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_DISHES")
            onCreate(db)
        }
    }
}
