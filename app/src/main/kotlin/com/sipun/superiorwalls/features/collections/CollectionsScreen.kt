package com.sipun.superiorwalls.features.collections

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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sipun.superiorwalls.domain.model.Collection

@Composable
fun CollectionsScreen(
    collections: List<Collection>,
    onCollectionClick: (Collection) -> Unit,
) {
    if (collections.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp)) {
            Text("No collections available yet.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(collections, key = { it.name }) { collection ->
            CollectionCard(collection, onCollectionClick)
        }
    }
}

@Composable
private fun CollectionCard(collection: Collection, onClick: (Collection) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(collection) },
    ) {
        collection.cover?.let { cover ->
            AsyncImage(
                model = cover.thumbnail?.takeIf { it.isNotBlank() } ?: cover.url,
                contentDescription = collection.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(180.dp),
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(collection.displayName, style = MaterialTheme.typography.titleLarge)
            Text("${collection.count} wallpapers", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
