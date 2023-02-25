package com.example.online_music.repository

import com.example.online_music.model.MusicData
import com.example.online_music.util.UiState

interface MusicRepository {

    suspend fun getMusicSongs(result:(UiState.Success<ArrayList<MusicData>>) -> Unit)
}