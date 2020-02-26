package com.example.postbox.Helper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

private const val DB_NAME = "TodoDatabase"
private const val DB_VERSION = 1

val DB_DEFAULT_STATE_TABLE = mapOf<String, Int>("TODO" to 1 , "NIGHT" to 2 , "DONE" to 3)

class TodoDataBaseOpenHelper(private val context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    // 初めて生成されるときに生成される
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("PRAGMA foreign_keys=true;")    // 外部キー制約を許可

        db?.execSQL("""
            create table state(
            id integer primary key autoincrement,
            name text not null
            );
        """.trimIndent())

        db?.execSQL("""
            create table todo(
            id integer primary key autoincrement,
            title text not null,
            detail text not null,
            state integer not null,
            foreign key (state) references state(id)
            on delete RESTRICT on update CASCADE
            );
        """.trimIndent())
    }


    // version変えたときに呼ばれる
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun defaultSetup() {
        // stateテーブルは初期値を設定
        writableDatabase.use {
            it.insert("state", null, ContentValues().apply { put("name", "TODO") })
            it.insert("state", null, ContentValues().apply { put("name", "NIGHT") })
            it.insert("state", null, ContentValues().apply { put("name", "TOMORROW") })
        }

        val sharedPreferences = context.getSharedPreferences("todo_database", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("default_setup", true).apply()
    }


    fun insertTodo(title: String, detail: String?, state: Int = 1) {
        writableDatabase.use {
            it.insert("todo", null, ContentValues().apply {
                put("title", title)
                put("detail", detail ?: "")
                put("state", state)
            })
        }
    }


    fun updateTodo(id: Int, title: String? = null, detail: String? = null, state: Int?) {
        val updateColumu = ContentValues().apply {
            if (title != null) put("title", title)
            if (detail != null) put("detail", detail)
            if (state != null) put("state", state)
        }

        writableDatabase.use {
            it.update(
                "todo",
                updateColumu,
                "id == ?",
                arrayOf(id.toString())
            )
        }
    }

    fun deleteTodo(id: Int) {
        writableDatabase.use {
            it.delete(
                "todo",
                "id == ?",
                arrayOf(id.toString())
            )
        }
    }


    fun readTodo(id: Int? = null, state: Int? = null, resolve: (Cursor) -> Unit) {
        var whereSentence = ""
        val whereList = mutableListOf<String>()
        if (id != null) {
            whereSentence += "id == ? "
            whereList.add(id.toString())
        }
        if (state != null) {
            whereSentence += "state == ? "
            whereList.add(state.toString())
        }

        readableDatabase.use {
            val cursor = it.query(
                "todo",
                arrayOf("id", "title", "detail", "state"),
                whereSentence,
                whereList.toTypedArray(),
                null,
                null,
                "id desc",
                null)

            resolve(cursor)
        }
    }



    // --- OPERATE STATE TABLE --- //
//
//    fun readState(where: String? = null, whereValue: Array<String>? = null): Cursor {
//        var cursor: Cursor? = null
//        readableDatabase.use {
//            val c = it.query(
//                "state",
//                arrayOf("id", "name"),
//                where,
//                whereValue,
//                null,
//                null,
//                null,
//                null
//            )
//            cursor = c
//        }
//        return cursor!!
//    }

//    fun stateId(target: String): Int? {
//        val cursor = readState()
//        do {
//            if (cursor.getString(cursor.getColumnIndex("state")) == target) {
//                return cursor.getInt(cursor.getColumnIndex("id"))
//            }
//        } while (cursor.moveToNext())
//
//        return null
//    }
}