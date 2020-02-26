package com.example.postbox.Widget

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.postbox.Helper.TodoDataBaseOpenHelper
import com.example.postbox.R

class AddButtonClickListener(private val context: Context, val okListener: (View?) -> Unit) : View.OnClickListener {

    private val dbHelper = TodoDataBaseOpenHelper(context)

    override fun onClick(v: View?) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_create_todo, null)
        val dialogBuilder = AlertDialog.Builder(context).apply {
            setView(view)
            setIcon(R.mipmap.ic_launcher)
            setCancelable(true)
        }

        val alertDialog = dialogBuilder.show()
        view.findViewById<Button>(R.id.edit_todo_update_button).setOnClickListener {
            val title = view.findViewById<EditText>(R.id.edit_todo_title_view).text.toString()
            val detail = view.findViewById<EditText>(R.id.edit_todo_detail_view).text.toString()
            if (title == "") {
                Toast.makeText(context, "TODO名を記入してください", Toast.LENGTH_LONG).show()
            } else {
                dbHelper.insertTodo(
                    title = title,
                    detail = detail,
                    state = 1
                )

                okListener(v)
                alertDialog.dismiss()   // hidden alert dialog
            }
        }
    }
}