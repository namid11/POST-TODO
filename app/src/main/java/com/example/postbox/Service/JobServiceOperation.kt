package com.example.postbox.Service

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import com.example.postbox.BuildConfig
import com.example.postbox.Functions.DEFAULT_UPDATE_TIME
import java.util.*
import java.util.concurrent.TimeUnit

fun operateFirstJobService(context: Context) {
    val operateTime = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        if (BuildConfig.DEBUG) {
            add(Calendar.MINUTE, 1)
        } else {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, DEFAULT_UPDATE_TIME)
            set(Calendar.MINUTE, 0)
        }
    }
    val jobInfo = JobInfo.Builder(1, ComponentName(context, FirstSettingJobService::class.java)).apply {
        setMinimumLatency(operateTime.timeInMillis - System.currentTimeMillis())
//        setOverrideDeadline(operateTime.timeInMillis - System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10))
    }.build()

    val scheduler = ContextCompat.getSystemService(context, JobScheduler::class.java)
    scheduler?.schedule(jobInfo)
}


fun operateNotificationJobService(context: Context) {
    val jobInfo = JobInfo.Builder(2, ComponentName(context, NotificationJobService::class.java)).apply {
        if (BuildConfig.DEBUG) {
            setPeriodic(TimeUnit.MINUTES.toMillis(15))
        } else {
            setPeriodic(TimeUnit.DAYS.toMillis(1))
        }

        setPersisted(true)      // 再起動しても有効
    }.build()

    val scheduler = ContextCompat.getSystemService(context, JobScheduler::class.java)
    scheduler?.schedule(jobInfo)
}