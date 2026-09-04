package com.sipun.superiorwalls

import android.content.Context
import androidx.room3.Room
import com.sipun.superiorwalls.data.local.SuperiorwallsDatabase
import com.sipun.superiorwalls.data.repository.RoomWallpaperRepository
import com.sipun.superiorwalls.domain.repository.WallpaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppContainer {
    private lateinit var applicationContext: Context
    private val database: SuperiorwallsDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            SuperiorwallsDatabase::class.java,
            "superiorwalls.db",
        ).build()
    }

    val wallpaperRepository: WallpaperRepository by lazy {
        RoomWallpaperRepository(applicationContext, database)
    }

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    suspend fun clearLocalData(context: Context) = withContext(Dispatchers.IO) {
        database.clearAllTables()
        runCatching { context.cacheDir.deleteRecursively() }
        runCatching { context.externalCacheDir?.deleteRecursively() }
    }
}
