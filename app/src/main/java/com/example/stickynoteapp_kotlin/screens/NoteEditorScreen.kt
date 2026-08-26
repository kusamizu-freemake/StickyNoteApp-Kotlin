package com.example.stickynoteapp_kotlin.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.stickynoteapp_kotlin.viewmodel.NoteViewModel

// 付箋を作成・編集する画面
// TopAppBar でMaterial3の実験的なAPIを使用
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteViewModel,
    noteId: Long?,
    onBack: () -> Unit
) {
    // 読み込み中の付箋データ（保存済みの元データ）。text 以外の項目（色など）を保持しておくために使用
    var loadedNote by remember { mutableStateOf<NoteEntity?>(null) }
    // 画面に入力されているテキスト本文。
    var text by remember { mutableStateOf("") }
    // 「まだ Room からの読み込みが終わっていない」あいだ、ローディング表示を出すためのフラグ。
    var isLoading by remember { mutableStateOf(true) }

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

    // 「戻る」または「保存」が押されたときにまとめて呼ぶ処理。
    fun saveAndGoBack() {
        val base = loadedNote ?: NoteEntity()
        val toSave = base.copy(text = text, updatedAt = System.currentTimeMillis())
        viewModel.saveNote(toSave)
        onBack()
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
                    IconButton(onClick = { saveAndGoBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.editor_back_description)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { saveAndGoBack() }) {
                        Text(stringResource(R.string.editor_save))
                    }
                }
            )
        }
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
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.editor_text_placeholder)) }
            )
        }
    }
}