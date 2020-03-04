package com.example.postbox.Adapter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Point
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.postbox.Helper.DB_DEFAULT_STATE_TABLE
import com.example.postbox.MyClass.PostboxView
import com.example.postbox.Helper.TodoDataBaseOpenHelper
import com.example.postbox.MainActivity
import com.example.postbox.R
import com.example.postbox.Widget.PostboxViewDragListener

class PostboxListAdapter(private val context: Context, private val dbHelper: TodoDataBaseOpenHelper, private var targetState: Int):
    RecyclerView.Adapter<PostboxListAdapter.ViewHolder>(), PostboxViewDragListener.AnimateListener {

    var listener: ItemDragListener? = null

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val postboxImage: ImageView = view.findViewById(R.id.postbox_image)
        val postboxTitle: TextView = view.findViewById(R.id.postbox_title)
    }

    interface ItemDragListener {
        fun callPostAnimation(adapter: RecyclerView.Adapter<PostboxListAdapter.ViewHolder>, view: View, start: Point, end: Point): Unit
    }


    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.layout_postbox, parent, false)
        val viewHolder = PostboxListAdapter.ViewHolder(view)


        // drag&drop時のイベント処理
        val postboxViewDragListener = PostboxViewDragListener(context)
        postboxViewDragListener.listener = this
        view.setOnDragListener(postboxViewDragListener)

        return viewHolder
    }

    override fun getItemCount(): Int {
        var count = 0
        dbHelper.readState(
            where = "id not in (?, ?)",
            whereValue = arrayOf(DB_DEFAULT_STATE_TABLE["DONE"].toString() ?: "0", DB_DEFAULT_STATE_TABLE["YET"].toString() ?: "0"))
        { cursor: Cursor ->
            count = cursor.count
        }
        return count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        dbHelper.readState(
            where = "id not in (?, ?)",
            whereValue = arrayOf(DB_DEFAULT_STATE_TABLE["DONE"].toString() ?: "0", DB_DEFAULT_STATE_TABLE["YET"].toString() ?: "0"))
        { cursor: Cursor ->
            cursor.moveToPosition(position)

            val stateId = cursor.getInt(cursor.getColumnIndex("id"))
            val name = cursor.getString(cursor.getColumnIndex("name"))
            holder.view.tag = PostboxView.PostboxData(stateID = stateId, name = name)
            holder.postboxTitle.text = name

            if (stateId == targetState) {
                holder.view.setBackgroundResource(R.drawable.theme_postbox_field_active)
                holder.postboxTitle.setTextColor(context.resources.getColor(R.color.colorAccent))
            } else {
                holder.view.setBackgroundResource(R.drawable.theme_postbox_field)
                holder.postboxTitle.setTextColor(context.resources.getColor(R.color.colorWhite))
            }

            holder.view.setOnClickListener {
                (context as? MainActivity)?.updatePreview(stateId)
            }
        }

        (holder.postboxImage.drawable as Animatable).stop()
        holder.postboxImage.setImageResource(R.drawable.ani_postbox_responce)
    }

    override fun postAnimation(view: View, start: Point, end: Point) {
        if (listener != null) listener!!.callPostAnimation(this, view, start, end)
    }


    // this function is called when state of adapter change
    fun changeState(stateId: Int) {
        targetState = stateId
        this.notifyDataSetChanged()
    }
}