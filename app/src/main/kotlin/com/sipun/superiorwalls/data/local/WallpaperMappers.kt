package com.sipun.superiorwalls.data.local

import com.sipun.superiorwalls.domain.model.Wallpaper

fun WallpaperEntity.toDomain(): Wallpaper = Wallpaper(
    name = name,
    url = url,
    author = author,
    thumbnail = thumbnail,
    collections = collections,
    dimensions = dimensions,
    copyright = copyright,
    downloadable = downloadable,
    size = size,
)

fun Wallpaper.toEntity(): WallpaperEntity = WallpaperEntity(
    url = url,
    name = name,
    author = author,
    thumbnail = thumbnail,
    collections = collections,
    dimensions = dimensions,
    copyright = copyright,
    downloadable = downloadable ?: true,
    size = size ?: 0L,
)
