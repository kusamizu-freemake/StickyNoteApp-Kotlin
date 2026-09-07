package com.example.stickynoteapp_kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.stickynoteapp_kotlin.data.AppDatabase
import com.example.stickynoteapp_kotlin.data.ImageStorage
import com.example.stickynoteapp_kotlin.data.NoteRepository
import com.example.stickynoteapp_kotlin.usecase.SaveImageUseCase
import com.example.stickynoteapp_kotlin.viewmodel.NoteListViewModel
import com.example.stickynoteapp_kotlin.viewmodel.NoteListViewModelFactory
import com.example.stickynoteapp_kotlin.viewmodel.NoteEditorViewModel
import com.example.stickynoteapp_kotlin.viewmodel.NoteEditorViewModelFactory
import com.example.stickynoteapp_kotlin.ui.theme.StickyNoteAppKotlinTheme

class MainActivity : ComponentActivity() {
    // 一覧画面用・編集画面用、それぞれ専用のViewModelをRepositoryから作成する
    private val listViewModel: NoteListViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = NoteRepository(database.noteDao())
        NoteListViewModelFactory(repository)
    }

    private val editorViewModel: NoteEditorViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = NoteRepository(database.noteDao())
        val imageStorage = ImageStorage(applicationContext)
        val saveImageUseCase = SaveImageUseCase(imageStorage)
        NoteEditorViewModelFactory(application, repository, saveImageUseCase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StickyNoteAppKotlinTheme {
                StickyNoteApp(listViewModel = listViewModel, editorViewModel = editorViewModel)
            }
        }
    }
}