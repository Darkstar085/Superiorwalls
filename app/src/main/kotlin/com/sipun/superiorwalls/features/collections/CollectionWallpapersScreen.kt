package com.sipun.superiorwalls.features.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sipun.superiorwalls.features.home.WallpaperGrid
import com.sipun.superiorwalls.domain.model.Collection
import com.sipun.superiorwalls.domain.model.Wallpaper

@Composable
fun CollectionWallpapersScreen(
    collection: Collection,
    onWallpaperClick: (Wallpaper) -> Unit,
) {
    if (collection.wallpapers.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No wallpapers in this collection yet.")
        }
        return
    }

    WallpaperGrid(collection.wallpapers, onWallpaperClick)
}
