package com.example.online_music.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.online_music.ui.MusicPlayer
import com.example.online_music.util.UiState
import java.io.IOException
import java.util.logging.Handler
import kotlin.coroutines.coroutineContext

private const val TAG = "PlayerRepositoryImp"
class PlayerRepositoryImp : PlayerRepository {

    private var mediaPlayer: MediaPlayer? = null


    fun initiableMediaPlayerObject() {
        Log.d(TAG, "initiableMediaPlayerObject: ")
        try{
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }
        }catch (e:Exception){
            Log.d(TAG, "initiableMediaPlayerObject: -> ${e.printStackTrace()}")
        }

        Log.d(TAG, "initiableMediaPlayerObject: finish ")
    }

    override fun initPlayer(url: String, result: (UiState<Int>) -> Unit) {
        if (mediaPlayer == null){
            initiableMediaPlayerObject()
        }
        if (mediaPlayer?.isPlaying == true){
            mediaPlayer!!.stop()
        }
        mediaPlayer!!.reset()
        try {
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.setOnPreparedListener { mediaPlayer?.start() }
            mediaPlayer?.prepareAsync()
        }catch (e: IOException){
            Log.d(TAG, "initPlayer: ${e.printStackTrace()}")
        }


        Log.d(TAG, "initPlayer: finish")
        result.invoke(UiState.Success(1))

    }

    override fun play() {
        mediaPlayer!!.start()
    }

    override fun pause() {
        mediaPlayer!!.pause()
    }

    override fun destroy() {
        mediaPlayer!!.release()
        mediaPlayer = null
    }


}