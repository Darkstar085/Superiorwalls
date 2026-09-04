package com.sipun.superiorwalls.features.collections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import com.sipun.superiorwalls.R
import com.sipun.superiorwalls.domain.model.Collection
import com.sipun.superiorwalls.ui.theme.LocalAnimationsEnabled

@Composable
fun CollectionsScreen(collections: List<Collection>, onCollectionClick: (Collection) -> Unit) {
    if (collections.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(dimensionResource(R.dimen.screen_padding)), contentAlignment = Alignment.Center) { Text(stringResource(R.string.collections_empty)) }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(dimensionResource(R.dimen.screen_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.grid_spacing)),
    ) {
        item {
            Column(Modifier.fillMaxWidth().padding(bottom = dimensionResource(R.dimen.compact_spacing))) {
                Text(stringResource(R.string.collections_title), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.collections_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(collections, key = { it.name }) { collection -> CollectionCard(collection, onCollectionClick) }
    }
}

@Composable
private fun CollectionCard(collection: Collection, onClick: (Collection) -> Unit) {
    val animationsEnabled = LocalAnimationsEnabled.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(collection) },
        shape = RoundedCornerShape(dimensionResource(R.dimen.collection_card_corner_radius)),
    ) {
        Box(Modifier.fillMaxWidth().height(dimensionResource(R.dimen.collection_card_height))) {
            collection.cover?.let { cover -> CollectionImage(cover.thumbnail?.takeIf { it.isNotBlank() } ?: cover.url, collection.displayName, animationsEnabled) }
            Column(modifier = Modifier.align(Alignment.CenterStart).padding(dimensionResource(R.dimen.screen_padding))) {
                Text(collection.displayName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
                Text(stringResource(R.string.collection_count, collection.count), color = MaterialTheme.colorScheme.onPrimary)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.align(Alignment.CenterEnd).padding(dimensionResource(R.dimen.screen_padding)), tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun CollectionImage(model: String, description: String, animationsEnabled: Boolean) {
    AnimatedVisibility(
        visible = true,
        enter = if (animationsEnabled) fadeIn(tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220)) else EnterTransition.None,
    ) {
        AsyncImage(model = model, contentDescription = description, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}
