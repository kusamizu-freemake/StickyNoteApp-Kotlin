package com.example.stickynoteapp_kotlin.data

import kotlinx.coroutines.flow.Flow

// Repository（リポジトリ）は、ViewModel と DAO（データベース）の間に立つ「窓口」役です。

class NoteRepository(private val noteDao: NoteDao) {

    // 削除されていない付箋の一覧（データが変わると自動で更新される）。
    val allActiveNotes: Flow<List<NoteEntity>> = noteDao.queryAllActive()

    suspend fun insert(note: NoteEntity): Long = noteDao.insert(note)

    suspend fun update(note: NoteEntity) = noteDao.update(note)

    suspend fun getById(id: Long): NoteEntity? = noteDao.getById(id)

    suspend fun markDeleted(id: Long) = noteDao.markDeleted(id)
}