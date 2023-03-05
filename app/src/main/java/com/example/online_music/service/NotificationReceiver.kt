package com.example.online_music.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.online_music.MyApplication
import com.example.online_music.R
import com.example.online_music.model.setSongPosition
import com.example.online_music.ui.MusicPlayer
import com.example.online_music.ui.PlayerViewModel
import kotlin.math.log
import kotlin.system.exitProcess

private const val TAG = "NotificationReceiver"
class NotificationReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action){
            MyApplication.PREVIOUS -> prevNextSong(false,context!!)
            MyApplication.NEXT -> prevNextSong(true,context!!)
            MyApplication.PLAY -> {
                if (MusicPlayer.isPlaying) pauseMusic()
                else playMusic()
            }
            MyApplication.EXIT -> {
                PlayerViewModel.musicService!!.stopForeground(Service.STOP_FOREGROUND_DETACH)
                PlayerViewModel.musicService = null
                exitProcess(1)
            }
        }

    }

    private fun playMusic(){
        MusicPlayer.isPlaying = true
        PlayerViewModel.musicService!!.mediaPlayer!!.start()
        PlayerViewModel.musicService!!.showNotification(R.drawable.pause_music_icon)
        MusicPlayer.binding.playPauseMusicBtn.setImageResource(R.drawable.pause_music_icon)
    }

    private fun pauseMusic(){
        MusicPlayer.isPlaying = false
        PlayerViewModel.musicService!!.mediaPlayer!!.pause()
        PlayerViewModel.musicService!!.showNotification(R.drawable.play_music_icon)
        MusicPlayer.binding.playPauseMusicBtn.setImageResource(R.drawable.play_music_icon)
    }

    private fun prevNextSong(increment: Boolean, context: Context){
        Log.d(TAG, "prevNextSong: previous-next")
        setSongPosition(increment)
        Log.d(TAG, "prevNextSong: prepare the musicplayer")
        PlayerViewModel.musicService!!.createMediaPlayer(MusicPlayer.musicList[MusicPlayer.position].songUrl)

        Log.d(TAG, "prevNextSong: set image and title ")
        Glide.with(context)
            .load(MusicPlayer.musicList.get(MusicPlayer.position).imageUrl)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .error(com.google.android.material.R.drawable.mtrl_ic_error)
            .into(MusicPlayer.binding.imagePlayer)
        MusicPlayer.binding.songNamePlayer.text = MusicPlayer.musicList.get(MusicPlayer.position).songName
        MusicPlayer.binding.singerNamePlayer.text = MusicPlayer.musicList.get(MusicPlayer.position).singerName
        playMusic()
    }


}