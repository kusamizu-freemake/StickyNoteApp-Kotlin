package com.example.stickynoteapp_kotlin.usecase

import android.net.Uri
import com.example.stickynoteapp_kotlin.data.ImageStorage

// 「選択した画像を保存する」という1つの処理をまとめたUseCaseクラス
class SaveImageUseCase(private val imageStorage: ImageStorage) {

    operator fun invoke(uri: Uri): Result<String> {
        return imageStorage.copyToInternalStorage(uri)
    }
}