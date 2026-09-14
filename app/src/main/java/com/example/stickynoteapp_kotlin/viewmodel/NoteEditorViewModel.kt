package com.example.stickynoteapp_kotlin.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stickynoteapp_kotlin.data.NoteEntity
import com.example.stickynoteapp_kotlin.data.NoteRepository
import com.example.stickynoteapp_kotlin.usecase.SaveImageUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// NoteEditorViewModel は「編集画面（NoteEditorScreen）」専用のViewModel
// 画像保存でアプリのContextを使うためAndroidViewModelを使用する
class NoteEditorViewModel(
    application: Application,
    private val repository: NoteRepository,
    private val saveImageUseCase: SaveImageUseCase
) : AndroidViewModel(application) {

    companion object {
        // 自動保存を実行するまでの待機時間（最後の操作から500ms後に自動保存する）
        private const val AUTO_SAVE_DEBOUNCE_MS = 500L
    }

    // テキストの自動保存を管理する
    // 新しいテキスト入力があったときに、前の待機をキャンセルするために使用する。
    private var textAutoSaveJob: Job? = null

    // 色の自動保存を管理する
    // テキストと色を別々に管理し、互いの保存をキャンセルしないようにする。
    private var colorAutoSaveJob: Job? = null

    // 画像の保存に失敗したことを画面に伝えるためのフラグ。
    private val _imageSaveError = MutableStateFlow(false)
    val imageSaveError: StateFlow<Boolean> = _imageSaveError

    // 指定した id の付箋を1件取得します。編集画面を開くときに呼び出し
    suspend fun getNoteById(id: Long): NoteEntity? = repository.getById(id)

    // 付箋を保存します。
    // 画像が選択されていれば、先に内部ストレージへ保存する
    // 画像のコピーに失敗した場合は、テキストを含め保存自体を中止する。
    // id が 0（＝まだ一度も保存されていない新規付箋）なら新規追加（insert）
    // それ以外（＝既存の付箋）なら上書き更新（update）
    fun saveNote(note: NoteEntity, imageUri: Uri?) {
        viewModelScope.launch {
            if (imageUri == null) {
                saveNoteEntity(note)
                return@launch
            }

            saveImageUseCase(imageUri)
                .onSuccess { path ->
                    saveNoteEntity(note.copy(imagePath = path))
                }
                .onFailure { e ->
                    Log.e("NoteEditorViewModel", "画像の保存に失敗しました", e)
                    _imageSaveError.value = true
                }
        }
    }

    // 付箋（NoteEntity）を保存する。新規なら追加、既存なら上書き更新する。
    private suspend fun saveNoteEntity(note: NoteEntity) {
        if (note.id == 0L) {
            repository.insert(note)
        } else {
            repository.update(note)
        }
    }

    // Snackbarでエラーを表示し終えたら、画面から呼び出してフラグをリセットする。
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
        textAutoSaveJob?.cancel()

        if (note.id == 0L) {
            return
        }

        textAutoSaveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DEBOUNCE_MS)
            val toSave = note.copy(text = text, updatedAt = System.currentTimeMillis())
            repository.update(toSave)
        }
    }

    // 色プリセットが変更されるたびに画面から呼び出す関数。
    // テキストと同じ仕組み（debounce・新規付箋は対象外）で自動保存する。
    fun onColorChanged(note: NoteEntity, colorId: Int) {
        colorAutoSaveJob?.cancel()

        if (note.id == 0L) {
            return
        }

        colorAutoSaveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DEBOUNCE_MS)
            val toSave = note.copy(colorId = colorId, updatedAt = System.currentTimeMillis())
            repository.update(toSave)
        }
    }

    // 手動保存（保存ボタン・戻るボタン）が実行される直前に呼び出す。
    // 待機中の自動保存（テキスト・色の両方）があればキャンセルし、手動保存のみが実行されるようにする。
    fun cancelPendingAutoSave() {
        textAutoSaveJob?.cancel()
        colorAutoSaveJob?.cancel()
    }
}

// Application・Repository・SaveImageUseCase を渡して NoteEditorViewModel を作成する
class NoteEditorViewModelFactory(
    private val application: Application,
    private val repository: NoteRepository,
    private val saveImageUseCase: SaveImageUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteEditorViewModel(application, repository, saveImageUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}