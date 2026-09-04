package com.sipun.superiorwalls.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.sipun.superiorwalls.domain.model.Wallpaper

@Composable
fun HomeScreen(
    onWallpaperClick: (Wallpaper) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.isLoading -> LoadingContent()
        state.errorMessage != null -> ErrorContent(state.errorMessage)
        state.wallpapers.isEmpty() -> EmptyContent()
        else -> WallpaperGrid(state.wallpapers, onWallpaperClick)
    }
}

@Composable
private fun WallpaperGrid(wallpapers: List<Wallpaper>, onWallpaperClick: (Wallpaper) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(wallpapers, key = { it.url }) { wallpaper -> WallpaperCard(wallpaper, onWallpaperClick) }
    }
}

@Composable
private fun WallpaperCard(wallpaper: Wallpaper, onClick: (Wallpaper) -> Unit) {
    AsyncImage(
        model = wallpaper.thumbnail?.takeIf { it.isNotBlank() } ?: wallpaper.url,
        contentDescription = wallpaper.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.68f)
            .clip(MaterialTheme.shapes.large)
            .clickable { onClick(wallpaper) },
    )
}

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun EmptyContent() {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Text("No wallpapers available yet.") }
}

@Composable
private fun ErrorContent(message: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Text(message) }
}
