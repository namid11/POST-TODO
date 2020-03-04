package com.example.postbox.Service

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import com.example.postbox.Functions.diffDay
import com.example.postbox.Functions.getNowDate
import com.example.postbox.Functions.notifyTodoUpdate
import com.example.postbox.Functions.toCalendar
import com.example.postbox.Helper.DB_DEFAULT_STATE_TABLE
import com.example.postbox.Helper.TodoDataBaseOpenHelper

class NotificationJobService: JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Thread {
            Log.d("[DEBUG]", "onStartJob Notification")

            val dbHelper = TodoDataBaseOpenHelper(this)

            // --- TODOのstateを更新 --- //
            val dateDataPref = getSharedPreferences("todo_date_database", Context.MODE_PRIVATE)
            val currentTime = System.currentTimeMillis()
            val lastUpdateDate = dateDataPref.getLong("last_update_date", currentTime)
            if (lastUpdateDate == currentTime) dateDataPref.edit().putLong("last_update_date", currentTime).apply() // 値がセットされていなかったら、初期セット
            val diffDay = diffDay(toCalendar(lastUpdateDate), getNowDate())  // 最終更新からの日数の差
            when {
                diffDay == 0 -> {
                }
                diffDay == 1 -> {
                    dbHelper.operateOneDayUpdateState()
                    dbHelper.operateRemovingDoneTodo(DB_DEFAULT_STATE_TABLE["DONE"] ?: 0)
                    dateDataPref.edit().putLong("last_update_date", currentTime).apply()    // 最終更新日時を更新
                    notifyTodoUpdate(this)  // 通知処理
                }
                diffDay >= 2 -> {
                    dbHelper.operateAllYetUpdateState()
                    dbHelper.operateRemovingDoneTodo(DB_DEFAULT_STATE_TABLE["DONE"] ?: 0)
                    dateDataPref.edit().putLong("last_update_date", currentTime).apply()    // 最終更新日時を更新
                    notifyTodoUpdate(this)
                }
                else -> {

                }
            }
        }.start()
        return true
    }

}