package com.example.stickynoteapp_kotlin.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.stickynoteapp_kotlin.R

// StickyNote用のNotification Channelを作成・登録するためのヘルパー
object NotificationHelper {

    // チャンネルID。一度登録した後にIDを変更すると別チャンネル扱いになるため注意。
    const val CHANNEL_ID = "sticky_note_channel"

    // Notification Channelを作成し、システムに登録する。
    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}