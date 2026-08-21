package com.example.stickynoteapp_kotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val text: String = "",
    val colorR: Int = 255,
    val colorG: Int = 245,
    val colorB: Int = 157,
    val isTop: Boolean = false,
    val isDeleted: Boolean = false,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)