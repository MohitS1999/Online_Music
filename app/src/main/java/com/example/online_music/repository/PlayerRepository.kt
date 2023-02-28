package com.example.online_music.repository

import com.example.online_music.util.UiState

interface PlayerRepository {
    fun initPlayer(url:String,result:(UiState<Int>) -> Unit)
    fun play()
    fun pause()
    fun destroy()
}