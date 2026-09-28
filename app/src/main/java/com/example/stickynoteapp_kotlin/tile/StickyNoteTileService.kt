package com.example.stickynoteapp_kotlin.tile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.stickynoteapp_kotlin.MainActivity

// クイック設定パネルに並ぶ「付箋を開く」ボタン。タップするとアプリを開く。
class StickyNoteTileService : TileService() {

    companion object {
        // PendingIntentを識別するためのID。
        private const val OPEN_APP_REQUEST_CODE = 0
    }

    // タイルが表示されるたびに呼ばれる。
    // 起動ボタンとして使うため、状態は常に「オフ」表示にする。
    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        tile.state = Tile.STATE_INACTIVE
        tile.updateTile()
    }

    // タイルがタップされたときに呼ばれる。
    // Android 14以降と、それより前のバージョンで、アプリを開く方法が異なる。
    override fun onClick() {
        super.onClick()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            openAppWithPendingIntent()
        } else {
            openAppWithIntent()
        }
    }

    // Android 14以降：PendingIntent（「あとで実行してもらう依頼書」）を使ってアプリを開く。
    private fun openAppWithPendingIntent() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            OPEN_APP_REQUEST_CODE,
            createOpenAppIntent(),
            // UPDATE_CURRENT：同じPendingIntentがあれば内容を更新する。
            // IMMUTABLE：Android 12以降で必須の指定。
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        startActivityAndCollapse(pendingIntent)
    }

    // Android 13以前：Intentを直接渡してアプリを開く。
    // Android 14以降ではこの方法が使えなくなったため、上の関数と使い分けている。
    @Suppress("DEPRECATION")
    private fun openAppWithIntent() {
        startActivityAndCollapse(createOpenAppIntent())
    }

    // MainActivityを開くためのIntentを作成する。
    private fun createOpenAppIntent(): Intent {
        return Intent(this, MainActivity::class.java).apply {
            // アプリの外（クイック設定パネル）から開くためにNEW_TASKが必要。
            // 既存の画面があれば再利用して前面に表示する。
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
}
