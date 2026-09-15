package com.sipun.superiorwalls.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.sipun.superiorwalls.R
import com.sipun.superiorwalls.data.repository.FavoriteWallpaperStore
import com.sipun.superiorwalls.domain.model.Wallpaper
import com.sipun.superiorwalls.ui.theme.LocalAnimationsEnabled
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(onWallpaperClick: (Wallpaper) -> Unit, viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val favoriteStore = remember { FavoriteWallpaperStore(context) }
    val favoriteUrls by favoriteStore.observeFavoriteUrls().collectAsStateWithLifecycle(initialValue = favoriteStore.favoriteUrls())
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            state.isLoading -> LoadingContent()
            else -> PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = viewModel::refresh, modifier = Modifier.fillMaxSize()) {
                when {
                    !state.hasLoadedRemoteData -> EmptyContent(state.errorMessage, onRetry = viewModel::refresh)
                    state.wallpapers.isEmpty() -> EmptyContent(state.errorMessage, onRetry = viewModel::refresh)
                    else -> WallpaperGrid(state.wallpapers, onWallpaperClick, state.errorMessage, favoriteUrls, onFavoriteToggle = { wallpaper -> favoriteStore.setFavorite(wallpaper.url, wallpaper.url !in favoriteUrls) }, showHeader = true)
                }
            }
        }
    }
}

@Composable
fun WallpaperGrid(wallpapers: List<Wallpaper>, onWallpaperClick: (Wallpaper) -> Unit, message: String? = null, favoriteUrls: Set<String> = emptySet(), onFavoriteToggle: (Wallpaper) -> Unit = {}, showHeader: Boolean = false) {
    val animationsEnabled = LocalAnimationsEnabled.current
    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(columns = GridCells.Adaptive(minSize = dimensionResource(R.dimen.wallpaper_grid_min_size)), contentPadding = PaddingValues(dimensionResource(R.dimen.screen_padding)), horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.grid_spacing)), verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.grid_spacing)), modifier = Modifier.fillMaxSize()) {
            if (showHeader) item(span = { GridItemSpan(maxLineSpan) }) { HomeHeader() }
            items(wallpapers, key = { it.url }) { wallpaper -> WallpaperCard(wallpaper, wallpaper.url in favoriteUrls, onWallpaperClick, onFavoriteToggle, animationsEnabled) }
        }
        if (message != null) Surface(shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)), tonalElevation = dimensionResource(R.dimen.message_elevation), modifier = Modifier.align(Alignment.BottomCenter).padding(start = dimensionResource(R.dimen.screen_padding), end = dimensionResource(R.dimen.screen_padding), bottom = dimensionResource(R.dimen.bottom_nav_height) + dimensionResource(R.dimen.bottom_nav_margin) + dimensionResource(R.dimen.screen_padding))) { Text(message, modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.screen_padding), vertical = dimensionResource(R.dimen.compact_spacing)), style = MaterialTheme.typography.labelLarge) }
    }
}

@Composable private fun HomeHeader() { Column(Modifier.fillMaxWidth().padding(bottom = dimensionResource(R.dimen.section_spacing)), verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.compact_spacing))) { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium); Text(stringResource(R.string.home_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable private fun WallpaperCard(wallpaper: Wallpaper, isFavorite: Boolean, onClick: (Wallpaper) -> Unit, onFavoriteToggle: (Wallpaper) -> Unit, animationsEnabled: Boolean) {
    val favoriteScale by animateFloatAsState(if (isFavorite) 1.12f else 1f, if (animationsEnabled) tween(180) else snap(), label = "favorite_scale")
    Card(Modifier.fillMaxWidth().clickable { onClick(wallpaper) }, shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius))) {
        Box {
            WallpaperImage(wallpaper, animationsEnabled)
            IconButton(onClick = { onFavoriteToggle(wallpaper) }, modifier = Modifier.align(Alignment.BottomEnd).padding(dimensionResource(R.dimen.card_action_padding))) {
                Surface(shape = CircleShape, color = colorResource(R.color.app_scrim)) {
                    Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = if (isFavorite) stringResource(R.string.viewer_unfavorite) else stringResource(R.string.viewer_favorite), tint = colorResource(R.color.viewer_overlay_content), modifier = Modifier.padding(dimensionResource(R.dimen.card_icon_padding)).graphicsLayer(scaleX = favoriteScale, scaleY = favoriteScale))
                }
            }
        }
    }
}

@Composable private fun WallpaperImage(wallpaper: Wallpaper, animationsEnabled: Boolean) {
    val context = LocalContext.current
    val model = remember(wallpaper.url, wallpaper.thumbnail) { wallpaperPreviewUrl(wallpaper) }
    var imageLoaded by remember(model) { mutableStateOf(false) }
    val motionSpec = if (animationsEnabled) tween<Float>(260) else snap()
    val placeholderAlpha by animateFloatAsState(if (imageLoaded) 0f else 1f, motionSpec, label = "preview_placeholder")
    val imageAlpha by animateFloatAsState(if (imageLoaded) 1f else 0f, motionSpec, label = "preview_alpha")
    val imageOffset by animateDpAsState(if (imageLoaded) 0.dp else 6.dp, if (animationsEnabled) tween(260) else snap(), label = "preview_offset")
    Box(Modifier.fillMaxWidth().aspectRatio(0.72f)) {
        Box(Modifier.fillMaxSize().alpha(placeholderAlpha).background(MaterialTheme.colorScheme.surfaceVariant))
        AsyncImage(model = model, contentDescription = wallpaper.name, contentScale = ContentScale.Crop, imageLoader = WallpaperPreviewImageLoader.get(context), modifier = Modifier.fillMaxSize().alpha(imageAlpha).offset(y = imageOffset), onLoading = { imageLoaded = false }, onSuccess = { imageLoaded = true }, onError = { imageLoaded = false })
    }
}

private fun wallpaperPreviewUrl(wallpaper: Wallpaper): String? { val sourceUrl = (wallpaper.thumbnail?.trim()?.takeIf { it.isNotEmpty() } ?: wallpaper.url.trim().takeIf { it.isNotEmpty() }) ?: return null; val encodedUrl = URLEncoder.encode(sourceUrl, StandardCharsets.UTF_8.name()); return "https://wsrv.nl/?url=$encodedUrl&w=480&output=webp&q=72" }

@Composable private fun LoadingContent() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }

@Composable private fun EmptyContent(message: String?, onRetry: () -> Unit) { val isNoNetwork = message == stringResource(R.string.home_no_network); val title = if (isNoNetwork) stringResource(R.string.home_no_network_title) else (message ?: stringResource(R.string.home_empty)); val subtitle = if (isNoNetwork) stringResource(R.string.home_no_network_subtitle) else null; LazyColumn(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, contentPadding = PaddingValues(dimensionResource(R.dimen.screen_padding))) { item { Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.compact_spacing))) { if (isNoNetwork) Icon(imageVector = Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.padding(bottom = dimensionResource(R.dimen.compact_spacing)).size(dimensionResource(R.dimen.viewer_navigation_button_size)), tint = MaterialTheme.colorScheme.onSurfaceVariant); Text(title, style = MaterialTheme.typography.headlineSmall); if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant); Button(onClick = onRetry, modifier = Modifier.padding(top = dimensionResource(R.dimen.compact_spacing))) { Icon(Icons.Default.Refresh, contentDescription = null); Text(stringResource(R.string.retry), modifier = Modifier.padding(start = dimensionResource(R.dimen.compact_spacing))) } } } } }