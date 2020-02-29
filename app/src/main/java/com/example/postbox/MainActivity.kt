package com.example.postbox

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.postbox.Adapter.PostboxListAdapter
import com.example.postbox.Adapter.TodoListAdapter
import com.example.postbox.Functions.*
import com.example.postbox.Helper.DB_DEFAULT_STATE_TABLE
import com.example.postbox.Helper.TodoDataBaseOpenHelper
import com.example.postbox.Service.operateFirstJobService
import com.example.postbox.Widget.AddButtonClickListener
import com.example.postbox.Widget.PostboxItemDecoration

class MainActivity : AppCompatActivity() {

    private lateinit var postboxRecyclerView: RecyclerView
    private lateinit var todoBoardRecyclerView: RecyclerView
    private lateinit var addButton: ImageButton
    private lateinit var boardTitleTextView: TextView

    private val dbHelper: TodoDataBaseOpenHelper = TodoDataBaseOpenHelper(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // firstSetUp
        firstSetUp()


        val todoDataPref = getSharedPreferences("todo_database", Context.MODE_PRIVATE)
        if (!todoDataPref.getBoolean("default_setup", false)) {
            dbHelper.defaultSetup()
        }

        val stateName = intent.getStringExtra("state_name") ?: "TODO"
        val stateID = intent.getIntExtra("state_id", DB_DEFAULT_STATE_TABLE["TODO"] ?: 0)

        boardTitleTextView = findViewById(R.id.todo_board_title_textview)
        boardTitleTextView.text = stateName

        postboxRecyclerView = findViewById(R.id.postbox_recyclerView)
        postboxRecyclerView.adapter = PostboxListAdapter(this, dbHelper, targetState = stateID)
        postboxRecyclerView.layoutManager = GridLayoutManager(this, 1).apply {
            orientation = GridLayoutManager.HORIZONTAL
        }
        postboxRecyclerView.addItemDecoration(PostboxItemDecoration(
            convertDp2Px(
                4f,
                this
            ).toInt()))

        todoBoardRecyclerView = findViewById(R.id.todo_board_recyclerView)
        todoBoardRecyclerView.adapter = TodoListAdapter(this, dbHelper, targetState = stateID)
        todoBoardRecyclerView.layoutManager = GridLayoutManager(this, 2).apply {

        }

        addButton = findViewById(R.id.add_todo_button)
        if (stateName != DB_DEFAULT_STATE_TABLE.keys.elementAt(0)) addButton.visibility = View.INVISIBLE
        addButton.setOnClickListener(AddButtonClickListener(this) {
            (todoBoardRecyclerView.adapter as TodoListAdapter).insert()
        })

    }


    override fun onResume() {
        super.onResume()

        // --- TODOのstateを更新 --- //
        val dateDataPref = getSharedPreferences("todo_date_database", Context.MODE_PRIVATE)
        val currentTime = System.currentTimeMillis()
        val lastUpdateDate = dateDataPref.getLong("last_update_date", currentTime)
        if (lastUpdateDate == currentTime) dateDataPref.edit().putLong("last_update_date", currentTime).apply() // 値がセットされていなかったら、初期セット
        val diffDay = diffDay(toCalendar(lastUpdateDate), getNowDate())  // 最終更新からの日数の差
        when {
            diffDay == 0 -> {

            }
            diffDay == 1 -> {
                // TODO(TODOs in Tomorrow update Today list.
                // TODO(TODOs in today update Yet list.
            }
            diffDay >= 2 -> {
                // TODO(TODOs in Tomorrow update Yet list.
                // TODO(TODOs in today update Yet list.
            }
            else -> {

            }
        }

    }


    private fun firstSetUp() {
        val metaPref = getSharedPreferences("meta", Context.MODE_PRIVATE)
        if (!metaPref.getBoolean("first_setup", false)) {
            // channel作成
            makeNotificationChannel(this)
            // 通知用初期設定Job開始
            operateFirstJobService(this)

            metaPref.edit().putBoolean("first_setup", true).apply()
        }
    }
}
