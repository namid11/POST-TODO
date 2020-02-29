package com.example.postbox.Functions

import android.icu.text.TimeZoneNames
import android.util.Log
import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

const val DEFAULT_UPDATE_TIME = 7

const val TIME_MILLISECOND = 1L
const val TIME_SECOND = TIME_MILLISECOND * 1000L
const val TIME_MINUTE = TIME_SECOND * 60L
const val TIME_HOUR = TIME_MINUTE * 60L
const val TIME_DAY = TIME_HOUR * 24L
const val TIME_WEEK = TIME_DAY * 7L


private val formatter = SimpleDateFormat("yyyy-MM-dd HH-mm-ss").apply {
    timeZone = TimeZone.getTimeZone("Asia/Tokyo")
}

fun toCalendar(mill: Long): Calendar {
    return Calendar.getInstance().apply {
        timeInMillis = mill
    }
}

//fun toCalendar(str: String): Calendar {
//    try {
//        if (formatter.parse(str) == null) return getNowDate()
//    } catch (e: ParseException) {
//        return getNowDate()
//    }
//
//    val date = formatter.parse(str)!!
//    return Calendar.getInstance().apply {
//        timeZone = TimeZone.getTimeZone("Asia/Tokyo")
//        time = date
//    }
//}

fun toString(calendar: Calendar): String {
    val date = calendar.time
    return formatter.format(date)
}

fun getNowDate(): Calendar {
    return toCalendar(System.currentTimeMillis())
}

fun getNowDateStr(): String {
    val calendar = getNowDate()
    return formatter.format(calendar.time)
}

fun diffDay(cal_before: Calendar, cal_after: Calendar): Int {
    Log.d("[Debug]", "after: %d, before: %d".format(cal_after.get(Calendar.HOUR_OF_DAY), cal_before.get(Calendar.HOUR_OF_DAY)))
    Log.d("[Debug]", "after: %d, before: %d".format(cal_after.time.time, cal_before.time.time))
    Log.d("[Debug]", "after: %s, before: %s".format(cal_after.time.toString(), cal_before.time.toString()))
    Log.d("[Debug]", "after: %f".format(((cal_after.time.time - cal_before.time.time).toDouble() / TIME_DAY.toDouble())))
    cal_after.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    cal_before.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diffDay = floor(((cal_after.time.time - cal_before.time.time).toDouble() / TIME_DAY.toDouble())).toInt()
    return diffDay
}