package com.sipun.superiorwalls.data.repository

import com.sipun.superiorwalls.domain.model.Collection
import com.sipun.superiorwalls.domain.model.Wallpaper
import com.sipun.superiorwalls.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InMemoryWallpaperRepository : WallpaperRepository {
    private val wallpapers = listOf(
        Wallpaper(
            name = "Mountain Lake",
            author = "Unsplash",
            url = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=85",
            thumbnail = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=600&q=75",
            collections = "nature",
        ),
        Wallpaper(
            name = "Desert Dunes",
            author = "Unsplash",
            url = "https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=1200&q=85",
            thumbnail = "https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=600&q=75",
            collections = "nature",
        ),
        Wallpaper(
            name = "Ocean Coast",
            author = "Unsplash",
            url = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=85",
            thumbnail = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=75",
            collections = "nature",
        ),
        Wallpaper(
            name = "Forest Path",
            author = "Unsplash",
            url = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=1200&q=85",
            thumbnail = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=600&q=75",
            collections = "nature",
        ),
    )

    override fun observeWallpapers(): Flow<List<Wallpaper>> = flowOf(wallpapers)

    override fun observeCollections(): Flow<List<Collection>> = flowOf(
        listOf(Collection(name = "nature", displayName = "Nature", wallpapers = wallpapers))
    )

    override fun findWallpaper(url: String): Wallpaper? = wallpapers.firstOrNull { it.url == url }
}
