package com.sipun.superiorwalls.data.repository

import android.content.Context
import com.sipun.superiorwalls.data.local.SuperiorwallsDatabase
import com.sipun.superiorwalls.data.local.WallpaperEntity
import com.sipun.superiorwalls.data.local.toDomain
import com.sipun.superiorwalls.data.local.toEntity
import com.sipun.superiorwalls.data.network.WallpapersJsonService
import com.sipun.superiorwalls.domain.model.Collection
import com.sipun.superiorwalls.domain.model.Wallpaper
import com.sipun.superiorwalls.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RoomWallpaperRepository(
    private val context: Context,
    private val database: SuperiorwallsDatabase,
) : WallpaperRepository {
    private val dao = database.wallpaperDao()
    private val refreshMutex = Mutex()

    @Volatile
    private var cachedWallpapers: List<Wallpaper> = emptyList()

    private val service by lazy {
        Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WallpapersJsonService::class.java)
    }

    override fun observeWallpapers(): Flow<List<Wallpaper>> =
        dao.observeAll().map { entities ->
            entities.map(WallpaperEntity::toDomain).also { cachedWallpapers = it }
        }

    override fun observeCollections(): Flow<List<Collection>> =
        observeWallpapers().map(WallpaperCollectionMapper::build)

    override fun findWallpaper(url: String): Wallpaper? =
        cachedWallpapers.firstOrNull { it.url == url }

    override suspend fun refresh(): Result<Unit> = refreshMutex.withLock {
        runCatching {
            val remote = service.getJson(DATA_URL)
                .filter { it.url.isNotBlank() }
                .distinctBy { it.url }
            if (remote.isNotEmpty()) {
                dao.replaceAll(remote.map(Wallpaper::toEntity))
            }
        }
    }

    companion object {
        const val DATA_URL = "https://raw.githubusercontent.com/SuperiorOS/Superiorwalls/master/wallpaper_configV2.json"
    }
}
