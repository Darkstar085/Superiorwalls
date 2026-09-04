package com.sipun.superiorwalls.features.system

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportedImageScreen(uri: Uri, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var busy by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Imported wallpaper") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("Back") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Imported wallpaper",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    enabled = !busy,
                    onClick = {
                        busy = true
                        scope.launch {
                            val error = setAsWallpaper(context, uri)
                            snackbar.showSnackbar(error ?: "Wallpaper applied")
                            busy = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (busy) CircularProgressIndicator() else Text("Set wallpaper")
                }
                OutlinedButton(
                    enabled = !busy,
                    onClick = {
                        busy = true
                        scope.launch {
                            val error = saveToGallery(context, uri, "imported-wallpaper")
                            snackbar.showSnackbar(error ?: "Saved to Pictures/Superiorwalls")
                            busy = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Save to gallery") }
            }
        }
    }
}
