package com.example.online_music.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.online_music.MyApplication
import com.example.online_music.R
import com.example.online_music.ui.MusicPlayer
import com.example.online_music.ui.NowPlaying
import com.example.online_music.ui.PlayerViewModel
import com.example.online_music.util.formatDuration
import com.example.online_music.util.getBitmapFromUrl
import com.example.online_music.util.setSongPosition
import kotlin.math.log
import kotlin.system.exitProcess

private const val TAG = "NotificationReceiver"

class NotificationReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            MyApplication.PREVIOUS -> prevNextSong(false, context!!)
            MyApplication.NEXT -> prevNextSong(true, context!!)
            MyApplication.PLAY -> {
                if (MusicPlayer.isPlaying) pauseMusic()
                else playMusic()
            }
            MyApplication.EXIT -> {
                PlayerViewModel.musicService!!.mediaPlayer!!.release()
                PlayerViewModel.musicService!!.mediaPlayer = null
                PlayerViewModel.musicService!!.stopForeground(true)
                PlayerViewModel.musicService = null
                exitProcess(1)
            }
        }

    }

    private fun playMusic() {
        Log.d(TAG, "playMusic: ")
        MusicPlayer.isPlaying = true
        PlayerViewModel.musicService?.mediaPlayer?.setOnPreparedListener {
            PlayerViewModel.musicService!!.mediaPlayer!!.start()
            setSeekbar()
        }

        PlayerViewModel.musicService!!.showNotification(R.drawable.pause_music_icon)
        MusicPlayer.binding.playPauseMusicBtn.setImageResource(R.drawable.pause_music_icon)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.pause_pa_icon)
    }

    fun setSeekbar() {
        MusicPlayer.binding.seekBarStart.text =
            formatDuration(PlayerViewModel.musicService!!.mediaPlayer!!.currentPosition.toLong())
        MusicPlayer.binding.seekBarEnd.text =
            formatDuration(PlayerViewModel.musicService!!.mediaPlayer!!.duration.toLong())
        MusicPlayer.binding.seekBarPA.progress = 0
        MusicPlayer.binding.seekBarPA.max = PlayerViewModel.musicService!!.mediaPlayer!!.duration
    }

    private fun pauseMusic() {
        MusicPlayer.isPlaying = false
        PlayerViewModel.musicService!!.showNotification(R.drawable.play_music_icon)
        MusicPlayer.binding.playPauseMusicBtn.setImageResource(R.drawable.play_music_icon)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.play_pa_icon)
        PlayerViewModel.musicService!!.mediaPlayer!!.pause()
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        Log.d(TAG, "prevNextSong: setsongposition")
        setSongPosition(increment)
        Log.d(TAG, "prevNextSong: prepare the musicplayer")
        PlayerViewModel.musicService!!.createMediaPlayer(MusicPlayer.musicList[MusicPlayer.position].songUrl)

        Log.d(TAG, "prevNextSong: set image and title for musix player ")
        Glide.with(context)
            .load(MusicPlayer.musicList.get(MusicPlayer.position).imageUrl)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .error(com.google.android.material.R.drawable.mtrl_ic_error)
            .into(MusicPlayer.binding.imagePlayer)
        MusicPlayer.binding.songNamePlayer.text =
            MusicPlayer.musicList.get(MusicPlayer.position).songName
        MusicPlayer.binding.singerNamePlayer.text =
            MusicPlayer.musicList.get(MusicPlayer.position).singerName
        val img = getBitmapFromUrl(MusicPlayer.musicList[MusicPlayer.position].imageUrl, context)
        NowPlaying.binding.songImpNP.setImageBitmap(img)
        NowPlaying.binding.songMusicNamePA.text =
            MusicPlayer.musicList[MusicPlayer.position].songName
        playMusic()
    }


}