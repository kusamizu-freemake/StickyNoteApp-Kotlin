package com.example.stickynoteapp_kotlin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    editorViewModel: NoteEditorViewModel
) {
    var screen by remember { mutableStateOf<Screen>(Screen.List) }

    when (val current = screen) {
        is Screen.List -> {
            NoteListScreen(
                viewModel = listViewModel,
                onNoteClick = { noteId -> screen = Screen.Editor(noteId) },
                onAddClick = { screen = Screen.Editor(null) }
            )
        }
        is Screen.Editor -> {
            NoteEditorScreen(
                viewModel = editorViewModel,
                noteId = current.noteId,
                onBack = { screen = Screen.List }
            )
        }
    }
}
