package com.example.postbox.Functions

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.example.postbox.MainActivity
import com.example.postbox.R
import com.example.postbox.Static.NOTIFICATION_ID
import com.example.postbox.Static.NOTIFICATION_TODO_UPDATE_ID
import com.example.postbox.Static.NOTIFICATION_TODO_UPDATE_REQUEST_CODE
import java.util.*


// チャンネル作成（Android8以上）
fun makeNotificationChannel(context: Context) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(NOTIFICATION_TODO_UPDATE_ID, "TODOチェック", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "現在溜まっているTODOの確認"
            enableVibration(true)
            setShowBadge(true)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}


// 通知実行
fun notifyTodoUpdate(context: Context) {
    // PendingIntent作成
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_TODO_UPDATE_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT)

    // 通知のBuilderを作成

    val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(context, NOTIFICATION_TODO_UPDATE_ID) else NotificationCompat.Builder(context)
    // スタイル設定
//    NotificationCompat.InboxStyle(builder)
//        .setBigContentTitle("今日のTODO")
//        .addLine("TODO 1")
//        .addLine("TODO 2")
//        .setSummaryText("TODOが更新されました")
    val notification = builder
        .setContentTitle("TODO")
        .setContentText("更新されました")
        .setContentIntent(pendingIntent)     // タップしたときに起動するインテント
        .setSmallIcon(R.drawable.ic_icon)  // アイコン
        .setGroupSummary(false)
        .setAutoCancel(true)     // 通知をタップしたら、その通知を消す
        .build()

    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID.UPDATE.hashCode(), notification)

}