package com.cyan_namid09.postbox.Helper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.NullPointerException

private const val DB_NAME = "TodoDatabase"
private const val DB_VERSION = 1

val DB_DEFAULT_STATE_TABLE = mapOf<String, Int>("DONE" to 1, "YET" to 2, "TODAY" to 3, "TOMORROW" to 4)

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
            for(i in 0 until DB_DEFAULT_STATE_TABLE.count()) it.insert("state", null, ContentValues().apply { put("name", DB_DEFAULT_STATE_TABLE.keys.elementAt(i)) })
        }

        val sharedPreferences = context.getSharedPreferences("todo_database", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("default_setup", true).apply()
    }


    fun insertTodo(title: String, detail: String?, state: Int = DB_DEFAULT_STATE_TABLE["TODAY"] ?: throw NullPointerException()) {
        writableDatabase.use {
            it.insert("todo", null, ContentValues().apply {
                put("title", title)
                put("detail", detail ?: "")
                put("state", state)
            })
        }
    }


    fun updateTodo(id: Int, title: String? = null, detail: String? = null, state: Int?) {
        val updateColumn = ContentValues().apply {
            if (title != null) put("title", title)
            if (detail != null) put("detail", detail)
            if (state != null) put("state", state)
        }

        writableDatabase.use {
            it.update(
                "todo",
                updateColumn,
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

    fun deleteTodoOnState(stateId: Int) {
        writableDatabase.use {
            it.delete(
                "todo",
                "state == ?",
                arrayOf(stateId.toString())
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



    // --- READ STATE TABLE --- //
    fun readState(where: String? = null, whereValue: Array<String>? = null, resolve: (Cursor) -> Unit) {
        var cursor: Cursor? = null
        readableDatabase.use {
            val cursor = it.query(
                "state",
                arrayOf("id", "name"),
                where,
                whereValue,
                null,
                null,
                null,
                null
            )

            resolve(cursor)
        }
    }

    fun insertState(name: String, resolve: (SQLiteDatabase) -> Unit = {}) {
        writableDatabase.use {
            it.insert("state", null, ContentValues().apply {
                put("name", name)
            })

            resolve(it)
        }
    }

    fun updateState(id: Int, name: String, resolve: (SQLiteDatabase) -> Unit = {}) {
        val updateColumn = ContentValues().apply {
            put("name", name)
        }

        writableDatabase.use {
            it.update(
                "state",
                updateColumn,
                "id == ?",
                arrayOf(id.toString())
            )
            resolve(it)
        }
    }

    fun deleteState(id: Int, resolve: (SQLiteDatabase) -> Unit = {}) {
        writableDatabase.use {
            it.delete(
                "state",
                "id == ?",
                arrayOf(id.toString())
            )
            resolve(it)
        }
    }


    // --- OPERATE TABLE --- //
    // 1日分更新
    fun operateOneDayUpdateState() {
        writableDatabase.use {
            // 昨日のTODOを"YET"
            it.update(
                "todo",
                ContentValues().apply { put("state", DB_DEFAULT_STATE_TABLE["YET"]) },
                "state == ?",
                arrayOf(DB_DEFAULT_STATE_TABLE["TODAY"].toString())
            )

            // 昨日のTOMORROWを"TODAY"
            it.update(
                "todo",
                ContentValues().apply { put("state", DB_DEFAULT_STATE_TABLE["TODAY"]) },
                "state == ?",
                arrayOf(DB_DEFAULT_STATE_TABLE["TOMORROW"].toString())
            )
        }
    }

    fun operateAllYetUpdateState() {
        writableDatabase.use {
            // 昨日のTODOを"YET"
            it.update(
                "todo",
                ContentValues().apply { put("state", DB_DEFAULT_STATE_TABLE["YET"]) },
                "state == ?",
                arrayOf(DB_DEFAULT_STATE_TABLE["TODAY"].toString())
            )

            // 昨日のTOMORROWを"YET"
            it.update(
                "todo",
                ContentValues().apply { put("state", DB_DEFAULT_STATE_TABLE["YET"]) },
                "state == ?",
                arrayOf(DB_DEFAULT_STATE_TABLE["TOMORROW"].toString())
            )
        }
    }


    fun operateRemovingDoneTodo(stateId: Int) {
        writableDatabase.use {
            it.delete(
                "todo",
                "state == ?",
                arrayOf(stateId.toString())
            )
        }
    }
}