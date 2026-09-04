package com.example.stickynoteapp_kotlin.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stickynoteapp_kotlin.data.ImageStorage
import com.example.stickynoteapp_kotlin.data.NoteEntity
import com.example.stickynoteapp_kotlin.data.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

// NoteEditorViewModel は「編集画面（NoteEditorScreen）」専用のViewModel
// 画像保存にアプリのContextが必要なため、AndroidViewModelを使用する
class NoteEditorViewModel(
    application: Application,
    private val repository: NoteRepository
) : AndroidViewModel(application) {

    companion object {
        // 自動保存を実行するまでの待機時間（最後の入力からこの時間だけ操作がなければ保存する）
        private const val AUTO_SAVE_DEBOUNCE_MS = 500L
    }

    // 画像のアプリ内部への保存をImageStorageに任せる
    private val imageStorage = ImageStorage(application)

    // 自動保存の待機中コルーチンを覚えておくための変数。
    // 新しい入力があったときに、前の待機をキャンセルするために使用する。
    private var autoSaveJob: Job? = null

    // 画像の保存に失敗したことを画面に伝えるためのフラグ。
    private val _imageSaveError = MutableStateFlow(false)
    val imageSaveError: StateFlow<Boolean> = _imageSaveError

    // 指定した id の付箋を1件取得します。編集画面を開くときに呼び出し
    suspend fun getNoteById(id: Long): NoteEntity? = repository.getById(id)

    // 付箋を保存します。
    // 画像が指定されていれば、先にアプリ内部へコピーして保存する
    // コピーに失敗した場合は、付箋の保存も中止する
    fun saveNote(note: NoteEntity, imageUri: Uri?) {
        viewModelScope.launch {
            var toSave = note

            if (imageUri != null) {
                try {
                    val path = imageStorage.copyToInternalStorage(imageUri)
                    toSave = toSave.copy(imagePath = path)
                } catch (e: IOException) {
                    Log.e("NoteEditorViewModel", "画像の保存に失敗しました", e)
                    _imageSaveError.value = true
                    return@launch
                }
            }

            if (toSave.id == 0L) {
                repository.insert(toSave)
            } else {
                repository.update(toSave)
            }
        }
    }

    // エラーメッセージを表示した後、エラー状態をリセットする
    fun onImageSaveErrorShown() {
        _imageSaveError.value = false
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

// ApplicationとRepositoryを渡してViewModelを作成する
class NoteEditorViewModelFactory(
    private val application: Application,
    private val repository: NoteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteEditorViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}