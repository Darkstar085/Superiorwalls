package com.sipun.superiorwalls.features.home

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import okio.Path.Companion.toPath

object WallpaperPreviewImageLoader {
    @Volatile
    private var instance: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        return instance ?: synchronized(this) {
            instance ?: ImageLoader.Builder(context.applicationContext)
                .diskCache {
                    DiskCache.Builder()
                        .directory(
                            context.cacheDir.absolutePath
                                .toPath()
                        )
                        .maxSizeBytes(64L * 1024L * 1024L)
                        .build()
                }
                .build()
                .also { instance = it }
        }
    }
}