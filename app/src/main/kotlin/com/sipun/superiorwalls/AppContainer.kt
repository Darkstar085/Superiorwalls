package com.sipun.superiorwalls

import android.content.Context
import androidx.room3.Room
import com.sipun.superiorwalls.data.local.SuperiorwallsDatabase
import com.sipun.superiorwalls.data.repository.AppSettingsStore
import com.sipun.superiorwalls.data.repository.RoomWallpaperRepository
import com.sipun.superiorwalls.domain.repository.WallpaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppContainer {
    private lateinit var context: Context
    val applicationContext: Context
        get() = context

    private val database: SuperiorwallsDatabase by lazy {
        Room.databaseBuilder(
            context,
            SuperiorwallsDatabase::class.java,
            "superiorwalls.db",
        ).build()
    }

    val wallpaperRepository: WallpaperRepository by lazy {
        RoomWallpaperRepository(database)
    }

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    suspend fun clearLocalData(context: Context) = withContext(Dispatchers.IO) {
        database.clearAllTables()
        AppSettingsStore(context).clearRemoteWallpapersLoaded()
        runCatching { context.cacheDir.deleteRecursively() }
        runCatching { context.externalCacheDir?.deleteRecursively() }
    }
}
