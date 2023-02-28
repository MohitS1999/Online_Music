package com.example.online_music.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.online_music.model.MusicData
import com.example.online_music.repository.PlayerRepository
import com.example.online_music.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

private const val TAG = "PlayerViewModel"
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: PlayerRepository
) : ViewModel() {

    private val _firstSong = MutableLiveData<UiState<Int>>()
    val firstSong: LiveData<UiState<Int>>
        get() = _firstSong

    init {


    }

    fun playFirstSong(url:String){
        Log.d(TAG, "playFirstSong: $url")
        _firstSong.value = UiState.Loading
        repository.initPlayer(url){
            Log.d(TAG, "playFirstSong: $it")
            _firstSong.value = it
        }
    }

    fun playMusic(){
        repository.play()
    }
    fun pauseMusic(){
        repository.pause()
    }

    fun destroy(){
        repository.destroy()
    }






}