package com.sipun.superiorwalls.features.favorites

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.superiorwalls.data.repository.FavoriteWallpaperStore
import com.sipun.superiorwalls.domain.model.Wallpaper
import com.sipun.superiorwalls.features.home.WallpaperGrid

@Composable
fun FavoritesScreen(
    wallpapers: List<Wallpaper>,
    context: Context,
    onWallpaperClick: (Wallpaper) -> Unit,
) {
    val store = remember { FavoriteWallpaperStore(context) }
    val favoriteUrls by store.observeFavoriteUrls().collectAsStateWithLifecycle(initialValue = store.favoriteUrls())
    val favorites = wallpapers.filter { it.url in favoriteUrls }

    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "No favorite wallpapers yet.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    } else {
        WallpaperGrid(favorites, onWallpaperClick)
    }
}
