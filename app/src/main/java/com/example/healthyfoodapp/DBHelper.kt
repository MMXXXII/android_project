import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "healthyfood.db"
        private const val DATABASE_VERSION = 20

        // Table "dishes"
        const val TABLE_DISHES = "dishes"
        const val COLUMN_DISH_ID = "id"
        const val COLUMN_DISH_NAME = "name"
        const val COLUMN_DISH_TYPE = "type"
        const val COLUMN_DISH_CALORIES = "calories"
        const val COLUMN_DISH_DESCRIPTION = "description"
        const val COLUMN_DISH_CATEGORY_ID = "category_id"  // Foreign key

        // Table "categories"
        const val TABLE_CATEGORIES = "categories"
        const val COLUMN_CATEGORY_ID = "id"
        const val COLUMN_CATEGORY_NAME = "name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createCategoriesTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY_NAME TEXT NOT NULL UNIQUE
            )
        """.trimIndent()

        val createDishesTable = """
            CREATE TABLE $TABLE_DISHES (
                $COLUMN_DISH_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DISH_NAME TEXT NOT NULL,
                $COLUMN_DISH_TYPE TEXT NOT NULL,
                $COLUMN_DISH_CALORIES TEXT NOT NULL,
                $COLUMN_DISH_DESCRIPTION TEXT,
                $COLUMN_DISH_CATEGORY_ID INTEGER,
                FOREIGN KEY($COLUMN_DISH_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COLUMN_CATEGORY_ID)
            )
        """.trimIndent()

        db.execSQL(createCategoriesTable)
        db.execSQL(createDishesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("ALTER TABLE $TABLE_CATEGORIES RENAME TO ${TABLE_CATEGORIES}_old")
        db.execSQL("ALTER TABLE $TABLE_DISHES RENAME TO ${TABLE_DISHES}_old")

        onCreate(db)

        db.execSQL("""
            INSERT INTO $TABLE_CATEGORIES ($COLUMN_CATEGORY_ID, $COLUMN_CATEGORY_NAME)
            SELECT $COLUMN_CATEGORY_ID, $COLUMN_CATEGORY_NAME
            FROM ${TABLE_CATEGORIES}_old
        """)

        db.execSQL("""
            INSERT INTO $TABLE_DISHES (
                $COLUMN_DISH_ID, 
                $COLUMN_DISH_NAME, 
                $COLUMN_DISH_TYPE, 
                $COLUMN_DISH_CALORIES, 
                $COLUMN_DISH_DESCRIPTION, 
                $COLUMN_DISH_CATEGORY_ID
            )
            SELECT 
                $COLUMN_DISH_ID, 
                $COLUMN_DISH_NAME, 
                $COLUMN_DISH_TYPE, 
                $COLUMN_DISH_CALORIES, 
                $COLUMN_DISH_DESCRIPTION, 
                $COLUMN_DISH_CATEGORY_ID
            FROM ${TABLE_DISHES}_old
        """)

        db.execSQL("DROP TABLE ${TABLE_CATEGORIES}_old")
        db.execSQL("DROP TABLE ${TABLE_DISHES}_old")
    }
}
