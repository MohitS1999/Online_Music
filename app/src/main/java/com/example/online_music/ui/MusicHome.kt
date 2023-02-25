package com.example.online_music.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.online_music.R
import com.example.online_music.adapter.MusicAdapter
import com.example.online_music.databinding.FragmentMusicHomeBinding
import com.example.online_music.model.MusicData
import com.example.online_music.util.UiState
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MusicHome"
@AndroidEntryPoint
class MusicHome : Fragment() {
    private lateinit var binding: FragmentMusicHomeBinding
    private lateinit var musicList:ArrayList<MusicData>
    private lateinit var musicAdapter:MusicAdapter
    private val viewModel by viewModels<MusicViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentMusicHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        musicList = ArrayList()

        observeSongs()

        binding.shuffleBtn.setOnClickListener {
            findNavController().navigate(R.id.action_musicHome_to_musicPlayer,Bundle().apply {  })
        }
        binding.favoriteBtn.setOnClickListener {
            findNavController().navigate(R.id.action_musicHome_to_favoriteFragment,Bundle().apply {  })
        }
    }

    private fun observeSongs() {
        viewModel.getSongs.observe(viewLifecycleOwner){
            when(it){
                is UiState.Success -> {
                    for (data in it.data) musicList.add(data)
                    Log.d(TAG, "observeSongs: $musicList")
                    updateRecyclerView()
                }
                is UiState.Loading ->{}
                is UiState.Failure -> {}
            }
        }
    }

    private fun updateRecyclerView() {
        Log.d(TAG, "updateRecyclerView: ${musicList.size}")
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(20)
        binding.musicRV.layoutManager = LinearLayoutManager(context)
        musicAdapter = MusicAdapter(requireActivity(),musicList,::onSongClicked)
        binding.musicRV.adapter = musicAdapter

    }
    private fun onSongClicked(list:ArrayList<MusicData>,pos:Int){
        val bundle = Bundle()
        Log.d(TAG, "onSongClicked list: $list")
        Log.d(TAG, "onSongClicked: $pos")
        bundle.putInt("pos",pos)
        bundle.putSerializable("musicList",list as java.io.Serializable)
        findNavController().navigate(R.id.action_musicHome_to_musicPlayer,bundle)
    }

}