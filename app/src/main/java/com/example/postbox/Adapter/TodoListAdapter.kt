package com.example.postbox.Adapter

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.postbox.Helper.TodoDataBaseOpenHelper
import com.example.postbox.R
import com.example.postbox.Widget.TodoItemViewClickListener
import com.example.postbox.Widget.TodoItemViewDragListener
import org.json.JSONObject
import java.lang.Exception

class TodoListAdapter(private val context: Context, private val dbHelper: TodoDataBaseOpenHelper, private val targetState: Int): RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val frameView = view.findViewById<ConstraintLayout>(R.id.frame_constraint_layout)
        val textView = view.findViewById<TextView>(R.id.todo_tile_text)
        var todoID: Int? = null
    }

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.layout_todo_tile, parent, false)
        val viewHolder = TodoListAdapter.ViewHolder(view)

        return viewHolder
    }

    override fun getItemCount(): Int {
        val readDB = dbHelper.readableDatabase
        val cursor = readDB.query(
            "todo",
            arrayOf("id", "title", "detail", "state"),
            "state == ?",
            arrayOf(targetState.toString()),
            null,
            null,
            null,
            null
        )
        val count = cursor.count
        cursor.close()
        readDB.close()

        return count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        dbHelper.readTodo(state = targetState) {cursor: Cursor ->
            cursor.move(position + 1)
            holder.textView.text = cursor.getString(cursor.getColumnIndex("title"))
            holder.todoID = cursor.getInt(cursor.getColumnIndex("id"))
        }

        holder.frameView.setOnClickListener(TodoItemViewClickListener(
            context,
            holder.todoID ?: throw NullPointerException(),
            updateListener = {
                notifyItemChanged(position)
                notifyDataSetChanged()
            },
            deleteListener = {
                notifyItemRemoved(position)
                notifyDataSetChanged()
            }))

        holder.frameView.setOnLongClickListener {
            // drag処理を許可
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.startDragAndDrop(
                    ClipData.newPlainText("info", JSONObject().apply {
                        put("id", holder.todoID ?: throw NullPointerException())
                    }.toString()),
                    View.DragShadowBuilder(it),
                    it,
                    0)
            } else {
                it.startDrag(null, View.DragShadowBuilder(it), it, 0)
            }
        }

        holder.frameView.setOnDragListener(View.OnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    if (event.result) {
                        notifyItemRemoved(holder.layoutPosition)
                        notifyDataSetChanged()
                    }
                    true
                }
                else -> false
            }
        })
    }


    // insert todo
    fun insert() {
        notifyItemInserted(0)
        notifyDataSetChanged()
    }
}