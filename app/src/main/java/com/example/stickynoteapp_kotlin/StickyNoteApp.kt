package com.example.stickynoteapp_kotlin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.stickynoteapp_kotlin.data.NoteEntity
import com.example.stickynoteapp_kotlin.screens.NoteEditorScreen
import com.example.stickynoteapp_kotlin.screens.NoteListScreen
import com.example.stickynoteapp_kotlin.viewmodel.NoteEditorViewModel
import com.example.stickynoteapp_kotlin.viewmodel.NoteListViewModel

// 「今どの画面を表示するか」を表す型
private sealed class Screen {
    data object List : Screen()
    data class Editor(val noteId: Long?) : Screen()
}

// 今の画面（一覧 or 編集）を覚えておき、それに応じて表示する内容を切り替えます。
// MainActivity から呼び出されるため private は付けません。
@Composable
fun StickyNoteApp(
    listViewModel: NoteListViewModel,
    editorViewModel: NoteEditorViewModel,
    // 新規付箋の作成画面を開く依頼があるか。
    isNewNoteRequested: Boolean = false,
    // 新規作成の依頼を処理したことを呼び出し元に伝える。
    onNewNoteRequestHandled: () -> Unit = {}
) {
    var screen by remember { mutableStateOf<Screen>(Screen.List) }
    // 削除した付箋の情報。「元に戻す」を表示するために使用する。
    var deletedNote by remember { mutableStateOf<NoteEntity?>(null) }

    // 新規作成の依頼を受けたら、編集画面を開く。
    LaunchedEffect(isNewNoteRequested) {
        if (isNewNoteRequested) {
            screen = Screen.Editor(null)
            onNewNoteRequestHandled()
        }
    }

    when (val current = screen) {
        is Screen.List -> {
            NoteListScreen(
                viewModel = listViewModel,
                onNoteClick = { noteId -> screen = Screen.Editor(noteId) },
                onAddClick = { screen = Screen.Editor(null) },
                deletedNote = deletedNote,
                // Snackbarの表示が終わったら、削除情報をリセットする。
                onUndoHandled = { deletedNote = null }
            )
        }
        is Screen.Editor -> {
            // 付箋IDが変わったら、編集画面を作り直す。
            key(current.noteId) {
                NoteEditorScreen(
                    viewModel = editorViewModel,
                    noteId = current.noteId,
                    onBack = { screen = Screen.List },
                    // 削除した付箋を「元に戻す」ための情報を保存する。
                    onNoteDeleted = { note -> deletedNote = note }
                )
            }
        }
    }
}