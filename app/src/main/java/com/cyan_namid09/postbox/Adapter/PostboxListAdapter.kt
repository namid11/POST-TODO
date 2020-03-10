package com.cyan_namid09.postbox.Adapter

import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.graphics.Point
import android.graphics.drawable.Animatable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.cyan_namid09.postbox.Helper.DB_DEFAULT_STATE_TABLE
import com.cyan_namid09.postbox.MyClass.PostboxView
import com.cyan_namid09.postbox.Helper.TodoDataBaseOpenHelper
import com.cyan_namid09.postbox.MainActivity
import com.cyan_namid09.postbox.R
import com.cyan_namid09.postbox.Widget.PostboxOnLongClickListener
import com.cyan_namid09.postbox.Widget.PostboxViewDragListener

class PostboxListAdapter(
    private val context: Context,
    private val dbHelper: TodoDataBaseOpenHelper,
    private var targetState: Int
) :
    RecyclerView.Adapter<PostboxListAdapter.ViewHolder>(), PostboxViewDragListener.AnimateListener {

    enum class TYPE {
        CONTENT, ADD
    }

    var listener: ItemDragListener? = null
    private val inflater = LayoutInflater.from(context)

    open class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var type: Int = 0
    }

    class PostViewHolder(view: View) : ViewHolder(view) {
        val postboxImage: ImageView = view.findViewById(R.id.postbox_image)
        val postboxTitle: TextView = view.findViewById(R.id.postbox_title)
    }

    class AddViewHolder(view: View) : ViewHolder(view) {
        val addButton: ImageButton = view.findViewById(R.id.add_button)
    }

    interface ItemDragListener {
        fun callPostAnimation(
            adapter: RecyclerView.Adapter<PostboxListAdapter.ViewHolder>,
            view: View,
            start: Point,
            end: Point
        ): Unit
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == TYPE.CONTENT.hashCode())
            inflater.inflate(R.layout.layout_postbox, parent, false)
        else
            inflater.inflate(R.layout.layout_postbox_added, parent, false)

        return if (viewType == TYPE.CONTENT.hashCode()) {
            val viewHolder = PostboxListAdapter.PostViewHolder(view).apply {
                type = viewType
            }
            // drag&drop時のイベント処理
            val postboxViewDragListener = PostboxViewDragListener(context)
            postboxViewDragListener.listener = this
            view.setOnDragListener(postboxViewDragListener)

            viewHolder
        } else {
            val viewHolder = PostboxListAdapter.AddViewHolder(view).apply {
                type = viewType
            }

            viewHolder
        }
    }

    override fun getItemCount(): Int {
        var count = 0
        dbHelper.readState(
            where = "id not in (?, ?)",
            whereValue = arrayOf(
                DB_DEFAULT_STATE_TABLE["DONE"].toString() ?: "0",
                DB_DEFAULT_STATE_TABLE["YET"].toString() ?: "0"
            )
        )
        { cursor: Cursor ->
            count = cursor.count + 1
        }
        return count
    }

    override fun getItemViewType(position: Int): Int {
        val count = this.itemCount
        return if (position == count - 1) {
            TYPE.ADD.hashCode()
        } else {
            TYPE.CONTENT.hashCode()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.type == TYPE.CONTENT.hashCode()) {
            // --- POSTBOX --- //

            // drag&drop時のイベント処理
            val postboxViewDragListener = PostboxViewDragListener(context)
            postboxViewDragListener.listener = this
            holder.view.setOnDragListener(postboxViewDragListener)

            val holder = holder as PostViewHolder
            dbHelper.readState(
                where = "id not in (?, ?)",
                whereValue = arrayOf(
                    DB_DEFAULT_STATE_TABLE["DONE"].toString() ?: "0",
                    DB_DEFAULT_STATE_TABLE["YET"].toString() ?: "0"
                )
            )
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

                if (stateId != DB_DEFAULT_STATE_TABLE["TODAY"] && stateId != DB_DEFAULT_STATE_TABLE["TOMORROW"]) {
                    holder.view.setOnLongClickListener(
                        PostboxOnLongClickListener(
                            context,
                            stateId = stateId,
                            updateCallback = {
                                this.notifyItemChanged(position)
                                this.notifyDataSetChanged()
                            },
                            deleteCallback = {
                                this.notifyItemRemoved(position)
                                this.notifyDataSetChanged()
                            })
                    )
                }

                holder.view.setOnClickListener {
                    (context as? MainActivity)?.updatePreview(stateId)
                }
            }

            (holder.postboxImage.drawable as Animatable).stop()
            holder.postboxImage.setImageResource(R.drawable.ani_postbox_responce)
        } else {
            // --- Add BUTTON --- //

            val holder = holder as AddViewHolder
            holder.addButton.setOnClickListener {
                val dialogView =
                    LayoutInflater.from(context).inflate(R.layout.dialog_create_state, null).apply {
                    }
                val dialogBuilder = AlertDialog.Builder(context).apply {
                    setView(dialogView) // Viewをセット
                    setIcon(R.mipmap.ic_launcher)
                    setCancelable(true)
                }
                val alertDialog = dialogBuilder.show()// show
                dialogView.findViewById<Button>(R.id.add_button).setOnClickListener {
                    val editText: EditText = dialogView.findViewById(R.id.create_state_edit_text)
                    if (editText.text.toString() != "") {
                        // State追加
                        dbHelper.insertState(name = editText.text.toString()) {
                            dbHelper.readState {
                                this.notifyItemInserted(it.count - 2)
                                this.notifyDataSetChanged()
                            }
                        }
                        alertDialog.dismiss()
                    } else {
                        Toast.makeText(context, "ポスト名を入力してください", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


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