package com.example.online_music.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.online_music.MainActivity
import com.example.online_music.MyApplication
import com.example.online_music.R
import com.example.online_music.ui.MusicPlayer
import com.example.online_music.ui.NowPlaying
import com.example.online_music.ui.PlayerViewModel
import com.example.online_music.util.formatDuration
import com.example.online_music.util.getBitmapFromUrl
import java.io.IOException

private const val TAG = "MusicService"
class MusicService :Service(), AudioManager.OnAudioFocusChangeListener {
    private var myBinder = MyBinder()
    var mediaPlayer:MediaPlayer ?= null
    private lateinit var runnable: Runnable
    private lateinit var mediaSession:MediaSessionCompat
    lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext,"My Music")
        return myBinder
    }

    inner class MyBinder:Binder(){
        fun currentService() : MusicService {
            return this@MusicService
        }
    }

    fun showNotification(playPauseBtn:Int){
        val bundle = Bundle()
        bundle.putString("onNowPlayedClicked","nowplaying")
       /* val pendingIntent = NavDeepLinkBuilder(baseContext)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.musicHome)
            .setArguments(bundle)
            .createTaskStackBuilder()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            pendingIntent.getPendingIntent(0,PendingIntent.FLAG_IMMUTABLE)
        }else {
            pendingIntent.getPendingIntent(0,PendingIntent.FLAG_CANCEL_CURRENT)
        }*/
        val intent = Intent(baseContext, MainActivity::class.java)

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }


        val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)



        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext,0,prevIntent,flag)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext,0,playIntent,flag)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext,0,nextIntent,flag)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext,0,exitIntent,flag)



        val bitmap = getBitmapFromUrl(MusicPlayer.musicList[MusicPlayer.position].imageUrl,baseContext)
        val notification = NotificationCompat.Builder(baseContext,MyApplication.CHANNEL_ID)
            .setContentTitle(MusicPlayer.musicList[MusicPlayer.position].songName)
            .setContentText(MusicPlayer.musicList[MusicPlayer.position].singerName)
            .setSmallIcon(R.drawable.music_icon)
            .setLargeIcon(bitmap)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.previous_music_icon,"previous",prevPendingIntent)
            .addAction(playPauseBtn,"play",playPendingIntent)
            .addAction(R.drawable.next_music_icon,"next",nextPendingIntent)
            .addAction(R.drawable.exit_icon,"exit",exitPendingIntent)
            .setContentIntent(contentIntent)
            .build()




        startForeground(101,notification)

    }

    fun createMediaPlayer(url: String) {
        try {
            if (PlayerViewModel.musicService!!.mediaPlayer == null) {
                Log.d(TAG, "createMediaPlayer:in music service")
                PlayerViewModel.musicService?.mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                }
            }
            Log.d(TAG, "createMediaPlayer: ")
            if (PlayerViewModel.musicService?.mediaPlayer?.isPlaying == true) {
                PlayerViewModel.musicService?.mediaPlayer!!.stop()
            }
            PlayerViewModel.musicService?.mediaPlayer!!.reset()
            MusicPlayer.binding.seekBarEnd.text = "loading..."

            PlayerViewModel.musicService?.mediaPlayer?.setDataSource(url)
            PlayerViewModel.musicService?.mediaPlayer?.setOnPreparedListener {
                setSeekbar()
            }
            PlayerViewModel.musicService?.mediaPlayer?.prepareAsync()
            PlayerViewModel.musicService!!.showNotification(R.drawable.pause_music_icon)

        } catch (e: IOException) {
            Log.d(TAG, "initPlayer: ${e.printStackTrace()}")
        }

    }


    fun setSeekbar(){
        MusicPlayer.binding.seekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
        MusicPlayer.binding.seekBarEnd.text = formatDuration(mediaPlayer!!.duration.toLong())
        MusicPlayer.binding.seekBarPA.progress = 0
        MusicPlayer.binding.seekBarPA.max = mediaPlayer!!.duration
    }

    fun seekBarSetup(){
        runnable = Runnable {
            MusicPlayer.binding.seekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            MusicPlayer.binding.seekBarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,400)
        }
        // when this runnable will start i.e. time is 0sec mean it will start immediate
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }



    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0){
            Log.d(TAG, "onAudioFocusChange: Pause Music")
            MusicPlayer.binding.playPauseMusicBtn.setImageResource(R.drawable.play_music_icon)
            NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.play_pa_icon)
            showNotification(R.drawable.play_music_icon)
            MusicPlayer.isPlaying = false
            mediaPlayer!!.pause()
        }else{
            Log.d(TAG, "onAudioFocusChange: Play Music")
            MusicPlayer.binding.playPauseMusicBtn.setImageResource(R.drawable.pause_music_icon)
            NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.pause_pa_icon)
            showNotification(R.drawable.pause_music_icon)
            MusicPlayer.isPlaying = true
            mediaPlayer!!.start()
        }
    }
}