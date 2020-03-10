package com.cyan_namid09.postbox.Functions

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