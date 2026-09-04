package com.example.stickynoteapp_kotlin.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

// ギャラリーなどから選択した画像を、アプリ内部のストレージにコピーするクラス。
class ImageStorage(private val context: Context) {

    // ①指定された画像をアプリ内部（filesDir）にコピーし、保存先のパスを返す。
    // 読み込みに失敗した場合は IOException を投げる。
    @Throws(IOException::class)
    fun copyToInternalStorage(uri: Uri): String {
        val fileName = "note_${System.currentTimeMillis()}.jpg"
        val destFile = File(context.filesDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("画像を読み込めませんでした: $uri")

        return destFile.absolutePath
    }
}