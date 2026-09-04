package com.sipun.superiorwalls.features.details

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.toBitmap
import com.sipun.superiorwalls.R
import com.sipun.superiorwalls.data.repository.FavoriteWallpaperStore
import com.sipun.superiorwalls.domain.model.Wallpaper
import com.sipun.superiorwalls.features.system.saveToGallery
import com.sipun.superiorwalls.features.system.setAsWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperDetailsScreen(
    wallpaper: Wallpaper,
    wallpapers: List<Wallpaper>,
    favoriteUrls: Set<String>,
    mode: String = "all",
    collectionName: String? = null,
    onWallpaperChange: (Wallpaper) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val favorites = remember { FavoriteWallpaperStore(context) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var busy by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var showBars by remember { mutableStateOf(true) }
    var paletteColors by remember(wallpaper.url) { mutableStateOf<List<Int>>(emptyList()) }
    val isFavorite = wallpaper.url in favoriteUrls
    val viewerWallpapers = remember(wallpapers, favoriteUrls, mode, collectionName) {
        when {
            mode == "favorites" -> wallpapers.filter { it.url in favoriteUrls }
            !collectionName.isNullOrBlank() -> wallpapers.filter { collectionName in it.collections.orEmpty() }
            else -> wallpapers
        }
    }
    val index = viewerWallpapers.indexOfFirst { it.url == wallpaper.url }

    fun setSystemBarsVisible(visible: Boolean) {
        showBars = visible
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
                if (visible) show(WindowInsetsCompat.Type.systemBars()) else hide(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    DisposableEffect(activity) {
        setSystemBarsVisible(true)
        onDispose { setSystemBarsVisible(true) }
    }
    BackHandler { if (showInfo) showInfo = false else onBack() }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(wallpaper.url, index, viewerWallpapers.size) {
                    var dragDistance = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount -> dragDistance += dragAmount },
                        onDragEnd = {
                            val threshold = 120f
                            when {
                                dragDistance <= -threshold && index >= 0 && index < viewerWallpapers.lastIndex -> onWallpaperChange(viewerWallpapers[index + 1])
                                dragDistance >= threshold && index > 0 -> onWallpaperChange(viewerWallpapers[index - 1])
                            }
                            dragDistance = 0f
                        },
                        onDragCancel = { dragDistance = 0f },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = wallpaper.url,
                contentDescription = wallpaper.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                onSuccess = { state ->
                    val bitmap = state.result.image.toBitmap()
                    scope.launch { paletteColors = extractPalette(bitmap) }
                },
            )
            if (showBars) {
                Surface(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(dimensionResource(R.dimen.viewer_top_padding))
                        .align(Alignment.TopStart)
                        .size(dimensionResource(R.dimen.viewer_navigation_button_size)),
                    shape = CircleShape,
                    color = colorResource(R.color.viewer_back_button_background),
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.viewer_back),
                            tint = colorResource(R.color.viewer_overlay_content),
                        )
                    }
                }
            }
            if (showBars) {
                ViewerActionBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    isFavorite = isFavorite,
                    busy = busy,
                    onInfo = { showInfo = true },
                    onSave = {
                        busy = true
                        scope.launch {
                            try { snackbar.showSnackbar(saveToGallery(context, wallpaper.url, wallpaper.name) ?: context.getString(R.string.viewer_saved)) }
                            finally { busy = false }
                        }
                    },
                    onApply = {
                        busy = true
                        scope.launch {
                            try { snackbar.showSnackbar(setAsWallpaper(context, wallpaper.url) ?: context.getString(R.string.viewer_applied)) }
                            finally { busy = false }
                        }
                    },
                    onFavorite = { favorites.setFavorite(wallpaper.url, !isFavorite) },
                )
            }
            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = dimensionResource(R.dimen.viewer_snackbar_bottom_padding)),
            )
        }
    }

    if (showInfo) ModalBottomSheet(
        onDismissRequest = { showInfo = false },
        sheetState = sheetState,
        shape = RoundedCornerShape(dimensionResource(R.dimen.viewer_sheet_corner_radius)),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        WallpaperInfoSheet(wallpaper, paletteColors, { colorInt ->
            val hex = "#${colorInt.toString(16).padStart(6, '0').uppercase()}"
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.viewer_color_clipboard_label), hex))
            scope.launch { snackbar.showSnackbar(context.getString(R.string.viewer_color_copied, hex)) }
        }, { showInfo = false })
    }
}

@Composable
private fun ViewerActionBar(
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    busy: Boolean,
    onInfo: () -> Unit,
    onSave: () -> Unit,
    onApply: () -> Unit,
    onFavorite: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(0.84f)
            .navigationBarsPadding()
            .padding(
                horizontal = dimensionResource(R.dimen.viewer_action_pill_horizontal_padding),
                vertical = dimensionResource(R.dimen.viewer_action_pill_vertical_padding),
            ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.viewer_action_pill_corner_radius)),
        color = colorResource(R.color.viewer_action_pill_background),
        tonalElevation = dimensionResource(R.dimen.viewer_action_pill_elevation),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(R.dimen.viewer_action_pill_inner_horizontal_padding),
                    vertical = dimensionResource(R.dimen.viewer_action_pill_inner_vertical_padding),
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ViewerAction(Modifier.weight(1f), stringResource(R.string.viewer_info), Icons.Default.Info, onInfo)
            ViewerAction(Modifier.weight(1f), stringResource(R.string.viewer_save), Icons.Default.Download, onSave, !busy)
            ViewerAction(Modifier.weight(1f), stringResource(R.string.viewer_apply), Icons.Default.Wallpaper, onApply, !busy)
            ViewerAction(Modifier.weight(1f), stringResource(R.string.viewer_favorite), if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, onFavorite, !busy)
        }
    }
}

@Composable
private fun ViewerAction(modifier: Modifier, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, enabled: Boolean = true) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, enabled = enabled) {
            if (enabled) Icon(icon, contentDescription = label)
            else CircularProgressIndicator(modifier = Modifier.size(dimensionResource(R.dimen.viewer_action_icon_size)))
        }
        Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable
private fun WallpaperInfoSheet(wallpaper: Wallpaper, paletteColors: List<Int>, onCopyColor: (Int) -> Unit, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.viewer_sheet_padding)), verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.viewer_action_spacing))) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.viewer_details), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            Surface(
                modifier = Modifier.size(dimensionResource(R.dimen.viewer_sheet_close_button_size)),
                shape = CircleShape,
                color = colorResource(R.color.viewer_sheet_close_button_background),
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.viewer_back))
                }
            }
        }
        InfoRow(stringResource(R.string.viewer_name), wallpaper.name)
        InfoRow(stringResource(R.string.viewer_author), wallpaper.author?.takeIf { it.isNotBlank() } ?: stringResource(R.string.viewer_unknown_author))
        wallpaper.dimensions?.takeIf { it.isNotBlank() }?.let { InfoRow(stringResource(R.string.viewer_dimensions), it) }
        wallpaper.size?.takeIf { it > 0 }?.let { InfoRow(stringResource(R.string.viewer_size), formatBytes(it)) }
        wallpaper.copyright?.takeIf { it.isNotBlank() }?.let { InfoRow(stringResource(R.string.viewer_copyright), it) }
        if (paletteColors.isNotEmpty()) {
            Spacer(Modifier.height(dimensionResource(R.dimen.viewer_action_spacing)))
            Text(stringResource(R.string.viewer_colors), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.viewer_copy_color_hint), style = MaterialTheme.typography.bodyMedium)
            paletteColors.chunked(3).forEach { rowColors ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.viewer_color_swatches_spacing))) {
                    rowColors.forEach { colorInt ->
                        val color = Color(colorInt)
                        val contentColor = if (color.luminance() > 0.55f) colorResource(R.color.viewer_swatch_dark_content) else colorResource(R.color.viewer_swatch_light_content)
                        Surface(onClick = { onCopyColor(colorInt) }, modifier = Modifier.weight(1f).height(dimensionResource(R.dimen.viewer_color_swatch_height)), shape = RoundedCornerShape(dimensionResource(R.dimen.viewer_action_spacing)), color = color) {
                            Box(contentAlignment = Alignment.Center) { Text("#${colorInt.toString(16).padStart(6, '0').uppercase()}", color = contentColor, style = MaterialTheme.typography.labelLarge) }
                        }
                    }
                    repeat(3 - rowColors.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.viewer_sheet_padding)))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.viewer_action_spacing))) {
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1.5f))
    }
}

private suspend fun extractPalette(bitmap: Bitmap): List<Int> = withContext(Dispatchers.Default) {
    val softwareBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
        bitmap.copy(Bitmap.Config.ARGB_8888, false)
    } else {
        bitmap
    }
    val maxDimension = 256
    val sample = if (softwareBitmap.width > maxDimension || softwareBitmap.height > maxDimension) {
        val scale = minOf(maxDimension.toFloat() / softwareBitmap.width, maxDimension.toFloat() / softwareBitmap.height)
        Bitmap.createScaledBitmap(softwareBitmap, (softwareBitmap.width * scale).toInt().coerceAtLeast(1), (softwareBitmap.height * scale).toInt().coerceAtLeast(1), true)
    } else softwareBitmap
    try {
        Palette.from(sample).maximumColorCount(6).generate().swatches.sortedByDescending { it.population }.take(6).map { it.rgb }
    } finally {
        if (sample !== softwareBitmap) sample.recycle()
        if (softwareBitmap !== bitmap) softwareBitmap.recycle()
    }
}
