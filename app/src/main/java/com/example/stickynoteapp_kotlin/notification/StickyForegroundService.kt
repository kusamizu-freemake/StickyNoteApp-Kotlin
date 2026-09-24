package com.example.stickynoteapp_kotlin.notification

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.stickynoteapp_kotlin.R

// アプリを閉じていても動き続け、常駐通知を出し続けるためのサービス。
class StickyForegroundService : Service() {

    companion object {
        // 通知を区別するための番号。同じ番号で出すと古い通知を上書きする。
        private const val NOTIFICATION_ID = 1
    }

    // 今回は画面と直接データをやり取りしないため、何も返さない。
    override fun onBind(intent: Intent?): IBinder? = null

    // サービスが起動されたときに呼ばれ、常駐通知を表示する。
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        // OSに終了させられても、システムが再起動を試みるようにする指定。
        return START_STICKY
    }

    // 常駐通知の中身を組み立てる。
    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            // TODO: 専用の通知アイコンを用意したら差し替える（現在はランチャーアイコンを暫定使用）
            .setSmallIcon(R.mipmap.ic_launcher)
            // スワイプで消せないようにする（常駐通知のため）
            .setOngoing(true)
            .build()
    }
}