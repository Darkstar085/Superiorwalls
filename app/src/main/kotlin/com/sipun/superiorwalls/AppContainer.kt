package com.sipun.superiorwalls

import com.sipun.superiorwalls.data.repository.InMemoryWallpaperRepository

object AppContainer {
    val wallpaperRepository: InMemoryWallpaperRepository by lazy(::InMemoryWallpaperRepository)
}
