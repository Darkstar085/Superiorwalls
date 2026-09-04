package com.sipun.superiorwalls.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.superiorwalls.R
import com.sipun.superiorwalls.data.repository.AppSettingsStore
import com.sipun.superiorwalls.data.repository.InterfaceSettings
import com.sipun.superiorwalls.data.repository.ThemeMode
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(store: AppSettingsStore) {
    val themeMode by remember(store) { store.observeThemeMode() }
        .collectAsStateWithLifecycle(initialValue = store.themeMode())
    val interfaceSettings by remember(store) { store.observeInterfaceSettings() }
        .collectAsStateWithLifecycle(initialValue = store.interfaceSettings())
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsContent(
            themeMode = themeMode,
            interfaceSettings = interfaceSettings,
            onThemeSelected = store::setThemeMode,
            onAmoledChanged = store::setAmoledTheme,
            onMaterialYouChanged = store::setMaterialYou,
            onNavigationBarChanged = store::setColorNavigationBar,
            onAnimationsChanged = store::setAnimationsEnabled,
            listState = listState,
        )
        SettingsCollapsingHeader(listState = listState)
    }
}

@Composable
private fun SettingsContent(
    themeMode: ThemeMode,
    interfaceSettings: InterfaceSettings,
    onThemeSelected: (ThemeMode) -> Unit,
    onAmoledChanged: (Boolean) -> Unit,
    onMaterialYouChanged: (Boolean) -> Unit,
    onNavigationBarChanged: (Boolean) -> Unit,
    onAnimationsChanged: (Boolean) -> Unit,
    listState: LazyListState,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 156.dp,
            start = dimensionResource(R.dimen.screen_padding),
            end = dimensionResource(R.dimen.screen_padding),
            bottom = dimensionResource(R.dimen.screen_padding),
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.settings_section_spacing)),
    ) {
        item {
            SettingsSection(
                title = stringResource(R.string.settings_appearance),
                summary = stringResource(R.string.settings_appearance_summary),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    ThemeModeSelector(
                        selected = themeMode,
                        onSelected = onThemeSelected,
                    )
                }
            }
        }

        item {
            SettingsSection(
                title = stringResource(R.string.settings_interface),
                summary = stringResource(R.string.settings_interface_summary),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_amoled_theme),
                        summary = stringResource(R.string.settings_amoled_theme_summary),
                        checked = interfaceSettings.amoledTheme,
                        onCheckedChange = onAmoledChanged,
                        icon = Icons.Default.DarkMode,
                    )
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_material_you),
                        summary = stringResource(R.string.settings_material_you_summary),
                        checked = interfaceSettings.materialYou,
                        onCheckedChange = onMaterialYouChanged,
                        icon = Icons.Default.LightMode,
                        enabled = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S,
                    )
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_color_navigation_bar),
                        summary = stringResource(R.string.settings_color_navigation_bar_summary),
                        checked = interfaceSettings.colorNavigationBar,
                        onCheckedChange = onNavigationBarChanged,
                        icon = Icons.Default.SettingsBrightness,
                    )
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_animations),
                        summary = stringResource(R.string.settings_animations_summary),
                        checked = interfaceSettings.animationsEnabled,
                        onCheckedChange = onAnimationsChanged,
                        icon = Icons.Default.SettingsBrightness,
                    )
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_about)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.app_name)) },
                        supportingContent = { Text(stringResource(R.string.settings_about_summary)) },
                        leadingContent = {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.padding(8.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        },
                    )
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_credits)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
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
        }

        item {
            Text(
                stringResource(R.string.settings_version),
                modifier = Modifier.padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
) {
    ListItem(
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (enabled) 1f else 0.55f),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = if (enabled) 1f else 0.55f),
                )
            }
        },
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        },
    )
}

@Composable
private fun SettingsCollapsingHeader(listState: LazyListState) {
    val density = LocalDensity.current
    val collapseDistancePx = with(density) { 112.dp.toPx() }
    val scrollOffsetPx = listState.firstVisibleItemIndex * 1000f + listState.firstVisibleItemScrollOffset
    val progress = (scrollOffsetPx / collapseDistancePx).coerceIn(0f, 1f)
    val targetLeftPx = with(density) { 20.dp.toPx() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp),
    ) {
        val titleWidthPx = with(density) { 92.dp.toPx() }
        val centeredLeftPx = (with(density) { maxWidth.toPx() } - titleWidthPx) / 2f
        val titleX = (targetLeftPx - centeredLeftPx) * progress
        val titleY = with(density) { lerp(62.dp, 8.dp, progress).toPx() }

        Text(
            text = stringResource(R.string.settings_title),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(titleX.roundToInt(), titleY.roundToInt())
                },
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = lerp(30.sp, 22.sp, progress),
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )

        Text(
            text = stringResource(R.string.settings_subtitle),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp)
                .offset(y = lerp(100.dp, 42.dp, progress))
                .graphicsLayer {
                    alpha = 1f - progress
                },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    summary: String? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.compact_spacing))) {
        Text(
            title,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
        )
        if (summary != null) {
            Text(
                summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        content()
    }
}

@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
) {
    val options = listOf(
        ThemeOption(ThemeMode.SYSTEM, stringResource(R.string.settings_system), Icons.Default.SettingsBrightness),
        ThemeOption(ThemeMode.LIGHT, stringResource(R.string.settings_light), Icons.Default.LightMode),
        ThemeOption(ThemeMode.DARK, stringResource(R.string.settings_dark), Icons.Default.DarkMode),
    )

    Column(modifier = Modifier.padding(8.dp)) {
        options.forEach { option ->
            val isSelected = option.mode == selected
            Surface(
                onClick = { onSelected(option.mode) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Text(
                        option.title,
                        modifier = Modifier.weight(1f),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (isSelected) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary,
                        ) {}
                    }
                }
            }
        }
    }
}

private data class ThemeOption(
    val mode: ThemeMode,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
