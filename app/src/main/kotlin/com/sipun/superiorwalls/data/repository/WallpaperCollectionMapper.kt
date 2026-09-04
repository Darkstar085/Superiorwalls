package com.sipun.superiorwalls.data.repository

import com.sipun.superiorwalls.domain.model.Collection
import com.sipun.superiorwalls.domain.model.Wallpaper

object WallpaperCollectionMapper {
    fun build(wallpapers: List<Wallpaper>): List<Collection> {
        val grouped = linkedMapOf<String, Pair<String, MutableList<Wallpaper>>>()
        wallpapers.forEach { wallpaper ->
            wallpaper.collections.orEmpty()
                .replace("|", ",")
                .split(",")
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .forEach { name ->
                    val key = name.lowercase()
                    val current = grouped[key]?.second ?: mutableListOf()
                    current += wallpaper
                    grouped[key] = name to current
                }
        }
        return grouped.values.map { (name, items) ->
            Collection(
                name = name,
                displayName = name.replaceFirstChar { it.uppercase() },
                wallpapers = items.distinctBy { it.url },
            )
        }
    }
}
