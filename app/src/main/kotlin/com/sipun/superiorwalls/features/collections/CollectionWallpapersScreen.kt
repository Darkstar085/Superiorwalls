package com.sipun.superiorwalls.features.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.sipun.superiorwalls.R
import com.sipun.superiorwalls.domain.model.Collection
import com.sipun.superiorwalls.domain.model.Wallpaper
import com.sipun.superiorwalls.features.home.WallpaperGrid

@Composable
fun CollectionWallpapersScreen(collection: Collection, onWallpaperClick: (Wallpaper) -> Unit) {
    if (collection.wallpapers.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(dimensionResource(R.dimen.screen_padding)), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.collection_empty))
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.screen_padding), vertical = dimensionResource(R.dimen.compact_spacing))) {
            Column(Modifier.padding(start = dimensionResource(R.dimen.screen_header_padding))) {
                Text(collection.displayName, style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.collection_count, collection.count), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        WallpaperGrid(collection.wallpapers, onWallpaperClick)
    }
}
