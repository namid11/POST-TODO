package com.example.postbox.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class AlarmBroadcastReceiver(): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("[Debug]", "AlarmBroadcastReceiver onReceive")
    }
}