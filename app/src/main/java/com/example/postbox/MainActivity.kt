package com.example.postbox

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.postbox.Adapter.PostboxListAdapter
import com.example.postbox.Adapter.TodoListAdapter
import com.example.postbox.Helper.TodoDataBaseOpenHelper
import com.example.postbox.Widget.AddButtonClickListener
import com.example.postbox.Widget.PostboxItemDecoration

class MainActivity : AppCompatActivity() {

    private lateinit var postboxRecyclerView: RecyclerView
    private lateinit var todoBoardRecyclerView: RecyclerView
    private lateinit var addButton: ImageButton

    private val dbHelper: TodoDataBaseOpenHelper = TodoDataBaseOpenHelper(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val todoDataPref = getSharedPreferences("todo_database", Context.MODE_PRIVATE)
        if (!todoDataPref.getBoolean("default_setup", false)) {
            dbHelper.defaultSetup()
        }

        postboxRecyclerView = findViewById(R.id.postbox_recyclerView)
        postboxRecyclerView.adapter = PostboxListAdapter(this, dbHelper, 1)
        postboxRecyclerView.layoutManager = GridLayoutManager(this, 1).apply {
            orientation = GridLayoutManager.HORIZONTAL
        }
        postboxRecyclerView.addItemDecoration(PostboxItemDecoration(convertDp2Px(8f, this).toInt()))

        todoBoardRecyclerView = findViewById(R.id.todo_board_recyclerView)
        todoBoardRecyclerView.adapter = TodoListAdapter(this, dbHelper, 1)
        todoBoardRecyclerView.layoutManager = GridLayoutManager(this, 2).apply {

        }

        addButton = findViewById(R.id.add_todo_button)
        addButton.setOnClickListener(AddButtonClickListener(this) {
            (todoBoardRecyclerView.adapter as TodoListAdapter).insert()
        })

    }
}
