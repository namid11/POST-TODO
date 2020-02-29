package com.example.postbox.Functions

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.postbox.BuildConfig
import com.example.postbox.Receiver.AlarmBroadcastReceiver
import java.util.*

fun alarmNotification(context: Context) {
    var alarmMgr: AlarmManager? = null
    lateinit var alarmIntent: PendingIntent

    alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmIntent = Intent(context, AlarmBroadcastReceiver::class.java).let { intent ->
        PendingIntent.getBroadcast(
            context,
            0,  // PendingIntentが２つ以上使う場合はこれで判別する
            intent,
            0)
    }


    val calendar: Calendar = Calendar.getInstance().apply {
        if (BuildConfig.DEBUG) {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 22)
        } else {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
        }

    }

    // setRepeating() lets you specify a precise custom interval--in this case,
    // 20 minutes.
    alarmMgr.setExact(
        AlarmManager.RTC_WAKEUP,    // スリープを解除して、処理を実行
        calendar.timeInMillis,
        alarmIntent
    )

}