package com.sipun.superiorwalls.data.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpapers ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE url = :url LIMIT 1")
    suspend fun findByUrl(url: String): WallpaperEntity?

    @Query("SELECT COUNT(*) FROM wallpapers")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertAll(wallpapers: List<WallpaperEntity>)
}
