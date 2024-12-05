package com.example.groovelt.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoModel(
    val id: Int = 0,
    val title: String = "",
    val url: String = "",
    val desc: String = "",
    val thumbnailUrl: String = ""
) : Parcelable