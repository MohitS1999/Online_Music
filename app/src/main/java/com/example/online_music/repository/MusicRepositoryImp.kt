package com.example.online_music.repository

import android.util.Log
import com.example.online_music.model.MusicData
import com.example.online_music.util.UiState
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "MusicRepositoryImp"
class MusicRepositoryImp(
    private val database:FirebaseFirestore
) : MusicRepository {
    private lateinit var musicList:ArrayList<MusicData>

    override suspend fun getMusicSongs(result: (UiState.Success<ArrayList<MusicData>>) -> Unit) {

        musicList = ArrayList()

        database.collection("audioSongs")

            .addSnapshotListener{ snapshots ,e ->{

            }
                for (document in snapshots!!){
                    musicList.add(MusicData(
                        document.data.get("name").toString(),
                        document.data.get("songUrl").toString(),
                        document.data.get("imageUrl").toString(),
                        document.data.get("singerName").toString(),
                        document.data.get("time").toString()))

                }
                Log.d(TAG, "getSongs: $musicList")
                result.invoke(UiState.Success(musicList))
            }

    }
}
