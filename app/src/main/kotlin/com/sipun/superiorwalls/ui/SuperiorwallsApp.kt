package com.sipun.superiorwalls.ui

import android.net.Uri
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sipun.superiorwalls.AppContainer
import com.sipun.superiorwalls.features.collections.CollectionWallpapersScreen
import com.sipun.superiorwalls.features.collections.CollectionsScreen
import com.sipun.superiorwalls.features.details.WallpaperDetailsScreen
import com.sipun.superiorwalls.features.favorites.FavoritesScreen
import com.sipun.superiorwalls.features.home.HomeScreen
import com.sipun.superiorwalls.features.system.ImportedImageScreen
import com.sipun.superiorwalls.navigation.AppDestination

@Composable
fun SuperiorwallsApp(importedImage: Uri? = null) {
    val navController = rememberNavController()
    val repository = AppContainer.wallpaperRepository
    val context = LocalContext.current
    val destinations = listOf(AppDestination.Home, AppDestination.Collections, AppDestination.Favorites)
    val entry by navController.currentBackStackEntryAsState()
    val current = entry?.destination
    val showNavigation = destinations.any { current?.hierarchy?.any { node -> node.route == it.route } == true }
    val useRail = LocalConfiguration.current.screenWidthDp >= 840

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (showNavigation && !useRail) {
                NavigationBar {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = current?.hierarchy?.any { it.route == destination.route } == true,
                            onClick = { navigateTopLevel(navController, destination.route) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        androidx.compose.foundation.layout.Row(Modifier.fillMaxSize().padding(padding)) {
            if (showNavigation && useRail) {
                NavigationRail {
                    destinations.forEach { destination ->
                        NavigationRailItem(
                            selected = current?.hierarchy?.any { it.route == destination.route } == true,
                            onClick = { navigateTopLevel(navController, destination.route) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
            NavHost(
                navController = navController,
                startDestination = if (importedImage != null) AppDestination.ImportedImage.route else AppDestination.Home.route,
                modifier = Modifier.weight(1f),
            ) {
                composable(AppDestination.Home.route) {
                    HomeScreen { wallpaper -> navController.navigate("${AppDestination.Details.routeBase}/${Uri.encode(wallpaper.url)}") }
                }
                composable(AppDestination.Collections.route) {
                    CollectionsScreen(repository.collections) { collection ->
                        navController.navigate("${AppDestination.CollectionDetails.routeBase}/${Uri.encode(collection.name)}")
                    }
                }
                composable(AppDestination.Favorites.route) {
                    FavoritesScreen(repository.wallpapers, context) { wallpaper ->
                        navController.navigate("${AppDestination.Details.routeBase}/${Uri.encode(wallpaper.url)}")
                    }
                }
                if (importedImage != null) {
                    composable(AppDestination.ImportedImage.route) {
                        ImportedImageScreen(importedImage) { navController.popBackStack() }
                    }
                }
                composable(AppDestination.CollectionDetails.route, listOf(navArgument("name") { type = NavType.StringType })) { entry ->
                    val collection = entry.arguments?.getString("name")?.let { name -> repository.collections.firstOrNull { it.name == name } }
                    if (collection == null) navController.popBackStack()
                    else CollectionWallpapersScreen(collection) { wallpaper ->
                        navController.navigate("${AppDestination.Details.routeBase}/${Uri.encode(wallpaper.url)}")
                    }
                }
                composable(AppDestination.Details.route, listOf(navArgument("url") { type = NavType.StringType })) { entry ->
                    val wallpaper = entry.arguments?.getString("url")?.let(repository::findWallpaper)
                    if (wallpaper == null) navController.popBackStack()
                    else WallpaperDetailsScreen(wallpaper) { navController.popBackStack() }
                }
            }
        }
    }
}

private fun navigateTopLevel(navController: androidx.navigation.NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(AppDestination.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
