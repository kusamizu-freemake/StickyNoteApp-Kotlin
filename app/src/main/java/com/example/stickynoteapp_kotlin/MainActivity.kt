package com.example.stickynoteapp_kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.stickynoteapp_kotlin.data.AppDatabase
import com.example.stickynoteapp_kotlin.data.NoteRepository
import com.example.stickynoteapp_kotlin.viewmodel.NoteViewModel
import com.example.stickynoteapp_kotlin.viewmodel.NoteViewModelFactory
import com.example.stickynoteapp_kotlin.screens.NoteEditorScreen
import com.example.stickynoteapp_kotlin.screens.NoteListScreen
import com.example.stickynoteapp_kotlin.ui.theme.StickyNoteAppKotlinTheme

class MainActivity : ComponentActivity() {
    // Repositoryを使ってViewModelを作成する
    private val viewModel: NoteViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = NoteRepository(database.noteDao())
        NoteViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StickyNoteAppKotlinTheme {
                StickyNoteApp(viewModel = viewModel)
            }
        }
    }
}

// 「今どの画面を表示するか」を表す型
private sealed class Screen {
    data object List : Screen()
    data class Editor(val noteId: Long?) : Screen()
}

// 今の画面（一覧 or 編集）を覚えておき、それに応じて表示する内容を切り替えます。
@Composable
private fun StickyNoteApp(viewModel: NoteViewModel) {
    var screen by remember { mutableStateOf<Screen>(Screen.List) }

    when (val current = screen) {
        is Screen.List -> {
            NoteListScreen(
                viewModel = viewModel,
                onNoteClick = { noteId -> screen = Screen.Editor(noteId) },
                onAddClick = { screen = Screen.Editor(null) }
            )
        }
        is Screen.Editor -> {
            NoteEditorScreen(
                viewModel = viewModel,
                noteId = current.noteId,
                onBack = { screen = Screen.List }
            )
        }
    }
}