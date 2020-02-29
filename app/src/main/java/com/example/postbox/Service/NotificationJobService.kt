package com.example.postbox.Service

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import com.example.postbox.Functions.notifyTodoUpdate

class NotificationJobService: JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Thread {
            Log.d("[DEBUG]", "onStartJob Notification")
            notifyTodoUpdate(this)
        }.start()
        return true
    }

}