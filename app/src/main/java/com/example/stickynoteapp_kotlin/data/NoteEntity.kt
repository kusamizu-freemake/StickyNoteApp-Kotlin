package com.example.stickynoteapp_kotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val text: String = "",
    // 色プリセットのID（PresetColor.id）。実際の色はPresetColor側で管理する。
    val colorId: Int = PresetColor.YELLOW.id,
    val isTop: Boolean = false,
    val isDeleted: Boolean = false,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)