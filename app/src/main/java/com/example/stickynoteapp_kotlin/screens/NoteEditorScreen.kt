package com.example.stickynoteapp_kotlin.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.stickynoteapp_kotlin.R
import com.example.stickynoteapp_kotlin.data.NoteEntity
import com.example.stickynoteapp_kotlin.viewmodel.NoteEditorViewModel

// 付箋を作成・編集する画面
// TopAppBar でMaterial3の実験的なAPIを使用
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    noteId: Long?,
    onBack: () -> Unit,
    // 削除した付箋を一覧画面へ渡すための処理
    onNoteDeleted: (NoteEntity) -> Unit
) {
    // 読み込み中の付箋データ（保存済みの元データ）。text 以外の項目（色など）を保持しておくために使用
    var loadedNote by remember { mutableStateOf<NoteEntity?>(null) }
    // 画面に入力されているテキスト本文。
    var text by remember { mutableStateOf("") }
    // 「まだ Room からの読み込みが終わっていない」あいだ、ローディング表示を出すためのフラグ。
    var isLoading by remember { mutableStateOf(true) }
    // 削除確認ダイアログを表示するかどうか。
    var showDeleteDialog by remember { mutableStateOf(false) }
    // ギャラリーから選択した画像のURI。まだ未選択の場合は null。
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 画像保存エラーなどを表示するためのSnackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val imageSaveErrorMessage = stringResource(R.string.editor_image_save_error)
    val imageSaveError by viewModel.imageSaveError.collectAsState()

    // 画像の保存に失敗したら、Snackbarで知らせる
    LaunchedEffect(imageSaveError) {
        if (imageSaveError) {
            snackbarHostState.showSnackbar(imageSaveErrorMessage)
            viewModel.onImageSaveErrorShown()
        }
    }

    // ギャラリーから画像を1枚選ぶための起動処理
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // キャンセルした場合は uri が null で返ってくるため、選択状態もリセットする
        selectedImageUri = uri
        Log.d("NoteEditorScreen", "selected image uri: $uri")
    }

    // noteIdが変わったときに、付箋データを読み込む
    LaunchedEffect(noteId) {
        val note = if (noteId != null) {
            viewModel.getNoteById(noteId)
        } else {
            // 新規作成の場合は、まだ何も保存されていない「空の付箋」を用意します（id は 0 のまま）。
            NoteEntity()
        }
        loadedNote = note
        text = note?.text ?: ""
        isLoading = false
    }

    // 現在の入力内容をDBに保存する処理。
    // 待機中の自動保存があればキャンセルしてから、手動保存を実行する。
    // 画像が選択されている場合は、保存時にアプリ内部へコピーされる。
    fun save() {
        viewModel.cancelPendingAutoSave()
        val base = loadedNote ?: NoteEntity()
        val toSave = base.copy(text = text, updatedAt = System.currentTimeMillis())
        viewModel.saveNote(toSave, selectedImageUri)
    }

    // 現在の付箋を論理削除する処理。
    // 保存はせず、待機中の自動保存だけキャンセルしてから削除を実行する。
    fun deleteNote() {
        val note = loadedNote ?: return
        viewModel.cancelPendingAutoSave()
        viewModel.deleteNote(note)
        // 削除した付箋を一覧画面へ通知する
        onNoteDeleted(note)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (noteId == null) {
                            stringResource(R.string.editor_title_new)
                        } else {
                            stringResource(R.string.editor_title_edit)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { save(); onBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.editor_back_description)
                        )
                    }
                },
                actions = {
                    // 削除ボタンは、既に保存済みの付箋（noteId != null）を編集しているときのみ表示する。
                    // 新規作成中（まだ一度も保存されていない付箋）には表示しない。
                    if (noteId != null && !isLoading) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.editor_delete_description)
                            )
                        }
                    }
                    TextButton(onClick = { save(); onBack() }) {
                        Text(stringResource(R.string.editor_save))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // 画像選択ボタン。
                Button(
                    onClick = {
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Text(stringResource(R.string.editor_select_image))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { newText ->
                        text = newText
                        // 入力のたびに ViewModel へ通知し、自動保存（debounce）の判定を任せる
                        viewModel.onTextChanged(loadedNote ?: NoteEntity(), newText)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text(stringResource(R.string.editor_text_placeholder)) }
                )
            }
        }
    }

    // 削除確認ダイアログ。「削除」を押すと論理削除を実行して一覧画面に戻り、
    // 「キャンセル」を押すと閉じるだけで何も変更しない。
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.editor_delete_dialog_title)) },
            text = { Text(stringResource(R.string.editor_delete_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deleteNote()
                    onBack()
                }) {
                    Text(stringResource(R.string.editor_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.editor_delete_cancel))
                }
            }
        )
    }
}