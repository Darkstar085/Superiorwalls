package com.sipun.superiorwalls.data.repository

import com.sipun.superiorwalls.domain.model.Wallpaper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperCollectionMapperTest {
    @Test
    fun `groups comma and pipe separated collections case insensitively`() {
        val wallpapers = listOf(
            wallpaper("one", "Nature, travel"),
            wallpaper("two", "nature|Travel"),
        )

        val collections = WallpaperCollectionMapper.build(wallpapers)

        assertEquals(listOf("Nature", "Travel"), collections.map { it.displayName })
        assertEquals(2, collections[0].count)
        assertEquals(2, collections[1].count)
    }

    @Test
    fun `ignores blank collection names and duplicate wallpaper urls`() {
        val duplicate = wallpaper("same", "nature, ,nature")

        val collections = WallpaperCollectionMapper.build(listOf(duplicate, duplicate))

        assertEquals(1, collections.size)
        assertEquals(1, collections.single().count)
        assertTrue(collections.single().wallpapers.single().url.endsWith("/same"))
    }

    private fun wallpaper(name: String, collections: String) = Wallpaper(
        name = name,
        url = "https://example.com/$name",
        collections = collections,
    )
}
