package com.sipun.superiorwalls.data.repository

import com.google.gson.GsonBuilder
import com.sipun.superiorwalls.data.network.WallpapersJsonService
import com.sipun.superiorwalls.domain.model.Collection
import com.sipun.superiorwalls.domain.model.Wallpaper
import com.sipun.superiorwalls.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InMemoryWallpaperRepository : WallpaperRepository {
    private val fallbackWallpapers = listOf(
        Wallpaper("Mountain Lake", "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=85", "Unsplash", "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=600&q=75", "nature"),
        Wallpaper("Desert Dunes", "https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=1200&q=85", "Unsplash", "https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=600&q=75", "nature"),
        Wallpaper("Ocean Coast", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=85", "Unsplash", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=75", "nature"),
        Wallpaper("Forest Path", "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=1200&q=85", "Unsplash", "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=600&q=75", "nature"),
    )

    private val wallpapersState = MutableStateFlow(fallbackWallpapers)
    private val collectionsState = MutableStateFlow(buildCollections(fallbackWallpapers))

    private val service by lazy {
        Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(WallpapersJsonService::class.java)
    }

    val wallpapers: List<Wallpaper> get() = wallpapersState.value
    val collections: List<Collection> get() = collectionsState.value

    override fun observeWallpapers(): Flow<List<Wallpaper>> = wallpapersState.asStateFlow()
    override fun observeCollections(): Flow<List<Collection>> = collectionsState.asStateFlow()
    override fun findWallpaper(url: String): Wallpaper? = wallpapers.firstOrNull { it.url == url }

    override suspend fun refresh(): Result<Unit> = runCatching {
        val remote = service.getJson(DATA_URL).filter { it.url.isNotBlank() }.distinctBy { it.url }
        if (remote.isNotEmpty()) {
            wallpapersState.value = remote
            collectionsState.value = buildCollections(remote)
        }
    }

    private fun buildCollections(wallpapers: List<Wallpaper>): List<Collection> {
        val grouped = linkedMapOf<String, Pair<String, MutableList<Wallpaper>>>()
        wallpapers.forEach { wallpaper ->
            wallpaper.collections.orEmpty()
                .replace("|", ",")
                .split(",")
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .forEach { name ->
                    val key = name.lowercase()
                    val current = grouped[key]?.second ?: mutableListOf()
                    current += wallpaper
                    grouped[key] = name to current
                }
        }
        return grouped.values.map { (name, items) ->
            Collection(name = name, displayName = name.replaceFirstChar { it.uppercase() }, wallpapers = items.distinctBy { it.url })
        }
    }

    companion object {
        const val DATA_URL = "https://raw.githubusercontent.com/SuperiorOS/Superiorwalls/master/wallpaper_configV2.json"
    }
}
