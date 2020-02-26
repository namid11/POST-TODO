package com.example.postbox.Adapter

import android.content.Context
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.postbox.Helper.TodoDataBaseOpenHelper
import com.example.postbox.R
import com.example.postbox.Widget.PostboxViewDragListener

class PostboxListAdapter(private val context: Context, private val dbHelper: TodoDataBaseOpenHelper, private val targetState: Int): RecyclerView.Adapter<PostboxListAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val postboxImage: ImageView = view.findViewById(R.id.postbox_image)
        val postboxTitle: TextView = view.findViewById(R.id.postbox_title)
    }


    private val inflater = LayoutInflater.from(context)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.layout_postbox, parent, false)
        val viewHolder = PostboxListAdapter.ViewHolder(view)

        view.setOnClickListener {

        }


        // drag&drop時のイベント処理
        view.setOnDragListener(PostboxViewDragListener())

        return viewHolder
    }

    override fun getItemCount(): Int {
        val readDB = dbHelper.readableDatabase
        val cursor = readDB.query(
            "state",
            arrayOf("id", "name"),
            "id != ?",
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
        val readDB = dbHelper.readableDatabase
        val cursor = readDB.query(
            "state",
            arrayOf("id", "name"),
            "id != ?",
            arrayOf(targetState.toString()),
            null,
            null,
            null,
            null
        )
        cursor.move(position + 1) // sqliteは添字の最初は"1"らしいので、+1する
        holder.postboxTitle.text = cursor.getString(cursor.getColumnIndex("name"))

        cursor.close()
        readDB.close()
    }
}