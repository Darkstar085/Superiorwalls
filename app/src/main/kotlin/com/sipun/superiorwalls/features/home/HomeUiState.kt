package com.sipun.superiorwalls.features.home

import com.sipun.superiorwalls.domain.model.Collection
import com.sipun.superiorwalls.domain.model.Wallpaper

data class HomeUiState(
    val wallpapers: List<Wallpaper> = emptyList(),
    val collections: List<Collection> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)
