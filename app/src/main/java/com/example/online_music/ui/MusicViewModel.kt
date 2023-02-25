package com.example.online_music.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.online_music.model.MusicData
import com.example.online_music.repository.MusicRepository
import com.example.online_music.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import javax.inject.Inject

private const val TAG = "MusicViewModel"
@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: MusicRepository
): ViewModel() {

    private val _getSongs = MutableLiveData<UiState<ArrayList<MusicData>>>()
    val getSongs:LiveData<UiState<ArrayList<MusicData>>>
            get() = _getSongs

    init{
        getAllAudios()
    }

    private fun getAllAudios() {
        _getSongs.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO){
            repository.getMusicSongs {
                Log.d(TAG, "getAllAudios: ${it.data}")
                _getSongs.value = it
            }
        }
    }

}