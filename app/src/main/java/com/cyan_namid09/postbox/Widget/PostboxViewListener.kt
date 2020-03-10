package com.cyan_namid09.postbox.Widget

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Point
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.cyan_namid09.postbox.Adapter.PostboxListAdapter
import com.cyan_namid09.postbox.Functions.convertDp2Px
import com.cyan_namid09.postbox.Functions.getLocationPointInWindow
import com.cyan_namid09.postbox.Helper.TodoDataBaseOpenHelper
import com.cyan_namid09.postbox.MyClass.PostboxView
import com.cyan_namid09.postbox.R
import org.json.JSONObject
import java.lang.Exception


class PostboxViewDragListener(
    private val context: Context,
    private val openDrawable: Int = R.drawable.ani_postbox_responce,
    private val closeDrawable: Int = R.drawable.ani_postbox_responce_reverse): View.OnDragListener {

    private val dbHelper = TodoDataBaseOpenHelper(context)

    var listener: AnimateListener? = null

    interface AnimateListener {
        fun postAnimation(view: View, start: Point, end: Point): Unit
    }

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        when (event?.action) {
            DragEvent.ACTION_DRAG_ENTERED -> {
                // ENTERED
                openPostAnimation(v)
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                // EXITED
                closePostAnimation(v)
            }
            DragEvent.ACTION_DROP -> {
                // DROP
                closePostAnimation(v)

                // dropされたtodoのstateを更新
                val clipJSON = JSONObject(event.clipData.getItemAt(0).text.toString())
                val id = clipJSON.getInt("id")
                val location = clipJSON.getJSONObject("location")
                if (listener != null && v != null) {
                    val point = v.getLocationPointInWindow()
                    point.x = (point.x.toFloat() + (v.width.toFloat() - context.resources.getDimension(R.dimen.post_image_width)) / 2f).toInt()
                    point.y = (point.y.toFloat() + (v.height.toFloat() - context.resources.getDimension(R.dimen.post_image_height)) / 2f).toInt() + convertDp2Px(8f, context).toInt()
                    listener!!.postAnimation(
                        view = v,
                        start = Point().apply {
                            x = location.getInt("x")
                            y = location.getInt("y")
                        },
                        end = point
                    )
                }

                dbHelper.updateTodo(id, state = (v?.tag as PostboxView.PostboxData).stateID)
            }
        }

        return true
    }


    private fun openPostAnimation(v: View?) {
        if (v != null) {
            val postboxImage: ImageView = v.findViewById(R.id.postbox_image)
            postboxImage.setImageResource(this.openDrawable)
            (postboxImage.drawable as Animatable).start()
        }
    }

    private fun closePostAnimation(v: View?) {
        if (v != null) {
            val postboxImage: ImageView = v.findViewById(R.id.postbox_image)
            try {
                postboxImage.setImageResource(this.closeDrawable)
                (postboxImage.drawable as Animatable).start()
            } catch (e: Exception) {
                Log.d("[ERROR]", "on closePostAnimation. message: %s".format(e.message))
                postboxImage.setImageResource(this.openDrawable)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    (postboxImage.drawable as AnimatedVectorDrawable).reset()
                }
            }
        }
    }
}



class PostboxOnLongClickListener(val context:Context, val stateId: Int, val updateCallback: () -> Unit, val deleteCallback: () -> Unit): View.OnLongClickListener {

    private val dbHelper = TodoDataBaseOpenHelper(context)

    override fun onLongClick(v: View?): Boolean {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_state, null)
        val dialogBuilder = AlertDialog.Builder(context).apply {
            setView(dialogView) // Viewをセット
            setIcon(R.mipmap.ic_launcher) // setTitle()をしないと出ない？
            setCancelable(true) // dialog以外をタップしたら、消える
        }
        val alertDialog = dialogBuilder.show()// show

        val editText = dialogView.findViewById<EditText>(R.id.edit_state_edit_text)
        val updateButton = dialogView.findViewById<Button>(R.id.edit_state_update_button)
        val deleteButton = dialogView.findViewById<ImageView>(R.id.edit_state_delete_button)

        dbHelper.readState(where = "id == ?", whereValue = arrayOf(stateId.toString())) {
            it.moveToFirst()
            editText.setText(it.getString(it.getColumnIndex("name")), TextView.BufferType.NORMAL)

            updateButton.setOnClickListener {
                if (editText.text.toString() != "") {
                    dbHelper.updateState(id = stateId, name = editText.text.toString())
                    alertDialog.dismiss()

                    updateCallback()
                } else {
                    Toast.makeText(context, "Postbox name を入力してください", Toast.LENGTH_SHORT).show()
                }
            }

            deleteButton.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(context).apply {
                    setIcon(R.mipmap.ic_launcher) // setTitle()をしないと出ない？
                    setTitle("ATTENTION")
                    setMessage("このポスト内のタスクも削除されます")
                    setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        dbHelper.deleteState(id = stateId)
                        dbHelper.deleteTodoOnState(stateId = stateId)
                        deleteCallback()
                        alertDialog.dismiss()
                    })
                    setNegativeButton("CANCEL", null)
                    setCancelable(true) // dialog以外をタップしたら、消える
                }
                dialogBuilder.show()
            }
        }

        return true
    }

}