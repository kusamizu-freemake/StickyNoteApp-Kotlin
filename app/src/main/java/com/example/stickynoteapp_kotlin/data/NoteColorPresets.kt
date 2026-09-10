package com.example.stickynoteapp_kotlin.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

// 編集画面で選べる、付箋の色の候補（6色分）。
// idは色を区別するための固定番号（保存対応Issueで使用予定）。
// 並び順が変わってもidは変わらないため、保存済みデータとずれない。
enum class PresetColor(val id: Int, val color: Color) {
    YELLOW(1, Color(0xFFFFF59D)), // 黄色（NoteEntityの初期値と同じ）
    BLUE(2, Color(0xFFB3E5FC)),   // 青
    GREEN(3, Color(0xFFC5E1A5)),  // 緑
    PINK(4, Color(0xFFF8BBD0)),   // ピンク
    PURPLE(5, Color(0xFFE1BEE7)), // 紫
    ORANGE(6, Color(0xFFFFE0B2))  // オレンジ
}

// PresetColorの色を、DB保存用の赤緑青（0〜255）の数値に変換する
fun PresetColor.toRgbInts(): Triple<Int, Int, Int> {
    val argb = color.toArgb()
    return Triple(
        android.graphics.Color.red(argb),
        android.graphics.Color.green(argb),
        android.graphics.Color.blue(argb)
    )
}