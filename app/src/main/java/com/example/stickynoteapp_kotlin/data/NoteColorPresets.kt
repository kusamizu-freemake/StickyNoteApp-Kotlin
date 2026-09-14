package com.example.stickynoteapp_kotlin.data

import androidx.compose.ui.graphics.Color

// 編集画面で選べる、付箋の色の候補（6色分）。
// idは色を区別するための固定番号。NoteEntity.colorIdとして保存される。
// 並び順が変わってもidは変わらないため、保存済みデータとずれない。
enum class PresetColor(val id: Int, val color: Color) {
    YELLOW(1, Color(0xFFFFF59D)), // 黄色（NoteEntityの初期値と同じ）
    BLUE(2, Color(0xFFB3E5FC)),   // 青
    GREEN(3, Color(0xFFC5E1A5)),  // 緑
    PINK(4, Color(0xFFF8BBD0)),   // ピンク
    PURPLE(5, Color(0xFFE1BEE7)), // 紫
    ORANGE(6, Color(0xFFFFE0B2)); // オレンジ
    // IDから色を取得するための処理
    companion object {
        // 保存したIDから色を取得する
        // IDが見つからない場合は黄色を使う。
        fun fromId(id: Int): PresetColor {
            return entries.find { it.id == id } ?: YELLOW
        }
    }
}