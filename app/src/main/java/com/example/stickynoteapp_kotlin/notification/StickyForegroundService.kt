package com.example.stickynoteapp_kotlin.notification

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.stickynoteapp_kotlin.MainActivity
import com.example.stickynoteapp_kotlin.R

// 常駐通知を表示するフォアグラウンドサービス。
class StickyForegroundService : Service() {

    companion object {
        // 常駐通知を識別するID。
        private const val NOTIFICATION_ID = 1

        // PendingIntentを識別するためのID。
        private const val CONTENT_INTENT_REQUEST_CODE = 0
    }

    // 画面とのデータ連携は行わないため、nullを返す。
    override fun onBind(intent: Intent?): IBinder? = null

    // サービス開始時に常駐通知を表示する。
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())

        // OSにサービスを強制終了された場合、再起動を試みる。
        return START_STICKY
    }

    // 常駐通知を作成する。
    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            // TODO: 専用アイコン作成後に変更する。
            .setSmallIcon(R.mipmap.ic_launcher)
            // 通知タップ時にアプリを開く。
            .setContentIntent(buildContentIntent())
            // 常駐通知として、通常は消えないようにする。
            .setOngoing(true)
            .build()
    }

    // 通知タップ時にMainActivityを開くためのPendingIntentを作成する。
    private fun buildContentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            // 通知（アプリの外）から開くためにNEW_TASKが必要。
            // 既存の画面があれば再利用して前面に表示する。
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            this,
            CONTENT_INTENT_REQUEST_CODE,
            intent,
            // UPDATE_CURRENT：同じPendingIntentがあれば内容を更新する。
            // IMMUTABLE：Android 12以降で必須の指定。
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}