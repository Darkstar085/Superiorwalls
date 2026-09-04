package com.sipun.superiorwalls.features.details

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.sipun.superiorwalls.data.repository.FavoriteWallpaperStore
import com.sipun.superiorwalls.domain.model.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun WallpaperDetailsScreen(wallpaper: Wallpaper, onBack: () -> Unit) {
    val context = LocalContext.current
    val favorites = remember { FavoriteWallpaperStore(context) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var isFavorite by remember { mutableStateOf(favorites.isFavorite(wallpaper.url)) }
    var busy by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AsyncImage(
                    model = wallpaper.url,
                    contentDescription = wallpaper.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.statusBarsPadding().padding(8.dp),
                ) { Text("Back") }
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
                        Button(onClick = {
                            isFavorite = !isFavorite
                            favorites.setFavorite(wallpaper.url, isFavorite)
                        }) { Text(if (isFavorite) "Unfavorite" else "Favorite") }
                        OutlinedButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, wallpaper.url)
                            }.let { Intent.createChooser(it, "Share wallpaper") })
                        }) { Text("Share") }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(enabled = !busy, onClick = {
                            busy = true
                            scope.launch {
                                val result = loadBitmap(context, wallpaper.url)
                                if (result == null) snackbar.showSnackbar("Could not load wallpaper")
                                else {
                                    WallpaperManager.getInstance(context).setBitmap(result)
                                    snackbar.showSnackbar("Wallpaper applied")
                                }
                                busy = false
                            }
                        }) { if (busy) CircularProgressIndicator() else Text("Set wallpaper") }
                        OutlinedButton(enabled = !busy, onClick = {
                            busy = true
                            scope.launch {
                                val saved = saveBitmap(context, wallpaper)
                                snackbar.showSnackbar(saved ?: "Could not save wallpaper")
                                busy = false
                            }
                        }) { Text("Download") }
                    }
                }
            }
        }
    }
}

private suspend fun loadBitmap(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
    val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
    val result = ImageLoader(context).execute(request)
    (result as? SuccessResult)?.image?.toBitmap()
}

private suspend fun saveBitmap(context: Context, wallpaper: Wallpaper): String? = withContext(Dispatchers.IO) {
    val bitmap = loadBitmap(context, wallpaper.url) ?: return@withContext null
    val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.apply { mkdirs() } ?: return@withContext null
    val safeName = wallpaper.name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "wallpaper" }
    val file = File(directory, "$safeName.jpg")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
    "Saved to ${file.absolutePath}"
}
