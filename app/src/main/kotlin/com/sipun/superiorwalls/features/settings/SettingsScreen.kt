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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.superiorwalls.R
import com.sipun.superiorwalls.data.repository.AppSettingsStore
import com.sipun.superiorwalls.data.repository.ThemeMode

@Composable
fun SettingsScreen(store: AppSettingsStore) {
    val themeMode by remember(store) { store.observeThemeMode() }
        .collectAsStateWithLifecycle(initialValue = store.themeMode())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(dimensionResource(R.dimen.screen_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.settings_section_spacing)),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.compact_spacing))) {
                Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.settings_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Text(stringResource(R.string.settings_appearance), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.settings_appearance_summary), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                ThemeOption(ThemeMode.SYSTEM, themeMode, store::setThemeMode)
                ThemeOption(ThemeMode.LIGHT, themeMode, store::setThemeMode)
                ThemeOption(ThemeMode.DARK, themeMode, store::setThemeMode)
            }
        }
        item {
            Text(stringResource(R.string.settings_about), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            Card(Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.app_name)) },
                    supportingContent = { Text(stringResource(R.string.settings_about_summary)) },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                )
            }
        }
        item {
            Text(stringResource(R.string.settings_credits), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            Card(Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_developer_name)) },
                    supportingContent = { Text(stringResource(R.string.settings_developer)) },
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_project_name)) },
                    supportingContent = { Text(stringResource(R.string.settings_project)) },
                )
            }
        }
        item {
            Text(stringResource(R.string.settings_version), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ThemeOption(mode: ThemeMode, selected: ThemeMode, onSelected: (ThemeMode) -> Unit) {
    val icon = when (mode) {
        ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
        ThemeMode.LIGHT -> Icons.Default.LightMode
        ThemeMode.DARK -> Icons.Default.DarkMode
    }
    val title = when (mode) {
        ThemeMode.SYSTEM -> stringResource(R.string.settings_system)
        ThemeMode.LIGHT -> stringResource(R.string.settings_light)
        ThemeMode.DARK -> stringResource(R.string.settings_dark)
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.compact_spacing))) {
        ListItem(
            headlineContent = { Text(title) },
            leadingContent = { Icon(icon, contentDescription = null) },
            trailingContent = { RadioButton(selected = mode == selected, onClick = { onSelected(mode) }) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
