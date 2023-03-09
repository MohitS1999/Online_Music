package com.example.online_music.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.online_music.ui.MusicPlayer
import java.util.concurrent.TimeUnit

private const val TAG = "MusicData"
data class MusicData(
    val songName:String = "",
    val songUrl:String = "",
    val imageUrl:String ="",
    val singerName:String ="",
    val time:String ="",
    var isFavourite: Boolean = false
) : java.io.Serializable




