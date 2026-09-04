package com.sipun.superiorwalls.features.system

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val GALLERY_SUBDIRECTORY = "Superiorwalls"

suspend fun loadBitmap(context: Context, source: Any): Bitmap? = withContext(Dispatchers.IO) {
    val request = ImageRequest.Builder(context)
        .data(source)
        .allowHardware(false)
        .build()
    val result = ImageLoader(context).execute(request)
    (result as? SuccessResult)?.image?.toBitmap()
}

suspend fun setAsWallpaper(context: Context, source: Any): String? = withContext(Dispatchers.IO) {
    val bitmap = loadBitmap(context, source) ?: return@withContext "Could not load wallpaper"
    runCatching {
        WallpaperManager.getInstance(context).setBitmap(
            bitmap,
            null,
            true,
            WallpaperManager.FLAG_SYSTEM,
        )
    }.fold(
        onSuccess = { null },
        onFailure = { it.message ?: "Could not apply wallpaper" },
    )
}

suspend fun saveToGallery(context: Context, source: Any, displayName: String): String? = withContext(Dispatchers.IO) {
    val bitmap = loadBitmap(context, source) ?: return@withContext "Could not load wallpaper"
    val safeName = displayName
        .replace(Regex("[^A-Za-z0-9._-]"), "_")
        .trimEnd('.')
        .ifBlank { "wallpaper" }
    val fileName = if (safeName.endsWith(".jpg", ignoreCase = true)) safeName else "$safeName.jpg"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + "/" + GALLERY_SUBDIRECTORY,
        )
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        ?: return@withContext "Could not create gallery item"

    runCatching {
        resolver.openOutputStream(uri)?.use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)) {
                "Could not encode wallpaper"
            }
        } ?: error("Could not open gallery output")
        resolver.update(
            uri,
            ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
            null,
            null,
        )
        null
    }.getOrElse {
        resolver.delete(uri, null, null)
        it.message ?: "Could not save wallpaper"
    }
}
