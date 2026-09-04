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
    private val _isRefreshing = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeWallpapers(),
                repository.observeCollections(),
                _isRefreshing,
                _errorMessage,
            ) { wallpapers, collections, isRefreshing, errorMessage ->
                HomeUiState(
                    wallpapers = wallpapers,
                    collections = collections,
                    isLoading = wallpapers.isEmpty() && isRefreshing,
                    isRefreshing = isRefreshing,
                    errorMessage = errorMessage,
                )
            }.collect { _uiState.value = it }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            repository.refresh().onFailure {
                _errorMessage.value = "Could not refresh wallpapers. Showing saved wallpapers."
            }
            _isRefreshing.value = false
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(AppContainer.wallpaperRepository) }
        }
    }
}
