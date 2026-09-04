package com.sipun.superiorwalls.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sipun.superiorwalls.features.home.HomeScreen
import com.sipun.superiorwalls.navigation.AppDestination

@Composable
fun SuperiorwallsApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route,
    ) {
        composable(AppDestination.Home.route) {
            HomeScreen()
        }
    }
}
