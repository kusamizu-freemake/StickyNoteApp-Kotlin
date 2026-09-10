package com.example.stickynoteapp_kotlin.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.stickynoteapp_kotlin.R
import com.example.stickynoteapp_kotlin.data.NoteEntity
import com.example.stickynoteapp_kotlin.data.PresetColor
import com.example.stickynoteapp_kotlin.data.toRgbInts
import com.example.stickynoteapp_kotlin.viewmodel.NoteEditorViewModel
import java.io.File

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
    // 選択中の色プリセット
    var selectedColor by remember { mutableStateOf(PresetColor.YELLOW) }

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
        // 保存済みの色と同じプリセットを探し、選択状態にする（見つからなければ黄色にする）
        selectedColor = note?.let { n ->
            PresetColor.entries.find { preset ->
                val (r, g, b) = preset.toRgbInts()
                r == n.colorR && g == n.colorG && b == n.colorB
            }
        } ?: PresetColor.YELLOW
        isLoading = false
    }

    // プレビューに表示する画像を決める。
    // 新しく選択した画像を優先し、なければ保存済みの画像を表示する。
    val previewImageModel: Any? = selectedImageUri ?: loadedNote?.imagePath?.let { path -> File(path) }

    // 現在の入力内容をDBに保存する処理。
    // 待機中の自動保存があればキャンセルしてから、手動保存を実行する。
    // 画像が選択されている場合は、保存時にアプリ内部へコピーされる。
    fun save() {
        viewModel.cancelPendingAutoSave()
        val base = loadedNote ?: NoteEntity()
        val toSave = base.copy(text = text, updatedAt = System.currentTimeMillis())
        // TODO: 選択した色のプリセットID（selectedColor.id）をノートに保存する（保存対応Issueで実施）
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

                // 画像がある場合のみ、選択ボタンの直下にプレビューを表示する
                previewImageModel?.let { model ->
                    Spacer(modifier = Modifier.height(12.dp))
                    NoteImage(
                        model = model,
                        contentDescription = stringResource(R.string.editor_image_preview_description),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 色プリセットの選択セクション
                Text(
                    text = stringResource(R.string.editor_color_section_title),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorPresetRow(
                    selectedColor = selectedColor,
                    onColorSelected = { preset -> selectedColor = preset }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 選んだ色とテキストのプレビュー（一覧画面と同じNoteCardを再利用）
                val (previewR, previewG, previewB) = selectedColor.toRgbInts()
                NoteCard(
                    note = (loadedNote ?: NoteEntity()).copy(
                        text = text,
                        colorR = previewR,
                        colorG = previewG,
                        colorB = previewB
                    ),
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(16.dp))

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

// 色プリセットを横並びの丸いスウォッチとして表示する
@Composable
private fun ColorPresetRow(
    selectedColor: PresetColor,
    onColorSelected: (PresetColor) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PresetColor.entries.forEach { preset ->
            ColorSwatch(
                color = preset.color,
                isSelected = preset == selectedColor,
                onClick = { onColorSelected(preset) }
            )
        }
    }
}

// 丸いスウォッチ1個分。選択中はチェックマークを重ねて表示する
@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}