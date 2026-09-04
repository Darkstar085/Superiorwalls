package com.sipun.superiorwalls.navigation

sealed class AppDestination(
    val route: String,
    val label: String,
) {
    data object Home : AppDestination("home", "Home")
    data object Collections : AppDestination("collections", "Collections")
    data object Favorites : AppDestination("favorites", "Favorites")
    data object CollectionDetails : AppDestination("collection/{name}", "Collection") {
        const val routeBase = "collection"
    }
    data object Details : AppDestination("details/{url}", "Wallpaper") {
        const val routeBase = "details"
    }
}
