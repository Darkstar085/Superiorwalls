package com.sipun.superiorwalls.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.sipun.superiorwalls.R
import com.sipun.superiorwalls.data.repository.FavoriteWallpaperStore
import com.sipun.superiorwalls.domain.model.Wallpaper

@Composable
fun HomeScreen(onWallpaperClick: (Wallpaper) -> Unit, viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val favoriteStore = remember { FavoriteWallpaperStore(context) }
    val favoriteUrls by favoriteStore.observeFavoriteUrls().collectAsStateWithLifecycle(initialValue = favoriteStore.favoriteUrls())
    when {
        state.isLoading -> LoadingContent()
        else -> PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = viewModel::refresh, modifier = Modifier.fillMaxSize()) {
            when {
                state.wallpapers.isEmpty() -> EmptyContent(state.errorMessage)
                else -> WallpaperGrid(state.wallpapers, onWallpaperClick, state.errorMessage, favoriteUrls, onFavoriteToggle = { wallpaper -> favoriteStore.setFavorite(wallpaper.url, wallpaper.url !in favoriteUrls) }, showHeader = true)
            }
        }
    }
}

@Composable
fun WallpaperGrid(
    wallpapers: List<Wallpaper>,
    onWallpaperClick: (Wallpaper) -> Unit,
    message: String? = null,
    favoriteUrls: Set<String> = emptySet(),
    onFavoriteToggle: (Wallpaper) -> Unit = {},
    showHeader: Boolean = false,
) {
    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = dimensionResource(R.dimen.wallpaper_grid_min_size)),
            contentPadding = PaddingValues(dimensionResource(R.dimen.screen_padding)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.grid_spacing)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.grid_spacing)),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (showHeader) item(span = { GridItemSpan(maxLineSpan) }) { HomeHeader() }
            items(wallpapers, key = { it.url }) { wallpaper ->
                WallpaperCard(wallpaper, wallpaper.url in favoriteUrls, onWallpaperClick, onFavoriteToggle)
            }
        }
        if (message != null) {
            Surface(shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)), tonalElevation = dimensionResource(R.dimen.viewer_navigation_elevation), modifier = Modifier.align(Alignment.BottomCenter).padding(dimensionResource(R.dimen.screen_padding))) {
                Text(message, modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.screen_padding), vertical = dimensionResource(R.dimen.compact_spacing)), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Column(
        Modifier.fillMaxWidth().padding(bottom = dimensionResource(R.dimen.section_spacing)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.compact_spacing)),
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.home_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WallpaperCard(wallpaper: Wallpaper, isFavorite: Boolean, onClick: (Wallpaper) -> Unit, onFavoriteToggle: (Wallpaper) -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick(wallpaper) }, shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius))) {
        Box {
            AsyncImage(model = wallpaper.thumbnail?.takeIf { it.isNotBlank() } ?: wallpaper.url, contentDescription = wallpaper.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(0.68f))
            IconButton(onClick = { onFavoriteToggle(wallpaper) }, modifier = Modifier.align(Alignment.BottomEnd).padding(dimensionResource(R.dimen.card_action_padding))) {
                Surface(shape = CircleShape, color = colorResource(R.color.app_scrim)) {
                    Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = if (isFavorite) stringResource(R.string.viewer_unfavorite) else stringResource(R.string.viewer_favorite), tint = colorResource(R.color.viewer_overlay_content), modifier = Modifier.padding(dimensionResource(R.dimen.card_icon_padding)))
                }
            }
        }
    }
}

@Composable private fun LoadingContent() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }

@Composable private fun EmptyContent(message: String?) { Box(Modifier.fillMaxSize().padding(dimensionResource(R.dimen.screen_padding)), contentAlignment = Alignment.Center) { Text(message ?: stringResource(R.string.home_empty), style = MaterialTheme.typography.bodyLarge) } }
