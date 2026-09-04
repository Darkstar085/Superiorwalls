package com.sipun.superiorwalls.features.details

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sipun.superiorwalls.data.repository.FavoriteWallpaperStore
import com.sipun.superiorwalls.domain.model.Wallpaper
import com.sipun.superiorwalls.features.system.saveToGallery
import com.sipun.superiorwalls.features.system.setAsWallpaper
import kotlinx.coroutines.launch

@Composable
fun WallpaperDetailsScreen(wallpaper: Wallpaper, onBack: () -> Unit) {
    val context = LocalContext.current
    val favorites = remember { FavoriteWallpaperStore(context) }
    val favoriteUrls by favorites.observeFavoriteUrls().collectAsState(initial = favorites.favoriteUrls())
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var busy by remember { mutableStateOf(false) }
    val isFavorite = wallpaper.url in favoriteUrls

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                AsyncImage(
                    model = wallpaper.url,
                    contentDescription = wallpaper.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.statusBarsPadding().padding(8.dp),
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(wallpaper.name, style = MaterialTheme.typography.headlineSmall)
                    wallpaper.author?.takeIf { it.isNotBlank() }?.let { Text("By $it") }
                    wallpaper.dimensions?.takeIf { it.isNotBlank() }?.let { Text("Dimensions: $it") }
                    wallpaper.size?.takeIf { it > 0 }?.let { Text("Size: ${it / 1024 / 1024} MB") }
                    wallpaper.copyright?.takeIf { it.isNotBlank() }?.let { Text(it) }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { favorites.setFavorite(wallpaper.url, !isFavorite) }) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                            )
                            Text(if (isFavorite) "Unfavorite" else "Favorite", Modifier.padding(start = 8.dp))
                        }
                        OutlinedButton(onClick = {
                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, wallpaper.url)
                            }, "Share wallpaper"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Text("Share", Modifier.padding(start = 8.dp))
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(enabled = !busy, onClick = {
                            busy = true
                            scope.launch {
                                try {
                                    snackbar.showSnackbar(setAsWallpaper(context, wallpaper.url) ?: "Wallpaper applied")
                                } finally {
                                    busy = false
                                }
                            }
                        }) {
                            if (busy) CircularProgressIndicator() else Icon(Icons.Default.Wallpaper, contentDescription = null)
                            if (!busy) Text("Set wallpaper", Modifier.padding(start = 8.dp))
                        }
                        OutlinedButton(enabled = !busy && wallpaper.downloadable != false, onClick = {
                            busy = true
                            scope.launch {
                                try {
                                    snackbar.showSnackbar(saveToGallery(context, wallpaper.url, wallpaper.name) ?: "Saved to Pictures/Superiorwalls")
                                } finally {
                                    busy = false
                                }
                            }
                        }) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Text("Download", Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }
}
