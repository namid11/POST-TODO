package com.cyan_namid09.postbox.Functions

import android.content.Context
import android.util.DisplayMetrics


/**
 * dpからpixelへの変換
 * @param dp
 * @param context
 * @return float pixel
 */
fun convertDp2Px(dp: Float, context: Context): Float {
    val metrics: DisplayMetrics = context.getResources().getDisplayMetrics()
    return dp * metrics.density
}

/**
 * pixelからdpへの変換
 * @param px
 * @param context
 * @return float dp
 */
fun convertPx2Dp(px: Int, context: Context): Float {
    val metrics: DisplayMetrics = context.getResources().getDisplayMetrics()
    return px / metrics.density
}