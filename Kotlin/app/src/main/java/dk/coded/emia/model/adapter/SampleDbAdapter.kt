package dk.coded.emia.model.adapter

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class SampleDbAdapter(private val context: Context) {

    private var dbHelper: DatabaseHelper? = null
    private var database: SQLiteDatabase? = null

    private val cursor: Cursor?
        get() = database!!.query(SQLITE_TABLE, DEFAULT_COLUMNS, null, null, null, null, null)

    private inner class DatabaseHelper internal constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            Log.w(TAG, DATABASE_CREATE)
            db.execSQL(DATABASE_CREATE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data")
            db.execSQL("DROP TABLE IF EXISTS $SQLITE_TABLE")
            onCreate(db)
        }
    }

    @Throws(SQLException::class)
    fun open(): SampleDbAdapter {
        dbHelper = DatabaseHelper(context)
        database = dbHelper!!.writableDatabase
        return this
    }

    fun close() {
        if (dbHelper != null) {
            dbHelper!!.close()
        }
    }

    fun createItem(text: String, rowSpan: Int, colSpan: Int): Long {
        val initialValues = ContentValues()
        initialValues.put(KEY_TEXT, text)
        initialValues.put(KEY_ROW_SPAN, rowSpan)
        initialValues.put(KEY_COL_SPAN, colSpan)

        return database!!.insert(SQLITE_TABLE, null, initialValues)
    }

    fun deleteAllData(): SampleDbAdapter {
        val doneDelete = database!!.delete(SQLITE_TABLE, null, null)
        Log.w(TAG, Integer.toString(doneDelete))
        return this
    }

    @Throws(SQLException::class)
    fun fetchDataByName(inputText: String?): Cursor? {
        Log.w(TAG, inputText)
        val mCursor: Cursor?
        if (inputText == null || inputText.length == 0) {
            mCursor = cursor
        } else {
            mCursor = database!!.query(true, SQLITE_TABLE, DEFAULT_COLUMNS,
                    "$KEY_TEXT like '%$inputText%'", null, null, null, null, null)
        }
        mCursor?.moveToFirst()
        return mCursor
    }

    fun fetchAllData(): Cursor? {
        val mCursor = cursor

        mCursor?.moveToFirst()
        return mCursor
    }

    fun seedDatabase(items: List<PostsCollectionViewItem>): SampleDbAdapter {
        for (item in items) {
            createItem(item.position.toString(), item.rowSpan, item.columnSpan)
        }
        return this
    }

    companion object {
        val KEY_TEXT = "text"
        private val KEY_ROW_SPAN = "rowspan"
        private val KEY_COL_SPAN = "colspan"
        private val KEY_ROWID = "_id"
        private val TAG = "CountriesDbAdapter"
        private val DATABASE_NAME = "AsymmetricGridViewDemo"
        private val SQLITE_TABLE = "Items"
        private val DATABASE_VERSION = 3

        private val DATABASE_CREATE = "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                KEY_ROWID + " integer PRIMARY KEY autoincrement," +
                KEY_TEXT + "," + KEY_ROW_SPAN + "," + KEY_COL_SPAN + "," +
                " UNIQUE (" + KEY_TEXT + "));"

        private val DEFAULT_COLUMNS = arrayOf(KEY_ROWID, KEY_TEXT, KEY_ROW_SPAN, KEY_COL_SPAN)
    }
}
