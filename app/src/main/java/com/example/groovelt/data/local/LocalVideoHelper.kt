package com.example.groovelt.data.local

import com.example.groovelt.utils.Constants

object LocalVideoHelper {
    private val videoList = Constants.videoList

    fun getAllVideo() = videoList

    fun getVideosByIds(ids: MutableList<Int>) = videoList.filter { it.id in ids }
}