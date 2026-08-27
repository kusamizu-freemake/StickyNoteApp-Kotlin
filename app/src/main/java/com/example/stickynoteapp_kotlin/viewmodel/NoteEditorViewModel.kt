package com.example.stickynoteapp_kotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stickynoteapp_kotlin.data.NoteEntity
import com.example.stickynoteapp_kotlin.data.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// NoteEditorViewModel は「編集画面（NoteEditorScreen）」専用のViewModel
class NoteEditorViewModel(private val repository: NoteRepository) : ViewModel() {

    companion object {
        // 自動保存を実行するまでの待機時間（最後の入力からこの時間だけ操作がなければ保存する）
        private const val AUTO_SAVE_DEBOUNCE_MS = 500L
    }

    // 自動保存の待機中コルーチンを覚えておくための変数。
    // 新しい入力があったときに、前の待機をキャンセルするために使用する。
    private var autoSaveJob: Job? = null

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

    // 付箋を論理削除（DBから完全に消すのではなく、isDeleted フラグを立てる）。
    // id が 0（＝まだ一度も保存されていない付箋）の場合、DBには存在しないため何も起こらない。
    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.delete(note.id)
        }
    }

    // テキストが変更されるたびに画面から呼び出す関数。
    // 既存の付箋のみ、入力が止まってから一定時間後に自動保存する。
    // 新規付箋はまだ一度も保存されていないため、自動保存の対象外とする。
    fun onTextChanged(note: NoteEntity, text: String) {
        // 前回の待機処理が残っていればキャンセルし、待ち直す（＝debounce）
        autoSaveJob?.cancel()

        if (note.id == 0L) {
            return
        }

        autoSaveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DEBOUNCE_MS)
            val toSave = note.copy(text = text, updatedAt = System.currentTimeMillis())
            repository.update(toSave)
        }
    }

    // 手動保存（保存ボタン・戻るボタン）が実行される直前に呼び出す。
    // 待機中の自動保存があればキャンセルし、手動保存のみが実行されるようにする。
    fun cancelPendingAutoSave() {
        autoSaveJob?.cancel()
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