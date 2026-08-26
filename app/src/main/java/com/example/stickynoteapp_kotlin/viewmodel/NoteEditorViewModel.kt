package com.example.stickynoteapp_kotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stickynoteapp_kotlin.data.NoteEntity
import com.example.stickynoteapp_kotlin.data.NoteRepository
import kotlinx.coroutines.launch

// NoteEditorViewModel は「編集画面（NoteEditorScreen）」専用のViewModel
class NoteEditorViewModel(private val repository: NoteRepository) : ViewModel() {

    // 指定した id の付箋を1件取得します。編集画面を開くときに呼び出し
    suspend fun getNoteById(id: Long): NoteEntity? = repository.getById(id)

    // 付箋を保存します。
    // id が 0（＝まだ一度も保存されていない新規付箋）なら新規追加（insert）
    // それ以外（＝既存の付箋）なら上書き更新（update）
    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.insert(note)
            } else {
                repository.update(note)
            }
        }
    }
}

// Repositoryを渡してNoteEditorViewModelを作成する
class NoteEditorViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteEditorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
