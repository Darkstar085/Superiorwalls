package com.sipun.superiorwalls.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sipun.superiorwalls.AppContainer
import com.sipun.superiorwalls.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: WallpaperRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.observeWallpapers(), repository.observeCollections()) { wallpapers, collections ->
                HomeUiState(wallpapers = wallpapers, collections = collections, isLoading = false)
            }.collect { _uiState.value = it }
        }
        viewModelScope.launch {
            repository.refresh().onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Could not refresh wallpapers.")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(AppContainer.wallpaperRepository) }
        }
    }
}
