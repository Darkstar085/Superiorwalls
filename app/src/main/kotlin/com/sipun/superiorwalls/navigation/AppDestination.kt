package com.sipun.superiorwalls.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Home : AppDestination("home", "Home", Icons.Default.Home)
    data object Collections : AppDestination("collections", "Collections", Icons.Default.Collections)
    data object Favorites : AppDestination("favorites", "Favorites", Icons.Default.Favorite)
    data object Settings : AppDestination("settings", "Settings", Icons.Default.Settings)
    data object ImportedImage : AppDestination("imported-image", "Imported wallpaper", Icons.Default.Home)
    data object Details : AppDestination("details/{url}", "Wallpaper", Icons.Default.Home) {
        const val routeBase = "details"
    }
    data object CollectionDetails : AppDestination("collection/{name}", "Collection", Icons.Default.Collections) {
        const val routeBase = "collection"
    }
}
