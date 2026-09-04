package com.sipun.superiorwalls.domain.repository

import com.sipun.superiorwalls.domain.model.Collection
import com.sipun.superiorwalls.domain.model.Wallpaper
import kotlinx.coroutines.flow.Flow

interface WallpaperRepository {
    fun observeWallpapers(): Flow<List<Wallpaper>>
    fun observeCollections(): Flow<List<Collection>>
    fun findWallpaper(url: String): Wallpaper?
}
