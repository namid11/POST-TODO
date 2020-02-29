package com.example.postbox.Service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.util.Log
import com.example.postbox.Functions.notifyTodoUpdate

class FirstSettingJobService(): JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Thread {
            Log.d("[DEBUG]", "onStartJob")
            operateNotificationJobService(this)
        }.start()

        return true
    }
}