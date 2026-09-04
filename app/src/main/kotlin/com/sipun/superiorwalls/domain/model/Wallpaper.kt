package com.sipun.superiorwalls.domain.model

data class Wallpaper(
    val name: String,
    val url: String,
    val author: String? = "",
    val thumbnail: String? = "",
    val collections: String? = "",
    val dimensions: String? = "",
    val copyright: String? = "",
    val downloadable: Boolean? = true,
    val size: Long? = 0L,
)
