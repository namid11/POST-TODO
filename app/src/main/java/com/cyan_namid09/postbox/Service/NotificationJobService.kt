package com.cyan_namid09.postbox.Service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.os.Handler
import android.util.Log
import com.cyan_namid09.postbox.BuildConfig
import com.cyan_namid09.postbox.Functions.diffDay
import com.cyan_namid09.postbox.Functions.getNowDate
import com.cyan_namid09.postbox.Functions.notifyTodoUpdate
import com.cyan_namid09.postbox.Functions.toCalendar
import com.cyan_namid09.postbox.Helper.DB_DEFAULT_STATE_TABLE
import com.cyan_namid09.postbox.Helper.TodoDataBaseOpenHelper

class NotificationJobService: JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        jobFinished(params, true)
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        val handler = Handler()
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
                    if (BuildConfig.DEBUG) {
                        dbHelper.operateOneDayUpdateState() // TOMORROW -> TODAY , TODAY -> YET
                        dbHelper.operateRemovingDoneTodo(DB_DEFAULT_STATE_TABLE["DONE"] ?: 0)   // DONE削除
                        dateDataPref.edit().putLong("last_update_date", currentTime).apply()    // 最終更新日時を更新
                        notifyTodoUpdate(this)  // 通知処理
                    }
                }
                diffDay == 1 -> {
                    dbHelper.operateOneDayUpdateState() // TOMORROW -> TODAY , TODAY -> YET
                    dbHelper.operateRemovingDoneTodo(DB_DEFAULT_STATE_TABLE["DONE"] ?: 0)   // DONE削除
                    dateDataPref.edit().putLong("last_update_date", currentTime).apply()    // 最終更新日時を更新
                    notifyTodoUpdate(this)  // 通知処理
                }
                diffDay >= 2 -> {
                    dbHelper.operateAllYetUpdateState() // TOMORROW -> YET , TODAY -> YET
                    dbHelper.operateRemovingDoneTodo(DB_DEFAULT_STATE_TABLE["DONE"] ?: 0)   // DONE削除
                    dateDataPref.edit().putLong("last_update_date", currentTime).apply()    // 最終更新日時を更新
                    notifyTodoUpdate(this)
                }
                else -> {

                }
            }

            jobFinished(params, false)
        }.start()
        return true
    }

}