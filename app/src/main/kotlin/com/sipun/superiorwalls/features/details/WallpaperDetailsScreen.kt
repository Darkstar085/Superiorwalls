package com.sipun.superiorwalls.features.details

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
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
            !collectionName.isNullOrBlank() -> wallpapers.filter {
                collectionName in it.collections.orEmpty()
            }
            else -> wallpapers
        }
    }
    val currentIndex = viewerWallpapers.indexOfFirst { it.url == wallpaper.url }
    val previous = currentIndex.takeIf { it > 0 }?.let(viewerWallpapers::get)
    val next = currentIndex.takeIf { it >= 0 && it < viewerWallpapers.lastIndex }
        ?.let(viewerWallpapers::get)

    fun setSystemBarsVisible(visible: Boolean) {
        showBars = visible
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
                if (visible) show(WindowInsetsCompat.Type.systemBars())
                else hide(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    DisposableEffect(activity) {
        setSystemBarsVisible(true)
        onDispose { setSystemBarsVisible(true) }
    }

    BackHandler {
        if (showInfo) showInfo = false else onBack()
    }

    val painter = rememberAsyncImagePainter(model = wallpaper.url)
    LaunchedEffect(painter.state) {
        val result = painter.state as? AsyncImagePainter.State.Success ?: return@LaunchedEffect
        paletteColors = extractPalette(result.result.image.toBitmap())
    }

    Scaffold(
        containerColor = colorResource(R.color.viewer_background),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (showBars) {
                ViewerActionBar(
                    isFavorite = isFavorite,
                    busy = busy,
                    onInfo = { showInfo = true },
                    onSave = {
                        busy = true
                        scope.launch {
                            try {
                                snackbar.showSnackbar(
                                    saveToGallery(context, wallpaper.url, wallpaper.name)
                                        ?: context.getString(R.string.viewer_saved),
                                )
                            } finally {
                                busy = false
                            }
                        }
                    },
                    onApply = {
                        busy = true
                        scope.launch {
                            try {
                                snackbar.showSnackbar(
                                    setAsWallpaper(context, wallpaper.url)
                                        ?: context.getString(R.string.viewer_applied),
                                )
                            } finally {
                                busy = false
                            }
                        }
                    },
                    onFavorite = { favorites.setFavorite(wallpaper.url, !isFavorite) },
                    onShare = {
                        val author = wallpaper.author?.takeIf { it.isNotBlank() }
                            ?: context.getString(R.string.viewer_unknown_author)
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        context.getString(
                                            R.string.viewer_share_text,
                                            wallpaper.name,
                                            author,
                                            wallpaper.url,
                                        ),
                                    )
                                },
                                context.getString(R.string.viewer_share_title),
                            ),
                        )
                    },
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(showBars) {
                    detectTapGestures(
                        onTap = { setSystemBarsVisible(!showBars) },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painter,
                contentDescription = wallpaper.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )

            if (showBars) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(dimensionResource(R.dimen.viewer_top_padding))
                        .align(Alignment.TopStart)
                        .size(dimensionResource(R.dimen.viewer_navigation_button_size)),
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.viewer_back),
                        tint = colorResource(R.color.viewer_overlay_content),
                    )
                }
                ViewerNavigationButton(
                    enabled = previous != null,
                    previous = true,
                    onClick = { previous?.let(onWallpaperChange) },
                    modifier = Modifier.align(Alignment.CenterStart),
                )
                ViewerNavigationButton(
                    enabled = next != null,
                    previous = false,
                    onClick = { next?.let(onWallpaperChange) },
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
        }
    }

    if (showInfo) {
        ModalBottomSheet(
            onDismissRequest = { showInfo = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(dimensionResource(R.dimen.viewer_sheet_corner_radius)),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            WallpaperInfoSheet(
                wallpaper = wallpaper,
                paletteColors = paletteColors,
                onCopyColor = { colorInt ->
                    val hex = "#${colorInt.toString(16).padStart(6, '0').uppercase()}"
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("color", hex))
                    scope.launch {
                        snackbar.showSnackbar(
                            context.getString(R.string.viewer_color_copied, hex),
                        )
                    }
                },
                onClose = { showInfo = false },
            )
        }
    }
}

@Composable
private fun ViewerActionBar(
    isFavorite: Boolean,
    busy: Boolean,
    onInfo: () -> Unit,
    onSave: () -> Unit,
    onApply: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = dimensionResource(R.dimen.viewer_action_elevation),
    ) {
        BottomAppBar(
            containerColor = Color.Transparent,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = dimensionResource(R.dimen.viewer_content_padding),
            ),
        ) {
            ViewerAction(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.viewer_info),
                icon = Icons.Default.Info,
                onClick = onInfo,
            )
            ViewerAction(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.viewer_save),
                icon = Icons.Default.Download,
                onClick = onSave,
                enabled = !busy,
            )
            ViewerAction(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.viewer_apply),
                icon = Icons.Default.Wallpaper,
                onClick = onApply,
                enabled = !busy,
            )
            ViewerAction(
                modifier = Modifier.weight(1f),
                label = if (isFavorite) {
                    stringResource(R.string.viewer_unfavorite)
                } else {
                    stringResource(R.string.viewer_favorite)
                },
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                onClick = onFavorite,
                enabled = !busy,
            )
            ViewerAction(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.viewer_share),
                icon = Icons.Default.Share,
                onClick = onShare,
                enabled = !busy,
            )
        }
    }
}

@Composable
private fun ViewerAction(
    modifier: Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            if (enabled) {
                Icon(icon, contentDescription = label)
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimensionResource(R.dimen.viewer_action_icon_size)),
                )
            }
        }
        Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable
private fun ViewerNavigationButton(
    enabled: Boolean,
    previous: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier.padding(horizontal = dimensionResource(R.dimen.viewer_content_padding)),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = dimensionResource(R.dimen.viewer_navigation_elevation),
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(dimensionResource(R.dimen.viewer_navigation_button_size)),
        ) {
            Icon(
                if (previous) Icons.Default.ArrowBack else Icons.Default.ArrowForward,
                contentDescription = stringResource(
                    if (previous) R.string.viewer_previous else R.string.viewer_next,
                ),
            )
        }
    }
}

@Composable
private fun WallpaperInfoSheet(
    wallpaper: Wallpaper,
    paletteColors: List<Int>,
    onCopyColor: (Int) -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.viewer_sheet_padding)),
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.viewer_action_spacing),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.viewer_details),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.viewer_back),
                )
            }
        }
        InfoRow(stringResource(R.string.viewer_name), wallpaper.name)
        InfoRow(
            stringResource(R.string.viewer_author),
            wallpaper.author?.takeIf { it.isNotBlank() }
                ?: stringResource(R.string.viewer_unknown_author),
        )
        wallpaper.dimensions?.takeIf { it.isNotBlank() }?.let {
            InfoRow(stringResource(R.string.viewer_dimensions), it)
        }
        wallpaper.size?.takeIf { it > 0 }?.let {
            InfoRow(stringResource(R.string.viewer_size), formatBytes(it))
        }
        wallpaper.copyright?.takeIf { it.isNotBlank() }?.let {
            InfoRow(stringResource(R.string.viewer_copyright), it)
        }

        if (paletteColors.isNotEmpty()) {
            Spacer(Modifier.height(dimensionResource(R.dimen.viewer_action_spacing)))
            Text(
                stringResource(R.string.viewer_colors),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                stringResource(R.string.viewer_copy_color_hint),
                style = MaterialTheme.typography.bodyMedium,
            )
            paletteColors.chunked(3).forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.viewer_color_swatches_spacing),
                    ),
                ) {
                    rowColors.forEach { colorInt ->
                        val color = Color(colorInt)
                        val contentColor = if (color.luminance() > 0.55f) {
                            colorResource(R.color.viewer_swatch_dark_content)
                        } else {
                            colorResource(R.color.viewer_swatch_light_content)
                        }
                        Surface(
                            onClick = { onCopyColor(colorInt) },
                            modifier = Modifier
                                .weight(1f)
                                .height(dimensionResource(R.dimen.viewer_color_swatch_height)),
                            shape = RoundedCornerShape(
                                dimensionResource(R.dimen.viewer_action_spacing),
                            ),
                            color = color,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "#${colorInt.toString(16).padStart(6, '0').uppercase()}",
                                    color = contentColor,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }
                    repeat(3 - rowColors.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.viewer_sheet_padding)))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.viewer_action_spacing),
        ),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1.5f),
        )
    }
}

private suspend fun extractPalette(bitmap: Bitmap): List<Int> = withContext(Dispatchers.Default) {
    Palette.from(bitmap)
        .maximumColorCount(6)
        .generate()
        .swatches
        .sortedByDescending { it.population }
        .take(6)
        .map { it.rgb }
}
