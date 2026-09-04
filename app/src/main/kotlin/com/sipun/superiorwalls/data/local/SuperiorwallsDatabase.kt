package com.sipun.superiorwalls.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [WallpaperEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SuperiorwallsDatabase : RoomDatabase() {
    abstract fun wallpaperDao(): WallpaperDao
}
