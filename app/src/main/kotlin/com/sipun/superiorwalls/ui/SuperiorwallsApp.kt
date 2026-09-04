package com.sipun.superiorwalls.ui

import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sipun.superiorwalls.AppContainer
import com.sipun.superiorwalls.R
import com.sipun.superiorwalls.data.repository.AppSettingsStore
import com.sipun.superiorwalls.data.repository.FavoriteWallpaperStore
import com.sipun.superiorwalls.data.repository.ThemeMode
import com.sipun.superiorwalls.features.collections.CollectionWallpapersScreen
import com.sipun.superiorwalls.features.collections.CollectionsScreen
import com.sipun.superiorwalls.features.details.WallpaperDetailsScreen
import com.sipun.superiorwalls.features.favorites.FavoritesScreen
import com.sipun.superiorwalls.features.home.HomeScreen
import com.sipun.superiorwalls.features.settings.SettingsScreen
import com.sipun.superiorwalls.navigation.AppDestination
import com.sipun.superiorwalls.ui.theme.SuperiorwallsTheme

private fun detailsRoute(url: String, mode: String = "all", collection: String? = null): String {
    val base = "${AppDestination.Details.routeBase}/${Uri.encode(url)}?mode=${Uri.encode(mode)}"
    return if (collection == null) base else "$base&collection=${Uri.encode(collection)}"
}

private fun navigateTopLevel(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(AppDestination.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun SuperiorwallsApp() {
    val navController = rememberNavController()
    val repository = AppContainer.wallpaperRepository
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = remember { AppSettingsStore(context) }
    val favorites = remember { FavoriteWallpaperStore(context) }
    val favoriteUrls by favorites.observeFavoriteUrls().collectAsStateWithLifecycle(initialValue = favorites.favoriteUrls())
    val themeMode by settings.observeThemeMode().collectAsStateWithLifecycle(initialValue = settings.themeMode())
    val interfaceSettings by settings.observeInterfaceSettings().collectAsStateWithLifecycle(initialValue = settings.interfaceSettings())
    val wallpapers by repository.observeWallpapers().collectAsStateWithLifecycle(initialValue = emptyList())
    val collections by repository.observeCollections().collectAsStateWithLifecycle(initialValue = emptyList())
    val destinations = listOf(AppDestination.Home, AppDestination.Collections, AppDestination.Favorites, AppDestination.Settings)
    val entry by navController.currentBackStackEntryAsState()
    val current = entry?.destination
    val showNavigation = destinations.any { destination -> current?.hierarchy?.any { it.route == destination.route } == true }
    val useRail = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 840
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    SuperiorwallsTheme(
        darkTheme = darkTheme,
        useMaterialYou = interfaceSettings.materialYou,
        useAmoledTheme = interfaceSettings.amoledTheme,
        colorNavigationBar = interfaceSettings.colorNavigationBar,
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
            modifier = Modifier.fillMaxSize(),
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                Row(Modifier.fillMaxSize()) {
                    if (showNavigation && useRail) {
                        NavigationRail {
                            destinations.forEach { destination ->
                                val label = stringResource(destination.labelRes)
                                NavigationRailItem(
                                    selected = current?.hierarchy?.any { it.route == destination.route } == true,
                                    onClick = { navigateTopLevel(navController, destination.route) },
                                    icon = { Icon(destination.icon, contentDescription = label) },
                                    label = { Text(label) },
                                )
                            }
                        }
                    }
                    NavHost(
                        navController = navController,
                        startDestination = AppDestination.Home.route,
                        modifier = Modifier.weight(1f),
                        enterTransition = {
                            if (interfaceSettings.animationsEnabled) {
                                fadeIn() + slideInHorizontally(initialOffsetX = { it / 8 })
                            } else {
                                EnterTransition.None
                            }
                        },
                        exitTransition = {
                            if (interfaceSettings.animationsEnabled) {
                                fadeOut() + slideOutHorizontally(targetOffsetX = { -it / 8 })
                            } else {
                                ExitTransition.None
                            }
                        },
                        popEnterTransition = {
                            if (interfaceSettings.animationsEnabled) {
                                fadeIn() + slideInHorizontally(initialOffsetX = { -it / 8 })
                            } else {
                                EnterTransition.None
                            }
                        },
                        popExitTransition = {
                            if (interfaceSettings.animationsEnabled) {
                                fadeOut() + slideOutHorizontally(targetOffsetX = { it / 8 })
                            } else {
                                ExitTransition.None
                            }
                        },
                    ) {
                        composable(AppDestination.Home.route) {
                            HomeScreen(onWallpaperClick = { wallpaper -> navController.navigate(detailsRoute(wallpaper.url)) })
                        }
                        composable(AppDestination.Collections.route) {
                            CollectionsScreen(collections) { collection -> navController.navigate("${AppDestination.CollectionDetails.routeBase}/${Uri.encode(collection.name)}") }
                        }
                        composable(AppDestination.Favorites.route) {
                            FavoritesScreen(wallpapers, context) { wallpaper -> navController.navigate(detailsRoute(wallpaper.url, mode = "favorites")) }
                        }
                        composable(AppDestination.Settings.route) { SettingsScreen(settings) }
                        composable(AppDestination.CollectionDetails.route, listOf(navArgument("name") { type = NavType.StringType })) { entry ->
                            val collection = entry.arguments?.getString("name")?.let { name -> collections.firstOrNull { it.name == name } }
                            if (collection == null) navController.popBackStack()
                            else CollectionWallpapersScreen(collection) { wallpaper -> navController.navigate(detailsRoute(wallpaper.url, collection = collection.name)) }
                        }
                        composable(
                            AppDestination.Details.route,
                            listOf(
                                navArgument("url") { type = NavType.StringType },
                                navArgument("mode") { type = NavType.StringType; defaultValue = "all" },
                                navArgument("collection") { type = NavType.StringType; nullable = true; defaultValue = null },
                            ),
                        ) { entry ->
                            val wallpaper = entry.arguments?.getString("url")?.let { url -> wallpapers.firstOrNull { it.url == url } }
                            val mode = entry.arguments?.getString("mode") ?: "all"
                            val collection = entry.arguments?.getString("collection")
                            if (wallpaper == null) navController.popBackStack()
                            else WallpaperDetailsScreen(
                                wallpaper = wallpaper,
                                wallpapers = wallpapers,
                                favoriteUrls = favoriteUrls,
                                mode = mode,
                                collectionName = collection,
                                onWallpaperChange = { next ->
                                    navController.navigate(detailsRoute(next.url, mode, collection)) {
                                        popUpTo(AppDestination.Details.route) { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() },
                            )
                        }
                    }
                }

                if (showNavigation && !useRail) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                            .navigationBarsPadding(),
                        shape = RoundedCornerShape(34.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f),
                        tonalElevation = 6.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            destinations.forEach { destination ->
                                val label = stringResource(destination.labelRes)
                                val selected = current?.hierarchy?.any { it.route == destination.route } == true
                                Surface(
                                    onClick = { navigateTopLevel(navController, destination.route) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(28.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent,
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = label,
                                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(25.dp),
                                        )
                                        Text(
                                            label,
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
