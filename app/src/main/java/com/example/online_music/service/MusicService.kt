package com.example.online_music.service

import android.app.Application
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.example.online_music.MyApplication
import com.example.online_music.R
import com.example.online_music.ui.MusicPlayer
import com.example.online_music.ui.PlayerViewModel
import com.example.online_music.util.getBitmapFromUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "MusicService"
class MusicService :Service() {
    private var myBinder = MyBinder()
    var mediaPlayer:MediaPlayer ?= null

    private lateinit var mediaSession:MediaSessionCompat

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

        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext,0,exitIntent,PendingIntent.FLAG_UPDATE_CURRENT)



        val bitmap = getBitmapFromUrl(MusicPlayer.musicList[MusicPlayer.position].imageUrl,baseContext)
        val image = bitmap ?: R.drawable.jmdphoto
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
            .build()



        startForeground(101,notification)

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }
}