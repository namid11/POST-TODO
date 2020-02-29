package com.example.postbox.Widget

import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.postbox.Helper.TodoDataBaseOpenHelper
import com.example.postbox.R
import java.lang.Exception

class TodoItemViewClickListener(private val context: Context, private val todoID: Int, val updateListener: (View?) -> Unit, val deleteListener: (View?) -> Unit): View.OnClickListener {

    private lateinit var updateButton: Button
    private lateinit var deleteButton: ImageButton

    private val dbHelper = TodoDataBaseOpenHelper(context)

    override fun onClick(v: View?) {
        // layoutをviewにインフレート
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_todo, null)

        val dialogBuilder = AlertDialog.Builder(context).apply {
            setView(dialogView) // Viewをセット
            setIcon(R.mipmap.ic_launcher) // setTitle()をしないと出ない？
            setCancelable(true) // dialog以外をタップしたら、消える
        }
        val alertDialog = dialogBuilder.show()// show


        // title , detail にもとのデータをセット
        val titleEditView = dialogView.findViewById<EditText>(R.id.edit_todo_title_view)
        val detailEditView = dialogView.findViewById<EditText>(R.id.edit_todo_detail_view)
        dbHelper.readTodo(id = todoID) { cursor: Cursor ->
            cursor.moveToFirst()
            titleEditView.setText(
                cursor.getString(cursor.getColumnIndex("title")),
                TextView.BufferType.NORMAL
            )
            detailEditView.setText(
                cursor.getString(cursor.getColumnIndex("detail"))
            )
        }

        // Update
        updateButton = dialogView.findViewById(R.id.edit_todo_update_button)
        updateButton.setOnClickListener {
            val title = titleEditView.text.toString()
            val detail = detailEditView.text.toString()
            if (title == "") {
                Toast.makeText(context, "TODO名を記入してください", Toast.LENGTH_LONG).show()
            } else {
                dbHelper.updateTodo(
                    todoID,
                    title = title,
                    detail = detail,
                    state = null
                )
                updateListener(v)
                alertDialog.dismiss()   // hidden alert dialog
            }
        }

        // Delete
        deleteButton = dialogView.findViewById(R.id.edit_todo_delete_button)
        deleteButton.setOnClickListener {
            dbHelper.deleteTodo(todoID)
            deleteListener(v)
            alertDialog.dismiss()   // hidden alert dialog
        }
    }

}


class TodoItemViewDragListener(context: Context): View.OnDragListener {
    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        when (event?.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                Log.d("[Drag Action]", "start")
                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                Log.d("[Drag Action]", "end")
                Log.d("[Drag Action]", event.result.toString())
                return true
            }

        }
        return false
    }

}