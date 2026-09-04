package com.sipun.superiorwalls.data.repository

import android.content.Context

class FavoriteWallpaperStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isFavorite(url: String): Boolean = favoriteUrls().contains(url)

    fun favoriteUrls(): Set<String> = preferences.getStringSet(KEY_URLS, emptySet()).orEmpty().toSet()

    fun setFavorite(url: String, favorite: Boolean) {
        val urls = favoriteUrls().toMutableSet()
        if (favorite) urls += url else urls -= url
        preferences.edit().putStringSet(KEY_URLS, urls).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "favorites"
        private const val KEY_URLS = "wallpaper_urls"
    }
}
