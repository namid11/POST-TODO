package com.cyan_namid09.postbox.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmBroadcastReceiver(): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("[Debug]", "AlarmBroadcastReceiver onReceive")
    }
}