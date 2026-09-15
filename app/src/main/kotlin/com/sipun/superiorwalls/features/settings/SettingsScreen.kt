package com.sipun.superiorwalls.features.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.superiorwalls.AppContainer
import com.sipun.superiorwalls.R
import com.sipun.superiorwalls.data.repository.AppSettingsStore
import com.sipun.superiorwalls.data.repository.InterfaceSettings
import com.sipun.superiorwalls.data.repository.NotificationSettings
import com.sipun.superiorwalls.data.repository.StorageSettings
import com.sipun.superiorwalls.data.repository.ThemeMode
import com.sipun.superiorwalls.features.notifications.cancelWallpaperNotifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(store: AppSettingsStore) {
    val context = LocalContext.current
    val themeMode by store.observeThemeMode().collectAsStateWithLifecycle(initialValue = store.themeMode())
    val interfaceSettings by store.observeInterfaceSettings().collectAsStateWithLifecycle(initialValue = store.interfaceSettings())
    val storageSettings by store.observeStorageSettings().collectAsStateWithLifecycle(initialValue = store.storageSettings())
    val notificationSettings by store.observeNotificationSettings().collectAsStateWithLifecycle(initialValue = store.notificationSettings())
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (!granted) store.setNotificationsEnabled(false) }
    val listState = rememberLazyListState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var cacheSize by remember(context) { mutableStateOf(context.getString(R.string.settings_cache_size_initial)) }
    val scope = rememberCoroutineScope()
    val hasTappableNavigationBar = remember(context) { hasTappableNavigationBar(context) }
    val versionName = remember(context) { getAppVersionName(context) }

    LaunchedEffect(Unit) { cacheSize = withContext(Dispatchers.IO) { calculateCacheSize(context) } }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = dimensionResource(R.dimen.screen_padding),
            end = dimensionResource(R.dimen.screen_padding),
            bottom = if (LocalConfiguration.current.screenWidthDp < 840)
                dimensionResource(R.dimen.screen_padding) +
                    dimensionResource(R.dimen.bottom_nav_height) +
                    dimensionResource(R.dimen.bottom_nav_margin)
            else dimensionResource(R.dimen.screen_padding)
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.settings_section_spacing)),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.screen_header_padding)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.compact_spacing)),
            ) {
                Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.settings_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            SettingsSection(stringResource(R.string.settings_appearance), stringResource(R.string.settings_appearance_summary)) {
                ThemePreferenceRow(themeMode) { showThemeDialog = true }
                SettingsSwitchRow(stringResource(R.string.settings_amoled_theme), stringResource(R.string.settings_amoled_theme_summary), interfaceSettings.amoledTheme, store::setAmoledTheme, Icons.Default.DarkMode)
                SettingsSwitchRow(stringResource(R.string.settings_material_you), stringResource(R.string.settings_material_you_summary), interfaceSettings.materialYou, store::setMaterialYou, Icons.Default.Palette, android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
                if (hasTappableNavigationBar) SettingsSwitchRow(stringResource(R.string.settings_color_navigation_bar), stringResource(R.string.settings_color_navigation_bar_summary), interfaceSettings.colorNavigationBar, store::setColorNavigationBar, Icons.Default.Navigation)
                SettingsSwitchRow(stringResource(R.string.settings_animations), stringResource(R.string.settings_animations_summary), interfaceSettings.animationsEnabled, store::setAnimationsEnabled, Icons.Default.Animation)
            }
        }
        item {
            SettingsSection(stringResource(R.string.settings_storage), stringResource(R.string.settings_storage_summary)) {
                SettingsSwitchRow(stringResource(R.string.settings_download_wifi_only), stringResource(R.string.settings_download_wifi_only_summary), storageSettings.downloadOnWifiOnly, store::setDownloadOnWifiOnly, Icons.Default.Wifi)
                SettingsSwitchRow(stringResource(R.string.settings_scale_to_fit), stringResource(R.string.settings_scale_to_fit_summary), storageSettings.scaleToFit, store::setScaleToFit, Icons.Default.FitScreen)
                ListItem(headlineContent = { Text(stringResource(R.string.settings_wallpapers_saved_to)) }, supportingContent = { Text(stringResource(R.string.settings_wallpapers_saved_to_summary)) }, leadingContent = { SettingsIcon(Icons.Default.Folder) })
                Spacer(Modifier.height(8.dp))
                ListItem(modifier = Modifier.clickable { scope.launch(Dispatchers.IO) { AppContainer.clearLocalData(context); withContext(Dispatchers.Main) { cacheSize = calculateCacheSize(context) } } }, headlineContent = { Text(stringResource(R.string.settings_clear_app_data)) }, supportingContent = { Text(stringResource(R.string.settings_clear_app_data_summary, cacheSize)) }, leadingContent = { SettingsIcon(Icons.Default.DeleteSweep, destructive = true) })
            }
        }
        item {
            SettingsSection(stringResource(R.string.settings_notifications), stringResource(R.string.settings_notifications_summary)) {
                SettingsSwitchRow(stringResource(R.string.settings_enable_notifications), stringResource(R.string.settings_enable_notifications_summary), notificationSettings.enabled, { enabled ->
                    store.setNotificationsEnabled(enabled)
                    if (enabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else if (!enabled) cancelWallpaperNotifications(context)
                }, Icons.Default.Notifications)
            }
        }
        item {
            SettingsSection(stringResource(R.string.settings_about)) {
                ListItem(modifier = Modifier.clickable { showAboutDialog = true }, headlineContent = { Text(stringResource(R.string.app_name)) }, supportingContent = { Text(stringResource(R.string.settings_about_summary)) }, leadingContent = { SettingsIcon(Icons.Default.Info) })
            }
        }
    }

    if (showThemeDialog) ThemeSelectionDialog(themeMode, { store.setThemeMode(it); showThemeDialog = false }) { showThemeDialog = false }
    if (showAboutDialog) AboutDialog(versionName) { showAboutDialog = false }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)
        ),
    ) {
        content()
    }
}

@Composable
private fun SettingsSection(title: String, summary: String? = null, content: @Composable () -> Unit) {
    SettingsCard {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.compact_spacing))) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                summary?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) }
            }
            content()
        }
    }
}

@Composable
private fun SettingsIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, destructive: Boolean = false) {
    val container = if (destructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
    val tint = if (destructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
    Surface(Modifier.size(40.dp), RoundedCornerShape(12.dp), color = container) { Icon(icon, contentDescription = null, Modifier.padding(8.dp), tint = tint) }
}

@Composable
private fun SettingsSwitchRow(title: String, summary: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean = true) {
    ListItem(leadingContent = { SettingsIcon(icon) }, headlineContent = { Text(title) }, supportingContent = { Text(summary) }, trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled) })
}

@Composable
private fun ThemePreferenceRow(selected: ThemeMode, onClick: () -> Unit) {
    ListItem(modifier = Modifier.clickable(onClick = onClick), leadingContent = { SettingsIcon(when (selected) { ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness; ThemeMode.LIGHT -> Icons.Default.LightMode; ThemeMode.DARK -> Icons.Default.DarkMode }) }, headlineContent = { Text(stringResource(R.string.settings_theme)) }, supportingContent = { Text(themeModeLabel(selected)) })
}

@Composable
private fun ThemeSelectionDialog(selected: ThemeMode, onSelected: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    val options = listOf(ThemeMode.SYSTEM to stringResource(R.string.settings_system), ThemeMode.LIGHT to stringResource(R.string.settings_light), ThemeMode.DARK to stringResource(R.string.settings_dark))
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().widthIn(max = 420.dp).padding(horizontal = 24.dp), RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 6.dp) {
            Column(Modifier.padding(vertical = 12.dp)) {
                Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
                options.forEach { (mode, label) -> ListItem(modifier = Modifier.clickable { onSelected(mode) }, headlineContent = { Text(label) }, leadingContent = { RadioButton(selected = mode == selected, onClick = { onSelected(mode) }) }) }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End).padding(horizontal = 16.dp)) { Text(stringResource(R.string.settings_done)) }
            }
        }
    }
}

@Composable
private fun AboutDialog(versionName: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().widthIn(max = 400.dp).padding(horizontal = 28.dp), RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 6.dp) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.superiorwalls_wordmark), stringResource(R.string.app_name), Modifier.fillMaxWidth().padding(horizontal = 18.dp))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.about_tagline), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Text(stringResource(R.string.about_tagline_os), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.about_version, versionName), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(stringResource(R.string.about_made_with_prefix))
                    Spacer(Modifier.size(5.dp))
                    Text("❤️", Modifier.graphicsLayer(alpha = 0.92f))
                    Spacer(Modifier.size(5.dp))
                    Text(stringResource(R.string.about_made_with_suffix))
                }
            }
        }
    }
}

@Composable
private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> stringResource(R.string.settings_system)
    ThemeMode.LIGHT -> stringResource(R.string.settings_light)
    ThemeMode.DARK -> stringResource(R.string.settings_dark)
}

private fun getAppVersionName(context: Context): String {
    val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0)) else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    return packageInfo.versionName ?: context.getString(R.string.settings_unknown_version)
}

private fun calculateCacheSize(context: Context): String {
    fun sizeOf(file: java.io.File): Long = if (file.isDirectory) file.listFiles()?.sumOf(::sizeOf) ?: 0L else file.length()
    val bytes = sizeOf(context.cacheDir) + (context.externalCacheDir?.let(::sizeOf) ?: 0L)
    val kb = bytes / 1024.0
    return if (kb > 1024) String.format("%.2f MB", kb / 1024.0) else String.format("%.2f KB", kb)
}

private fun hasTappableNavigationBar(context: Context): Boolean {
    val resourceId = context.resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
    return if (resourceId != 0) context.resources.getInteger(resourceId) != 2 else true
}
