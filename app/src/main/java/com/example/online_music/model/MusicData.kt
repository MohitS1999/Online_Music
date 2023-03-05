package com.example.online_music.model

import java.util.concurrent.TimeUnit

data class MusicData(
    val songName:String = "",
    val songUrl:String = "",
    val imageUrl:String ="",
    val singerName:String ="",
    val time:String =""
) : java.io.Serializable

fun formatDuration(duration: Long):String{
    val minutes = TimeUnit.MINUTES.convert(duration,TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration,TimeUnit.MILLISECONDS)) -
            minutes*TimeUnit.SECONDS.convert(1,TimeUnit.MINUTES)
    return String.format("%2d:%2d",minutes,seconds)

}

