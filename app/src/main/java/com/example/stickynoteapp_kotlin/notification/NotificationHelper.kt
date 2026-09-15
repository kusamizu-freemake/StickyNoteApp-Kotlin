package com.example.stickynoteapp_kotlin.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.stickynoteapp_kotlin.R
import android.util.Log // 確認後削除

// StickyNote用のNotification Channelを作成・登録するためのヘルパー
object NotificationHelper {

    // チャンネルID。一度登録した後にIDを変更すると別チャンネル扱いになるため注意。
    const val CHANNEL_ID = "sticky_note_channel"

    // Notification Channelを作成し、システムに登録する。
    // API 26未満は対象外（Channelの概念がないため）
    fun createNotificationChannel(context: Context) {
        Log.d("NotificationHelper", "createNotificationChannel called. SDK_INT=${Build.VERSION.SDK_INT}") // 確認後削除
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            Log.d("NotificationHelper", "Channel created: $CHANNEL_ID") // 確認後削除
        }
    }
}