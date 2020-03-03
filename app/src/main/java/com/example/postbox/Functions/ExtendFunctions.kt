package com.example.postbox.Functions

import android.graphics.Point
import android.util.Log
import android.view.View

fun View.getLocationPointInWindow(): Point {
    val array = IntArray(2)
    this.getLocationInWindow(array)
    return Point(array[0], array[1])
}

fun View.getLocationPointOnScreen(): Point {
    val array = IntArray(2)
    this.getLocationOnScreen(array)
    return Point(array[0], array[1])
}
