package com.sipun.superiorwalls.data.repository

import android.content.Context

class FavoriteWallpaperStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun isFavorite(url: String): Boolean = preferences.getStringSet(KEY_URLS, emptySet()).orEmpty().contains(url)

    fun setFavorite(url: String, favorite: Boolean) {
        val urls = preferences.getStringSet(KEY_URLS, emptySet()).orEmpty().toMutableSet()
        if (favorite) urls += url else urls -= url
        preferences.edit().putStringSet(KEY_URLS, urls).apply()
    }

    companion object {
        private const val KEY_URLS = "wallpaper_urls"
    }
}
