package com.sipun.superiorwalls.features.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sipun.superiorwalls.AppContainer
import com.sipun.superiorwalls.data.repository.AppSettingsStore
import com.sipun.superiorwalls.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: WallpaperRepository,
    private val context: Context,
) : ViewModel() {
    private val settingsStore = AppSettingsStore(context)
    private val _isRefreshing = MutableStateFlow(false)
    private val _hasLoadedRemoteData = MutableStateFlow(settingsStore.hasLoadedRemoteWallpapers())
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = !_hasLoadedRemoteData.value))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeWallpapers(),
                repository.observeCollections(),
                _isRefreshing,
                _hasLoadedRemoteData,
                _errorMessage,
            ) { wallpapers, collections, isRefreshing, hasLoadedRemoteData, errorMessage ->
                HomeUiState(
                    wallpapers = wallpapers,
                    collections = collections,
                    isLoading = !hasLoadedRemoteData && isRefreshing,
                    isRefreshing = isRefreshing,
                    hasLoadedRemoteData = hasLoadedRemoteData,
                    errorMessage = errorMessage,
                )
            }.collect { _uiState.value = it }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val hasLoadedRemoteData = _hasLoadedRemoteData.value
            if (!hasLoadedRemoteData && !hasInternetConnection()) {
                _isRefreshing.value = false
                _errorMessage.value = "No network connection. Connect to the internet."
                return@launch
            }

            if (hasLoadedRemoteData && !hasInternetConnection()) {
                _isRefreshing.value = false
                _errorMessage.value = "Offline · Showing cached wallpapers"
                return@launch
            }

            _isRefreshing.value = true
            _errorMessage.value = null
            repository.refresh().onSuccess {
                _hasLoadedRemoteData.value = true
                settingsStore.setRemoteWallpapersLoaded()
            }.onFailure {
                _errorMessage.value = "Could not load wallpapers. Please try again."
            }
            _isRefreshing.value = false
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    AppContainer.wallpaperRepository,
                    AppContainer.applicationContext,
                )
            }
        }
    }
}
