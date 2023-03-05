package com.example.online_music.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.online_music.MyApplication
import com.example.online_music.R
import com.example.online_music.ui.MusicPlayer
import com.example.online_music.ui.PlayerViewModel
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action){
            MyApplication.PREVIOUS -> Toast.makeText(context,"Previous clicked",Toast.LENGTH_SHORT).show()
            MyApplication.NEXT -> {

            }
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
}