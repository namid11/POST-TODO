package com.example.postbox.Widget

import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.postbox.Helper.TodoDataBaseOpenHelper
import com.example.postbox.R
import java.lang.Exception

class TodoItemViewClickListener(private val context: Context, val updateListener: (View?) -> Unit, val deleteListener: (View?) -> Unit): View.OnClickListener {

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


        val id = v?.tag as Int
        // title , detail にもとのデータをセット
        val titleEditView = dialogView.findViewById<EditText>(R.id.edit_todo_title_view)
        val detailEditView = dialogView.findViewById<EditText>(R.id.edit_todo_detail_view)
        dbHelper.readTodo(id = id) { cursor: Cursor ->
            cursor.moveToFirst()
            titleEditView.setText(
                cursor.getString(cursor.getColumnIndex("title")),
                TextView.BufferType.NORMAL
            )
            detailEditView.setText(
                cursor.getString(cursor.getColumnIndex("detail"))
            )
        }

        updateButton = dialogView.findViewById(R.id.edit_todo_update_button)
        updateButton.setOnClickListener {
            val title = titleEditView.text.toString()
            val detail = detailEditView.text.toString()
            if (title == "") {
                Toast.makeText(context, "TODO名を記入してください", Toast.LENGTH_LONG).show()
            } else {
                dbHelper.updateTodo(
                    id,
                    title = title,
                    detail = detail,
                    state = 1
                )
                updateListener(v)
                alertDialog.dismiss()   // hidden alert dialog
            }
        }

        deleteButton = dialogView.findViewById(R.id.edit_todo_delete_button)
        deleteButton.setOnClickListener {
            dbHelper.deleteTodo(id)
            deleteListener(v)
            alertDialog.dismiss()   // hidden alert dialog
        }
    }

}