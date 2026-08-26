package com.example.stickynoteapp_kotlin.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stickynoteapp_kotlin.R
import com.example.stickynoteapp_kotlin.data.NoteEntity
import com.example.stickynoteapp_kotlin.viewmodel.NoteListViewModel
import com.example.stickynoteapp_kotlin.ui.theme.StickyNoteAppKotlinTheme

// 付箋の一覧を表示する画面
// TopAppBar でMaterial3の実験的なAPIを使用
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel,
    onNoteClick: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    // ViewModelから付箋一覧を取得し、画面に反映する
    val notes by viewModel.notes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.list_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.list_add_description))
            }
        }
    ) { innerPadding ->
        if (notes.isEmpty()) {
            // 付箋が1件もないときは、空であることが分かるメッセージを出します。
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.list_empty_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // key = { it.id } を指定すると、リストの一部だけが変わったときに
                // Compose が無駄な再描画をせず、賢く更新してくれます。
                items(notes, key = { it.id }) { note ->
                    NoteCard(note = note, onClick = { onNoteClick(note.id) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// 付箋1件分を表す、色付きのカード
@Composable
fun NoteCard(note: NoteEntity, onClick: () -> Unit) {
    val backgroundColor = Color(
        red = note.colorR,
        green = note.colorG,
        blue = note.colorB
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Text(
            text = note.text.ifBlank { stringResource(R.string.note_empty_text) },
            modifier = Modifier.padding(16.dp),
            color = Color.Black,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteCardPreview() {
    StickyNoteAppKotlinTheme {
        NoteCard(
            note = NoteEntity(id = 1, text = "牛乳を買う\n卵も忘れずに"),
            onClick = {}
        )
    }
}