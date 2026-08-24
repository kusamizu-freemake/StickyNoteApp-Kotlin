package com.example.stickynoteapp_kotlin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteDao {

    // 新しい付箋を1件保存します
    @Insert
    suspend fun insert(note: NoteEntity): Long

    // 既存の付箋を更新（上書き保存）します。
    @Update
    suspend fun update(note: NoteEntity)

    // 削除されていない（isDeleted = 0）付箋を一覧表示用に取得します。
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isTop DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    // id を指定して1件だけ取得します。
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    // 物理的には消さず、isDeleted フラグだけを立てる「論理削除」です（フェーズ2で使用）。
    @Query("UPDATE notes SET isDeleted = 1 WHERE id = :id")
    suspend fun delete(id: Long)
}