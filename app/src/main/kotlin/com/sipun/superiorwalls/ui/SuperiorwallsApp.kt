package com.sipun.superiorwalls.ui

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.sipun.superiorwalls.navigation.AppDestination

@Composable
fun SuperiorwallsApp() {
    val navController = rememberNavController()
    val repository = AppContainer.wallpaperRepository
    val context = LocalContext.current
    val topLevelDestinations = listOf(AppDestination.Home, AppDestination.Collections, AppDestination.Favorites)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showNavigation = topLevelDestinations.any { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showNavigation) {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(AppDestination.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(destination.label.take(1)) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(AppDestination.Home.route) {
                HomeScreen(onWallpaperClick = { wallpaper ->
                    navController.navigate("${AppDestination.Details.routeBase}/${Uri.encode(wallpaper.url)}")
                })
            }
            composable(AppDestination.Collections.route) {
                CollectionsScreen(
                    collections = repository.collections,
                    onCollectionClick = { collection ->
                        navController.navigate("${AppDestination.CollectionDetails.routeBase}/${Uri.encode(collection.name)}")
                    },
                )
            }
            composable(AppDestination.Favorites.route) {
                FavoritesScreen(
                    wallpapers = repository.wallpapers,
                    context = context,
                    onWallpaperClick = { wallpaper ->
                        navController.navigate("${AppDestination.Details.routeBase}/${Uri.encode(wallpaper.url)}")
                    },
                )
            }
            composable(
                route = AppDestination.CollectionDetails.route,
                arguments = listOf(navArgument("name") { type = NavType.StringType }),
            ) { entry ->
                val name = entry.arguments?.getString("name")
                val collection = repository.collections.firstOrNull { it.name == name }
                if (collection == null) navController.popBackStack()
                else CollectionWallpapersScreen(collection, onWallpaperClick = { wallpaper ->
                    navController.navigate("${AppDestination.Details.routeBase}/${Uri.encode(wallpaper.url)}")
                })
            }
            composable(
                route = AppDestination.Details.route,
                arguments = listOf(navArgument("url") { type = NavType.StringType }),
            ) { entry ->
                val url = entry.arguments?.getString("url")
                val wallpaper = url?.let(repository::findWallpaper)
                if (wallpaper == null) navController.popBackStack()
                else WallpaperDetailsScreen(wallpaper, onBack = { navController.popBackStack() })
            }
        }
    }
}
