package com.example.postbox.Widget

import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.ImageView
import com.example.postbox.Functions.convertDp2Px
import com.example.postbox.Functions.getLocationPointInWindow
import com.example.postbox.Helper.TodoDataBaseOpenHelper
import com.example.postbox.MyClass.PostboxView
import com.example.postbox.R
import org.json.JSONObject


class PostboxViewDragListener(private val context: Context): View.OnDragListener {

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
            postboxImage.setImageResource(R.drawable.ani_postbox_responce)
            val drawable = postboxImage.drawable as? AnimatedVectorDrawable
            if (drawable != null) {
                if (!drawable.isRunning) {
                    drawable.start()
                }
            } else {
                (postboxImage.drawable as Animatable).start()
            }
        }
    }

    private fun closePostAnimation(v: View?) {
        if (v != null) {
            val postboxImage: ImageView = v.findViewById(R.id.postbox_image)
            postboxImage.setImageResource(R.drawable.ani_postbox_responce_reverse)
            val drawable = postboxImage.drawable as? AnimatedVectorDrawable
            if (drawable != null) {
                if (!drawable.isRunning) {
                    drawable.start()
                }
            } else {
                (postboxImage.drawable as Animatable).start()
            }
        }
    }
}