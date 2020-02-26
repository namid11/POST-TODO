package com.example.postbox.Widget

import android.graphics.Color
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.example.postbox.R

class PostboxViewDragListener(): View.OnDragListener {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        when (event?.action) {
            DragEvent.ACTION_DRAG_ENTERED -> {
                // ENTERED
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
            DragEvent.ACTION_DRAG_EXITED -> {
                // EXITED
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
            DragEvent.ACTION_DROP -> {
                // DROP
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

        return true
    }
}