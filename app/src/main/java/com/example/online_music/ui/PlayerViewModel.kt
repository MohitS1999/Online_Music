package com.example.online_music.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.online_music.service.MusicService
import com.example.online_music.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlin.math.log

private const val TAG = "PlayerViewModel"

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {


    companion object {
        private var musicService: MusicService? = null
        private lateinit var serviceConnection: ServiceConnection
    }

    private val _firstSong = MutableLiveData<UiState<Int>>()
    val firstSong: LiveData<UiState<Int>>
        get() = _firstSong

    private val _isServiceBound = MutableLiveData<UiState<Boolean>>()
    val isServiceBound: LiveData<UiState<Boolean>>
        get() = _isServiceBound

    fun startMyService(context: Context) {
        // for starting service

        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        context.startService(intent)
        Log.d(TAG, "startedtMyService: ")
    }


    fun initialzeMediaPlayer(){
        try{
            Log.d(TAG, "initialize media player: ")
            musicService?.mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }
            Log.d(TAG, "${musicService?.mediaPlayer} ")
        }catch (e:Exception){
            Log.d(TAG, "initiableMediaPlayerObject: -> ${e.printStackTrace()}")
        }
    }
    fun playFirstSong(url: String) {
        _firstSong.postValue(UiState.Loading)
        Log.d(TAG, "playFirstSong: $musicService")

        Log.d(TAG, "playFirstSong: $musicService")
        if (musicService!!.mediaPlayer == null) initialzeMediaPlayer()
        if (musicService?.mediaPlayer?.isPlaying == true) {
            musicService?.mediaPlayer!!.stop()
        }
        musicService?.mediaPlayer!!.reset()
        try {
            musicService?.mediaPlayer?.setDataSource(url)
            musicService?.mediaPlayer?.setOnPreparedListener { musicService?.mediaPlayer?.start() }
            musicService?.mediaPlayer?.prepareAsync()
        } catch (e: IOException) {
            Log.d(TAG, "initPlayer: ${e.printStackTrace()}")
        }


        _firstSong.postValue(UiState.Success(1))
    }

    fun playMusic() {
        musicService?.mediaPlayer!!.start()
    }

    fun pauseMusic() {
        musicService?.mediaPlayer!!.pause()
    }

    fun destroy() {
        musicService?.mediaPlayer!!.release()
        musicService?.mediaPlayer = null
    }


    fun bindToService() {
        _isServiceBound.postValue(UiState.Loading)
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

                val binder = service as MusicService.MyBinder
                musicService = binder.currentService()
                Log.d(TAG, "onServiceConnected: ${musicService.toString()}")
                _isServiceBound.postValue(UiState.Success(true))
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d(TAG, "onServiceDisconnected: ")
                musicService = null

            }

        }
    }


}