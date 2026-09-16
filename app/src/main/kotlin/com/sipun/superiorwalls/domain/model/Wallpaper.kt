package com.sipun.superiorwalls.domain.model

import com.google.gson.annotations.SerializedName

data class Wallpaper(
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("author")
    val author: String? = "",
    @SerializedName(value = "thumbnail", alternate = ["thumbUrl", "thumb", "url-thumb"])
    val thumbnail: String? = "",
    @SerializedName(value = "collections", alternate = ["categories", "category"])
    val collections: String? = "",
    @SerializedName(value = "dimensions", alternate = ["dimension"])
    val dimensions: String? = "",
    @SerializedName("copyright")
    val copyright: String? = "",
    @SerializedName("downloadable")
    val downloadable: Boolean? = true,
    @SerializedName("size")
    val size: Long? = 0L,
)
