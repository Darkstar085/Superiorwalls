package com.sipun.superiorwalls.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.superiorwalls.data.repository.AppSettingsStore
import com.sipun.superiorwalls.data.repository.ThemeMode

@Composable
fun SettingsScreen(
    store: AppSettingsStore,
) {
    val themeMode by remember(store) { store.observeThemeMode() }
        .collectAsStateWithLifecycle(initialValue = store.themeMode())

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Settings", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Personalize your Superiorwalls experience.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Appearance") },
                            supportingContent = { Text("Choose how the app follows your device theme.") },
                        )
                        ThemeOption(ThemeMode.SYSTEM, themeMode, store::setThemeMode)
                        ThemeOption(ThemeMode.LIGHT, themeMode, store::setThemeMode)
                        ThemeOption(ThemeMode.DARK, themeMode, store::setThemeMode)
                    }
                }
            }
            item {
                Text(
                    "Superiorwalls",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "Wallpaper browsing, collections, favorites, downloads, sharing, and system wallpaper tools.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ThemeOption(
    mode: ThemeMode,
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
) {
    val icon = when (mode) {
        ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
        ThemeMode.LIGHT -> Icons.Default.LightMode
        ThemeMode.DARK -> Icons.Default.DarkMode
    }
    val title = when (mode) {
        ThemeMode.SYSTEM -> "System default"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        ListItem(
            headlineContent = { Text(title) },
            leadingContent = { Icon(icon, contentDescription = null) },
            trailingContent = {
                RadioButton(selected = mode == selected, onClick = { onSelected(mode) })
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
