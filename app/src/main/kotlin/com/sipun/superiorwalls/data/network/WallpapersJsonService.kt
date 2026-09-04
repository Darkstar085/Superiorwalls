package com.sipun.superiorwalls.data.network

import com.sipun.superiorwalls.domain.model.Wallpaper
import retrofit2.http.GET
import retrofit2.http.Url

interface WallpapersJsonService {
    @GET
    suspend fun getJson(@Url url: String): List<Wallpaper>
}
