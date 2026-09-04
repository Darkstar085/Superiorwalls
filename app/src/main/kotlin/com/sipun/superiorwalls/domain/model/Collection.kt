package com.sipun.superiorwalls.domain.model

data class Collection(
    val name: String,
    val displayName: String = name,
    val wallpapers: List<Wallpaper> = emptyList(),
) {
    val count: Int get() = wallpapers.size
    val cover: Wallpaper? get() = wallpapers.firstOrNull()
}
