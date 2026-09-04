package com.sipun.superiorwalls.navigation

sealed class AppDestination(val route: String) {
    data object Home : AppDestination("home")
    data object Collections : AppDestination("collections")
    data object Favorites : AppDestination("favorites")
    data object ImportedImage : AppDestination("imported-image")
    data object Details : AppDestination("details/{url}") {
        const val routeBase = "details"
    }
    data object CollectionDetails : AppDestination("collection/{name}") {
        const val routeBase = "collection"
    }
}
