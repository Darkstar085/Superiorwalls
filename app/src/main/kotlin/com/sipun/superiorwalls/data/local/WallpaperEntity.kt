package com.sipun.superiorwalls.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "wallpapers")
data class WallpaperEntity(
    @PrimaryKey val url: String,
    val name: String,
    val author: String?,
    val thumbnail: String?,
    val collections: String?,
    val dimensions: String?,
    val copyright: String?,
    val downloadable: Boolean,
    val size: Long,
)
