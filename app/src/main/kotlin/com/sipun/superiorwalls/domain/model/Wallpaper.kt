package com.sipun.superiorwalls.domain.model

import com.google.gson.annotations.SerializedName

data class Wallpaper(
    val name: String,
    val url: String,
    val author: String? = "",
    @SerializedName(value = "thumbnail", alternate = ["thumbUrl", "thumb", "url-thumb"])
    val thumbnail: String? = "",
    @SerializedName(value = "collections", alternate = ["categories", "category"])
    val collections: String? = "",
    @SerializedName(value = "dimensions", alternate = ["dimension"])
    val dimensions: String? = "",
    val copyright: String? = "",
    val downloadable: Boolean? = true,
    val size: Long? = 0L,
)
