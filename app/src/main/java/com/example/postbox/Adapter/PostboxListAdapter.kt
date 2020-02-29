package com.example.postbox.Adapter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.postbox.ExtendClass.PostboxView
import com.example.postbox.Helper.TodoDataBaseOpenHelper
import com.example.postbox.MainActivity
import com.example.postbox.R
import com.example.postbox.Widget.PostboxViewDragListener

class PostboxListAdapter(private val context: Context, private val dbHelper: TodoDataBaseOpenHelper, private val targetState: Int): RecyclerView.Adapter<PostboxListAdapter.ViewHolder>() {

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val postboxImage: ImageView = view.findViewById(R.id.postbox_image)
        val postboxTitle: TextView = view.findViewById(R.id.postbox_title)
    }


    private val inflater = LayoutInflater.from(context)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.layout_postbox, parent, false)
        val viewHolder = PostboxListAdapter.ViewHolder(view)


        // drag&drop時のイベント処理
        view.setOnDragListener(PostboxViewDragListener(context))

        return viewHolder
    }

    override fun getItemCount(): Int {
        var count = 0
        dbHelper.readState(where = "id != ?", whereValue = arrayOf(targetState.toString())) {cursor: Cursor ->
            count = cursor.count
        }
        return count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        dbHelper.readState(where = "id != ?", whereValue = arrayOf(targetState.toString())) {cursor: Cursor ->
            cursor.moveToPosition(position)

            val name = cursor.getString(cursor.getColumnIndex("name"))
            holder.view.tag = PostboxView.PostboxData(stateID = cursor.getInt(cursor.getColumnIndex("id")), name = name)
            holder.postboxTitle.text = name

            holder.view.setOnClickListener {
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("state_id", (it.tag as PostboxView.PostboxData).stateID)
                intent.putExtra("state_name", (it.tag as PostboxView.PostboxData).name)
                context.startActivity(intent)
            }
        }
    }
}