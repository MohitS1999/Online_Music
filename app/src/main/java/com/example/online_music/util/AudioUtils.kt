package com.example.online_music.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

import com.example.online_music.ui.MusicPlayer
import java.util.concurrent.TimeUnit
private val TAG = "Audio Utils"

fun formatDuration(duration: Long):String{
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS)) -
            minutes* TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
    return String.format("%02d:%02d",minutes,seconds)

}

fun getBitmapFromUrl(url: String,context: Context): Bitmap? {
    return try {
        val options = RequestOptions()
            .override(200, 200)
            .diskCacheStrategy(DiskCacheStrategy.ALL)

        Glide.with(context)
            .asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(options)
            .submit()

            .get()

    } catch (e: Exception) {
        null
    }
}

fun setSongPosition(increment: Boolean){
    if (!MusicPlayer.repeat){
        if (increment){
            if(MusicPlayer.musicList.size-1 == MusicPlayer.position) MusicPlayer.position = 0
            else ++MusicPlayer.position
        }else{
            if (MusicPlayer.position == 0) MusicPlayer.position = MusicPlayer.musicList.size - 1
            else --MusicPlayer.position
        }
    }

}
