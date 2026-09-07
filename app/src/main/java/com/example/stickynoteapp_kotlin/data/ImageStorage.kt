package com.example.stickynoteapp_kotlin.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

// 選択した画像をアプリ内部に保存するクラス
class ImageStorage(private val context: Context) {

    // 画像をアプリ内部にコピーし、成功・失敗の結果を返す
    // 成功時：保存先のパスを持つ Result.success
    // 失敗時：発生した例外を持つ Result.failure
    fun copyToInternalStorage(uri: Uri): Result<String> {
        return try {
            val fileName = "note_${System.currentTimeMillis()}.jpg"
            val destFile = File(context.filesDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IOException("画像を読み込めませんでした: $uri")

            Result.success(destFile.absolutePath)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}