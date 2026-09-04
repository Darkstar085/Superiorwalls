package com.sipun.superiorwalls.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.sipun.superiorwalls.R

sealed class AppDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    data object Home : AppDestination("home", R.string.nav_home, Icons.Default.Home)
    data object Collections : AppDestination("collections", R.string.nav_collections, Icons.Default.Collections)
    data object Favorites : AppDestination("favorites", R.string.nav_favorites, Icons.Default.Favorite)
    data object Settings : AppDestination("settings", R.string.nav_settings, Icons.Default.Settings)
    data object Details : AppDestination("details/{url}", R.string.nav_wallpaper, Icons.Default.Home) {
        const val routeBase = "details"
    }
    data object CollectionDetails : AppDestination("collection/{name}", R.string.nav_collection, Icons.Default.Collections) {
        const val routeBase = "collection"
    }
}
