package com.sipun.superiorwalls.features.details

import android.app.Activity
import android.app.WallpaperManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import coil3.request.ImageRequest
import coil3.toBitmap
import com.sipun.superiorwalls.R
import com.sipun.superiorwalls.data.repository.FavoriteWallpaperStore
import com.sipun.superiorwalls.data.repository.WallpaperCollectionMapper
import com.sipun.superiorwalls.domain.model.Wallpaper
import com.sipun.superiorwalls.features.system.saveToGallery
import com.sipun.superiorwalls.features.system.setAsWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    direction: String = "none",
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
    var busyLabel by remember { mutableStateOf<String?>(null) }
    var showInfo by remember { mutableStateOf(false) }
    var showApplySheet by remember { mutableStateOf(false) }
    var showBars by remember { mutableStateOf(true) }
    var showSwipeHint by rememberSaveable(direction) { mutableStateOf(direction == "none") }
    var imageRetryKey by remember(wallpaper.url) { mutableStateOf(0) }
    var imageState by remember(wallpaper.url) { mutableStateOf(ImageState.Loading) }
    var paletteColors by remember(wallpaper.url) { mutableStateOf<List<Int>>(emptyList()) }
    val isFavorite = wallpaper.url in favoriteUrls
    val viewerWallpapers = remember(wallpapers, favoriteUrls, mode, collectionName) {
        when {
            mode == "favorites" -> wallpapers.filter { it.url in favoriteUrls }
            !collectionName.isNullOrBlank() -> wallpapers.filter { WallpaperCollectionMapper.containsCollection(it.collections, collectionName.orEmpty()) }
            else -> wallpapers
        }
    }
    val index = viewerWallpapers.indexOfFirst { it.url == wallpaper.url }
    LaunchedEffect(direction) {
        if (direction == "none") {
            delay(3000)
            showSwipeHint = false
        }
    }

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
                        onHorizontalDrag = { _, dragAmount ->
                            dragDistance += dragAmount
                            showSwipeHint = false
                        },
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
                model = ImageRequest.Builder(context)
                    .data(wallpaper.url)
                    .memoryCacheKey("${wallpaper.url}:$imageRetryKey")
                    .build(),
                contentDescription = wallpaper.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onLoading = { imageState = ImageState.Loading },
                onSuccess = { state ->
                    imageState = ImageState.Success
                    val bitmap = state.result.image.toBitmap()
                    scope.launch { paletteColors = extractPalette(bitmap) }
                },
                onError = { imageState = ImageState.Error },
            )
            when (imageState) {
                ImageState.Loading -> Surface(
                    modifier = Modifier.align(Alignment.Center),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.42f),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(18.dp).size(28.dp),
                        color = Color.White,
                    )
                }
                ImageState.Error -> Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.72f),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(stringResource(R.string.viewer_image_error), style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(
                            stringResource(R.string.viewer_image_error_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.78f),
                        )
                        androidx.compose.material3.Button(onClick = { imageRetryKey++ }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
                ImageState.Success -> Unit
            }
            if (showBars && viewerWallpapers.size > 1 && index >= 0) {
                Surface(
                    modifier = Modifier
                        .statusBarsPadding()
                        .align(Alignment.TopCenter)
                        .padding(top = dimensionResource(R.dimen.viewer_top_padding)),
                    shape = RoundedCornerShape(20.dp),
                    color = colorResource(R.color.viewer_action_pill_background),
                ) {
                    Text(
                        text = stringResource(R.string.viewer_position, index + 1, viewerWallpapers.size),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        color = colorResource(R.color.viewer_overlay_content),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            if (showBars) {
                Surface(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = dimensionResource(R.dimen.screen_padding), top = dimensionResource(R.dimen.viewer_top_padding))
                        .align(Alignment.TopStart)
                        .size(dimensionResource(R.dimen.viewer_navigation_button_size)),
                    shape = CircleShape,
                    color = colorResource(R.color.viewer_back_button_background),
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.viewer_back),
                            tint = colorResource(R.color.viewer_overlay_content),
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = showBars && showSwipeHint && viewerWallpapers.size > 1 && index >= 0,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center),
            ) {
                Surface(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.58f),
                ) {
                    Text(
                        stringResource(R.string.viewer_swipe_hint),
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            if (showBars) {
                ViewerActionBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    isFavorite = isFavorite,
                    busy = busy,
                    busyLabel = busyLabel,
                    downloadable = wallpaper.downloadable != false,
                    onInfo = { showInfo = true },
                    onSave = {
                        busy = true
                        busyLabel = context.getString(R.string.viewer_save)
                        scope.launch {
                            try { snackbar.showSnackbar(saveToGallery(context, wallpaper.url, wallpaper.name) ?: context.getString(R.string.viewer_saved)) }
                            finally { busy = false; busyLabel = null }
                        }
                    },
                    onApply = { showApplySheet = true },
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
        shape = RoundedCornerShape(
            topStart = dimensionResource(R.dimen.viewer_sheet_corner_radius),
            topEnd = dimensionResource(R.dimen.viewer_sheet_corner_radius),
            bottomStart = 0.dp,
            bottomEnd = 0.dp,
        ),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        WallpaperInfoSheet(wallpaper, paletteColors, { colorInt ->
            val hex = "#${(colorInt and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()}"
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.viewer_color_clipboard_label), hex))
            scope.launch { snackbar.showSnackbar(context.getString(R.string.viewer_color_copied, hex)) }
        }, { showInfo = false })
    }

    if (showApplySheet) ModalBottomSheet(
        onDismissRequest = { if (!busy) showApplySheet = false },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = Color.Black.copy(alpha = 0.76f),
        contentColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.58f),
        dragHandle = {
            Surface(
                modifier = Modifier.size(width = 40.dp, height = 4.dp),
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.65f),
            ) {}
        },
    ) {
        ApplyWallpaperSheet(
            busy = busy,
            onHome = {
                busyLabel = context.getString(R.string.viewer_apply)
                applyWallpaper(context, wallpaper.url, WallpaperManager.FLAG_SYSTEM, scope, snackbar) { isBusy ->
                    busy = isBusy
                    if (!isBusy) { busyLabel = null; showApplySheet = false }
                }
            },
            onLock = {
                busyLabel = context.getString(R.string.viewer_apply)
                applyWallpaper(context, wallpaper.url, WallpaperManager.FLAG_LOCK, scope, snackbar) { isBusy ->
                    busy = isBusy
                    if (!isBusy) { busyLabel = null; showApplySheet = false }
                }
            },
            onBoth = {
                busyLabel = context.getString(R.string.viewer_apply)
                applyWallpaper(context, wallpaper.url, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK, scope, snackbar) { isBusy ->
                    busy = isBusy
                    if (!isBusy) { busyLabel = null; showApplySheet = false }
                }
            },
            onCancel = { if (!busy) showApplySheet = false },
        )
    }
}

private fun applyWallpaper(
    context: Context,
    source: Any,
    which: Int,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbar: SnackbarHostState,
    setBusy: (Boolean) -> Unit,
) {
    setBusy(true)
    scope.launch {
        try {
            snackbar.showSnackbar(setAsWallpaper(context, source, which) ?: context.getString(R.string.viewer_applied))
        } finally {
            setBusy(false)
        }
    }
}

@Composable
private fun ApplyWallpaperSheet(
    busy: Boolean,
    onHome: () -> Unit,
    onLock: () -> Unit,
    onBoth: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.viewer_set_wallpaper),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            WallpaperTarget(
                label = stringResource(R.string.viewer_home_screen),
                icon = Icons.Default.Home,
                enabled = !busy,
                onClick = onHome,
            )
            WallpaperTarget(
                label = stringResource(R.string.viewer_lock_screen),
                icon = Icons.Default.Lock,
                enabled = !busy,
                onClick = onLock,
            )
            WallpaperTarget(
                label = stringResource(R.string.viewer_both),
                icon = Icons.Default.Wallpaper,
                enabled = !busy,
                onClick = onBoth,
            )
        }
        if (busy) {
            Spacer(Modifier.height(20.dp))
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = 8.dp),
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.White.copy(alpha = 0.18f)) {}
        }
        Spacer(Modifier.height(16.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.12f),
            onClick = onCancel,
            enabled = !busy,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.viewer_cancel), style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun WallpaperTarget(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp),
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.12f),
            onClick = onClick,
            enabled = enabled,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
private fun ViewerActionBar(
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    busy: Boolean,
    busyLabel: String?,
    downloadable: Boolean,
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
            ViewerAction(Modifier.weight(1f), stringResource(R.string.viewer_info), Icons.Default.Info, onInfo, !busy, busyLabel)
            ViewerAction(Modifier.weight(1f), stringResource(R.string.viewer_save), Icons.Default.Download, onSave, !busy && downloadable, busyLabel)
            ViewerAction(Modifier.weight(1f), stringResource(R.string.viewer_apply), Icons.Default.Wallpaper, onApply, !busy && downloadable, busyLabel)
            ViewerAction(Modifier.weight(1f), stringResource(R.string.viewer_favorite), if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, onFavorite, !busy, busyLabel)
        }
    }
}

@Composable
private fun ViewerAction(modifier: Modifier, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, enabled: Boolean = true, busyLabel: String? = null) {
    val isBusy = busyLabel == label
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, enabled = enabled) {
            if (isBusy) CircularProgressIndicator(modifier = Modifier.size(dimensionResource(R.dimen.viewer_action_icon_size)))
            else Icon(icon, contentDescription = label)
        }
        Text(if (isBusy) if (label == stringResource(R.string.viewer_save)) stringResource(R.string.viewer_saving) else stringResource(R.string.viewer_applying) else label, style = MaterialTheme.typography.labelMedium, maxLines = 1, modifier = Modifier.offset(y = (-6).dp))
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
        wallpaper.collections?.let { WallpaperCollectionMapper.collectionNames(it).joinToString(", ").takeIf(String::isNotBlank) }?.let {
            InfoRow(stringResource(R.string.viewer_collection), it)
        }
        wallpaper.dimensions?.takeIf { it.isNotBlank() }?.let {
            InfoRow(stringResource(R.string.viewer_dimensions), it)
            aspectRatio(it)?.let { ratio -> InfoRow(stringResource(R.string.viewer_aspect_ratio), ratio) }
        }
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
                            Box(contentAlignment = Alignment.Center) { Text("#${(colorInt and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()}", color = contentColor, style = MaterialTheme.typography.labelLarge) }
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

private enum class ImageState { Loading, Success, Error }

private fun aspectRatio(dimensions: String): String? {
    val match = Regex("""(\d+)\s*[x×]\s*(\d+)""").find(dimensions) ?: return null
    val width = match.groupValues[1].toLongOrNull() ?: return null
    val height = match.groupValues[2].toLongOrNull() ?: return null
    if (width <= 0L || height <= 0L) return null
    fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
    val divisor = gcd(width, height)
    return "${width / divisor}:${height / divisor}"
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
