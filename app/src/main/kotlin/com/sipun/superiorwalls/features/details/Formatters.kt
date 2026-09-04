package com.sipun.superiorwalls.features.details

import java.util.Locale

internal fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> String.format(
        Locale.getDefault(),
        "%.1f MB",
        bytes / 1024f / 1024f,
    )
    bytes >= 1024L -> String.format(
        Locale.getDefault(),
        "%.0f KB",
        bytes / 1024f,
    )
    else -> "$bytes B"
}
