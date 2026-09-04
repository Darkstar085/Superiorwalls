package com.sipun.superiorwalls.features.favorites

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.superiorwalls.R
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

    Column(Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.screen_padding))) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.compact_spacing))) {
                Text(stringResource(R.string.favorites_title), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.favorites_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (favorites.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.favorites_empty), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            WallpaperGrid(favorites, onWallpaperClick, favoriteUrls = favoriteUrls, onFavoriteToggle = { wallpaper -> store.setFavorite(wallpaper.url, false) })
        }
    }
}
