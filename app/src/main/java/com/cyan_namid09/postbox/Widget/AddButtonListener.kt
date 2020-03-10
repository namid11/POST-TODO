package com.cyan_namid09.postbox.Widget

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.cyan_namid09.postbox.Helper.TodoDataBaseOpenHelper
import com.cyan_namid09.postbox.R

class AddButtonClickListener(private val context: Context, private val stateId: Int, val okListener: (View?) -> Unit) : View.OnClickListener {

    private val dbHelper = TodoDataBaseOpenHelper(context)

    override fun onClick(v: View?) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_create_todo, null)
        val dialogBuilder = AlertDialog.Builder(context).apply {
            setView(view)
            setCancelable(true)
        }

        val alertDialog = dialogBuilder.show()
        view.findViewById<Button>(R.id.create_todo_add_button).setOnClickListener {
            val title = view.findViewById<EditText>(R.id.create_todo_title_view).text.toString()
            val detail = view.findViewById<EditText>(R.id.create_todo_detail_view).text.toString()
            if (title == "") {
                Toast.makeText(context, "TODO名を記入してください", Toast.LENGTH_LONG).show()
            } else {
                dbHelper.insertTodo(
                    title = title,
                    detail = detail,
                    state = stateId
                )

                okListener(v)
                alertDialog.dismiss()   // hidden alert dialog
            }
        }
    }
}