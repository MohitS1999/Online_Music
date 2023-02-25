package com.example.online_music.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.online_music.R
import com.example.online_music.databinding.FragmentMusicPlayerBinding
import com.example.online_music.model.MusicData
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MusicPlayer"

@AndroidEntryPoint
class MusicPlayer : Fragment() {
    
    private lateinit var musicList:ArrayList<MusicData>
    private var position:Int = 0

    private  lateinit var binding: FragmentMusicPlayerBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMusicPlayerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        musicList = (arguments?.getSerializable("musicList") as? ArrayList<MusicData>)!!
        position = arguments?.getInt("pos")!!

        Log.d(TAG, "onViewCreated: $musicList")
        Log.d(TAG, "onViewCreated: $position")
    }
}