package com.example.stickynoteapp_kotlin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// アプリ全体のデータベース本体です。

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // この DAO を通じて notes テーブルにアクセス
    abstract fun noteDao(): NoteDao

    companion object {
        // @Volatile を付けると、複数のスレッドから見ても常に最新の値が見えるようになる。
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // データベースのインスタンスを取得
        // まだ作られていなければ新しく作り、すでにあればそれを再利用
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sticky_note_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}