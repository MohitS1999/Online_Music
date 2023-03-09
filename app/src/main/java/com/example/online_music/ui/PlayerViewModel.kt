package com.example.online_music.ui

import android.annotation.SuppressLint
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.online_music.R
import com.example.online_music.service.MusicService
import com.example.online_music.util.UiState
import com.example.online_music.util.formatDuration
import com.example.online_music.util.setSongPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlin.math.log

private const val TAG = "PlayerViewModel"

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {


    companion object {
        @SuppressLint("StaticFieldLeak")
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        var contextMusicPlayer: Context? = null
        private lateinit var serviceConnection: ServiceConnection

    }

    private val _firstSong = MutableLiveData<UiState<Int>>()
    val firstSong: LiveData<UiState<Int>>
        get() = _firstSong

    private val _isServiceBound = MutableLiveData<UiState<Boolean>>()
    val isServiceBound: LiveData<UiState<Boolean>>
        get() = _isServiceBound

    fun startMyService(context: Context) {
        Log.d(TAG, "startMyService: ")
        // for starting service
        contextMusicPlayer = context
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        context.startService(intent)

        Log.d(TAG, "startedtMyService: ")
    }

    fun unbindService(context: Context) {
        Log.d(TAG, "unbindService: ")
        context.unbindService(serviceConnection)
    }


    fun createMediaPlayer(url: String) {
        _firstSong.postValue(UiState.Loading)
        try {
            if (musicService!!.mediaPlayer == null) {
                Log.d(TAG, "createMediaPlayer: mediaplayer creating")
                musicService?.mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                }
            }
            Log.d(TAG, "createMediaPlayer:- ")
            if (musicService?.mediaPlayer?.isPlaying == true) {
                musicService?.mediaPlayer!!.stop()
            }
            musicService?.mediaPlayer!!.reset()
            MusicPlayer.binding.seekBarEnd.text = "loading..."

            musicService?.mediaPlayer?.setDataSource(url)
            musicService?.mediaPlayer?.setOnPreparedListener {
                playMusic()
                setSeekbar()
            }
            musicService?.mediaPlayer?.prepareAsync()
            musicService!!.showNotification(R.drawable.pause_music_icon)
           /* musicService!!.mediaPlayer!!.setOnCompletionListener {
                Log.d(TAG, "onCompletion: ")
                setSongPosition(true)
                createMediaPlayer(MusicPlayer.musicList[MusicPlayer.position].songUrl)
                try {
                    contextMusicPlayer?.let { setContentLayout(it) }
                } catch (e: Exception) {
                    Log.d(TAG, "onCompletion: ${e.printStackTrace()}")
                }
            }*/

        } catch (e: IOException) {
            Log.d(TAG, "initPlayer: ${e.printStackTrace()}")
        }


        _firstSong.postValue(UiState.Success(1))
    }

    fun setSeekbar(){
        MusicPlayer.binding.seekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
        MusicPlayer.binding.seekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
        MusicPlayer.binding.seekBarPA.progress = 0
        MusicPlayer.binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
    }


    fun playMusic() {
        musicService!!.showNotification(R.drawable.pause_music_icon)
        musicService?.mediaPlayer!!.start()
    }

    fun pauseMusic() {
        musicService!!.showNotification(R.drawable.play_music_icon)
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
                createMediaPlayer(MusicPlayer.musicList[MusicPlayer.position].songUrl)
                musicService!!.seekBarSetup()
                Log.d(TAG, "onServiceConnected: ${musicService.toString()}")
                _isServiceBound.postValue(UiState.Success(true))
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d(TAG, "onServiceDisconnected: ")
                musicService = null

            }

        }

    }


    fun setContentLayout(context: Context) {
        Log.d(TAG, "setContentLayout: start")
        Glide.with(context)
            .load(MusicPlayer.musicList.get(MusicPlayer.position).imageUrl)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .error(com.google.android.material.R.drawable.mtrl_ic_error)
            .into(MusicPlayer.binding.imagePlayer)
        MusicPlayer.binding.songNamePlayer.text = MusicPlayer.musicList.get(MusicPlayer.position).songName
        MusicPlayer.binding.singerNamePlayer.text = MusicPlayer.musicList.get(MusicPlayer.position).singerName
        Log.d(TAG, "setContentLayout: finish")
    }


}