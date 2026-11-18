package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookmarkEntity::class,
        HistoryEntity::class,
        ShortcutEntity::class,
        DownloadEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HoloDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun shortcutDao(): ShortcutDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: HoloDatabase? = null

        fun getInstance(context: Context): HoloDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HoloDatabase::class.java,
                    "holo_browser.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
