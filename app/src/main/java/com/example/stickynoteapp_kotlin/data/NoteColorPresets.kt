package com.example.stickynoteapp_kotlin.data

// 編集画面で選べる、付箋の色の候補（6色分）。
// NoteEntityのcolorR/G/Bと同じ数値の組み合わせで色を表す。
data class ColorPreset(
    val colorR: Int,
    val colorG: Int,
    val colorB: Int
)

val NoteColorPresets = listOf(
    ColorPreset(255, 245, 157), // 黄色（NoteEntityの初期値と同じ）
    ColorPreset(179, 229, 252), // 青
    ColorPreset(197, 225, 165), // 緑
    ColorPreset(248, 187, 208), // ピンク
    ColorPreset(225, 190, 231), // 紫
    ColorPreset(255, 224, 178)  // オレンジ
)