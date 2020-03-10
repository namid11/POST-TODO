package com.cyan_namid09.postbox.Service

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import android.widget.Toast
import com.cyan_namid09.postbox.BuildConfig

class FirstSettingJobService(): JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        jobFinished(params, true)
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Thread {
            Log.d("[DEBUG]", "onStartJob")
            operateNotificationJobService(this)
            jobFinished(params, false)
        }.start()

        return true
    }

}