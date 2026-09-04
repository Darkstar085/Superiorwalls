package com.sipun.superiorwalls.navigation

sealed class AppDestination(val route: String) {
    data object Home : AppDestination("home")
    data object Details : AppDestination("details/{url}")
}
