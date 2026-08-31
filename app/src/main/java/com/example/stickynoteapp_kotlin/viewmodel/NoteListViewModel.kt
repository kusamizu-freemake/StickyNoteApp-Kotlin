package com.example.stickynoteapp_kotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stickynoteapp_kotlin.data.NoteEntity
import com.example.stickynoteapp_kotlin.data.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// NoteListViewModel は「一覧画面（NoteListScreen）」専用のViewModel
class NoteListViewModel(private val repository: NoteRepository) : ViewModel() {

    companion object {
        // 画面が非表示になってからも、この時間（ミリ秒）はデータの収集を続ける。
        // 画面回転などで一瞬だけ購読が途切れても、すぐに再取得し直さずに済むようにするための待ち時間。
        private const val SUBSCRIPTION_TIMEOUT_MS = 5000L
    }

    // 画面に表示する付箋の一覧
    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = emptyList()
        )

    // 削除された付箋を元に戻します。
    fun restoreNote(id: Long) {
        viewModelScope.launch {
            repository.restore(id)
        }
    }
}

// Repositoryを渡してNoteListViewModelを作成する
class NoteListViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}