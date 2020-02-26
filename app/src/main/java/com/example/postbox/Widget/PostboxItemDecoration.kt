package com.example.postbox.Widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class PostboxItemDecoration(val space: Int): RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = space
        outRect.right = space
    }
}