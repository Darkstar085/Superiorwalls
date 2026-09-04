package com.sipun.superiorwalls.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sipun.superiorwalls.data.repository.InMemoryWallpaperRepository
import com.sipun.superiorwalls.features.details.WallpaperDetailsScreen
import com.sipun.superiorwalls.features.home.HomeScreen
import com.sipun.superiorwalls.navigation.AppDestination

@Composable
fun SuperiorwallsApp() {
    val navController = rememberNavController()
    val repository = InMemoryWallpaperRepository()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route,
    ) {
        composable(AppDestination.Home.route) {
            HomeScreen(
                onWallpaperClick = { wallpaper ->
                    navController.navigate("details/${Uri.encode(wallpaper.url)}")
                },
            )
        }
        composable(
            route = AppDestination.Details.route,
            arguments = listOf(navArgument("url") { type = NavType.StringType }),
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url")
            val wallpaper = url?.let { repository.findWallpaper(it) }
            if (wallpaper == null) {
                navController.popBackStack()
            } else {
                WallpaperDetailsScreen(
                    wallpaper = wallpaper,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
