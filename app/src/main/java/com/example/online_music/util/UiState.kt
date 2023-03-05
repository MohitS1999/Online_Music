package com.example.online_music.util

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

sealed class UiState<out T> {
    object Loading: UiState<Nothing>()
    data class Success<out T>(val data: T): UiState<T>()
    data class Failure(val error: String?): UiState<Nothing>()
}

fun getBitmapFromUrl(url: String,context: Context): Bitmap? {
    return try {
        val options = RequestOptions()
            .override(200, 200)
            .diskCacheStrategy(DiskCacheStrategy.ALL)

        Glide.with(context)
            .asBitmap()
            .load(url)
            .apply(options)
            .submit()
            .get()
    } catch (e: Exception) {
        null
    }
}