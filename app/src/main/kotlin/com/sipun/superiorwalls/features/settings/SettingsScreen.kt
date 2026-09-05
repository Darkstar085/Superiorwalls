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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
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
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(store: AppSettingsStore) {
    val context = LocalContext.current
    val themeMode by remember(store) { store.observeThemeMode() }.collectAsStateWithLifecycle(initialValue = store.themeMode())
    val interfaceSettings by remember(store) { store.observeInterfaceSettings() }.collectAsStateWithLifecycle(initialValue = store.interfaceSettings())
    val storageSettings by remember(store) { store.observeStorageSettings() }.collectAsStateWithLifecycle(initialValue = store.storageSettings())
    val notificationSettings by remember(store) { store.observeNotificationSettings() }.collectAsStateWithLifecycle(initialValue = store.notificationSettings())
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) store.setNotificationsEnabled(false)
    }
    val listState = rememberLazyListState()
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsContent(themeMode, interfaceSettings, storageSettings, notificationSettings, store::setThemeMode, store::setAmoledTheme, store::setMaterialYou, store::setColorNavigationBar, store::setAnimationsEnabled, store::setHighQualityThumbnails, store::setDownloadOnWifiOnly, store::setScaleToFit, { enabled ->
            store.setNotificationsEnabled(enabled)
            if (enabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else if (!enabled) {
                cancelWallpaperNotifications(context)
            }
        }, listState)
        SettingsCollapsingHeader(listState)
    }
}

@Composable
private fun SettingsContent(themeMode: ThemeMode, interfaceSettings: InterfaceSettings, storageSettings: StorageSettings, notificationSettings: NotificationSettings, onThemeSelected: (ThemeMode) -> Unit, onAmoledChanged: (Boolean) -> Unit, onMaterialYouChanged: (Boolean) -> Unit, onNavigationBarChanged: (Boolean) -> Unit, onAnimationsChanged: (Boolean) -> Unit, onHighQualityChanged: (Boolean) -> Unit, onWifiOnlyChanged: (Boolean) -> Unit, onScaleToFitChanged: (Boolean) -> Unit, onNotificationsChanged: (Boolean) -> Unit, listState: LazyListState) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var cacheSize by remember { mutableStateOf("0 KB") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hasTappableNavigationBar = remember(context) { hasTappableNavigationBar(context) }
    val versionName = remember(context) { getAppVersionName(context) }
    val useBottomNavigation = LocalConfiguration.current.screenWidthDp < 840
    val bottomContentPadding = if (useBottomNavigation) {
        dimensionResource(R.dimen.screen_padding) + dimensionResource(R.dimen.bottom_nav_height) + dimensionResource(R.dimen.bottom_nav_margin)
    } else {
        dimensionResource(R.dimen.screen_padding)
    }
    LaunchedEffect(Unit) { cacheSize = withContext(Dispatchers.IO) { calculateCacheSize(context) } }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 156.dp, start = dimensionResource(R.dimen.screen_padding), end = dimensionResource(R.dimen.screen_padding), bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.settings_section_spacing)),
    ) {
        item {
            SettingsSection(title = stringResource(R.string.settings_appearance), summary = stringResource(R.string.settings_appearance_summary)) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    ThemePreferenceRow(themeMode) { showThemeDialog = true }
                    SettingsSwitchRow(stringResource(R.string.settings_amoled_theme), stringResource(R.string.settings_amoled_theme_summary), interfaceSettings.amoledTheme, onAmoledChanged, Icons.Default.DarkMode)
                    SettingsSwitchRow(stringResource(R.string.settings_material_you), stringResource(R.string.settings_material_you_summary), interfaceSettings.materialYou, onMaterialYouChanged, Icons.Default.Palette, enabled = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
                    if (hasTappableNavigationBar) SettingsSwitchRow(stringResource(R.string.settings_color_navigation_bar), stringResource(R.string.settings_color_navigation_bar_summary), interfaceSettings.colorNavigationBar, onNavigationBarChanged, Icons.Default.Navigation)
                    SettingsSwitchRow(stringResource(R.string.settings_animations), stringResource(R.string.settings_animations_summary), interfaceSettings.animationsEnabled, onAnimationsChanged, Icons.Default.Animation)
                }
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_storage), summary = stringResource(R.string.settings_storage_summary)) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    SettingsSwitchRow(stringResource(R.string.settings_high_quality_thumbnails), stringResource(R.string.settings_high_quality_thumbnails_summary), storageSettings.highQualityThumbnails, onHighQualityChanged, Icons.Default.Storage)
                    SettingsSwitchRow(stringResource(R.string.settings_download_wifi_only), stringResource(R.string.settings_download_wifi_only_summary), storageSettings.downloadOnWifiOnly, onWifiOnlyChanged, Icons.Default.Wifi)
                    SettingsSwitchRow(stringResource(R.string.settings_scale_to_fit), stringResource(R.string.settings_scale_to_fit_summary), storageSettings.scaleToFit, onScaleToFitChanged, Icons.Default.FitScreen)
                    ListItem(headlineContent = { Text(stringResource(R.string.settings_wallpapers_saved_to)) }, supportingContent = { Text(stringResource(R.string.settings_wallpapers_saved_to_summary)) }, leadingContent = { Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer) { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) } })
                    ListItem(modifier = Modifier.clickable {
                        scope.launch(Dispatchers.IO) {
                            AppContainer.clearLocalData(context)
                            val size = calculateCacheSize(context)
                            withContext(Dispatchers.Main) { cacheSize = size }
                        }
                    }, headlineContent = { Text(stringResource(R.string.settings_clear_app_data)) }, supportingContent = { Text(stringResource(R.string.settings_clear_app_data_summary, cacheSize)) }, leadingContent = { Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer) { Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.onErrorContainer) } })
                }
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_notifications), summary = stringResource(R.string.settings_notifications_summary)) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    SettingsSwitchRow(stringResource(R.string.settings_enable_notifications), stringResource(R.string.settings_enable_notifications_summary), notificationSettings.enabled, onNotificationsChanged, Icons.Default.Notifications)
                }
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_about)) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    ListItem(
                        modifier = Modifier.clickable { showAboutDialog = true },
                        headlineContent = { Text(stringResource(R.string.app_name)) },
                        supportingContent = { Text(stringResource(R.string.settings_about_summary)) },
                        leadingContent = { Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer) } },
                    )
                }
            }
        }
    }
    if (showThemeDialog) ThemeSelectionDialog(themeMode, { onThemeSelected(it); showThemeDialog = false }, { showThemeDialog = false })
    if (showAboutDialog) AboutDialog(versionName, { showAboutDialog = false })
}

private fun getAppVersionName(context: Context): String {
    val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    return packageInfo.versionName ?: "Unknown"
}

@Composable
private fun AboutDialog(versionName: String, onDismiss: () -> Unit) {
    val heartTransition = rememberInfiniteTransition(label = "heart")
    val heartScale by heartTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "heartScale",
    )
    val heartAlpha by heartTransition.animateFloat(
        initialValue = 0.84f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "heartAlpha",
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp)
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.superiorwalls_wordmark),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.about_tagline),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    stringResource(R.string.about_tagline_os),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.about_version, versionName),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text("Made with", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.size(5.dp))
                    Text(
                        text = "❤️",
                        modifier = Modifier.graphicsLayer {
                            scaleX = heartScale
                            scaleY = heartScale
                            alpha = heartAlpha
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Text("by Sipun", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
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

@Composable
private fun SettingsSwitchRow(title: String, summary: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean = true) {
    ListItem(leadingContent = { Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (enabled) 1f else 0.55f)) { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = if (enabled) 1f else 0.55f)) } }, headlineContent = { Text(title) }, supportingContent = { Text(summary) }, trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled) })
}

@Composable
private fun ThemePreferenceRow(selected: ThemeMode, onClick: () -> Unit) {
    ListItem(modifier = Modifier.clickable(onClick = onClick), leadingContent = { Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) { Icon(imageVector = when (selected) { ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness; ThemeMode.LIGHT -> Icons.Default.LightMode; ThemeMode.DARK -> Icons.Default.DarkMode }, contentDescription = null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer) } }, headlineContent = { Text(stringResource(R.string.settings_theme)) }, supportingContent = { Text(themeModeLabel(selected)) })
}

@Composable
private fun ThemeSelectionDialog(selected: ThemeMode, onSelected: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    val options = listOf(ThemeMode.SYSTEM to stringResource(R.string.settings_system), ThemeMode.LIGHT to stringResource(R.string.settings_light), ThemeMode.DARK to stringResource(R.string.settings_dark))
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp).padding(horizontal = 24.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.settings_theme_summary), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                options.forEach { (mode, label) -> ThemeOptionRow(mode, label, selected == mode) { onSelected(mode) } }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_done)) } }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(mode: ThemeMode, label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant) {
                Icon(imageVector = when (mode) { ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness; ThemeMode.LIGHT -> Icons.Default.LightMode; ThemeMode.DARK -> Icons.Default.DarkMode }, contentDescription = null, modifier = Modifier.padding(8.dp), tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(label, modifier = Modifier.weight(1f).padding(horizontal = 12.dp), style = MaterialTheme.typography.bodyLarge)
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

@Composable
private fun themeModeLabel(mode: ThemeMode): String = when (mode) { ThemeMode.SYSTEM -> stringResource(R.string.settings_system); ThemeMode.LIGHT -> stringResource(R.string.settings_light); ThemeMode.DARK -> stringResource(R.string.settings_dark) }

@Composable
private fun SettingsCollapsingHeader(listState: LazyListState) {
    val density = LocalDensity.current
    val collapseDistancePx = with(density) { 112.dp.toPx() }
    val scrollOffsetPx = listState.firstVisibleItemIndex * 1000f + listState.firstVisibleItemScrollOffset
    val progress = (scrollOffsetPx / collapseDistancePx).coerceIn(0f, 1f)
    val targetLeftPx = with(density) { 20.dp.toPx() }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(148.dp)) {
        val titleWidthPx = with(density) { 92.dp.toPx() }
        val centeredLeftPx = (with(density) { maxWidth.toPx() } - titleWidthPx) / 2f
        val titleX = (targetLeftPx - centeredLeftPx) * progress
        val titleY = with(density) { lerp(62.dp, 8.dp, progress).toPx() }
        Text(stringResource(R.string.settings_title), modifier = Modifier.align(Alignment.TopCenter).offset { IntOffset(titleX.roundToInt(), titleY.roundToInt()) }, style = MaterialTheme.typography.headlineLarge.copy(fontSize = lerp(30.sp, 22.sp, progress)), textAlign = TextAlign.Center, maxLines = 1)
        Text(stringResource(R.string.settings_subtitle), modifier = Modifier.align(Alignment.TopCenter).padding(horizontal = 20.dp).offset(y = lerp(100.dp, 42.dp, progress)).graphicsLayer { alpha = 1f - progress }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
private fun SettingsSection(title: String, summary: String? = null, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.compact_spacing))) {
        Text(title, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
        if (summary != null) Text(summary, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        content()
    }
}
