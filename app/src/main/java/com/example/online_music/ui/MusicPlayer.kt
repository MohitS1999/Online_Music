package com.example.online_music.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.online_music.R
import com.example.online_music.databinding.FragmentMusicPlayerBinding
import com.example.online_music.model.MusicData
import com.example.online_music.util.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_music_player.view.*
import kotlin.math.log

private const val TAG = "MusicPlayer"

@AndroidEntryPoint
class MusicPlayer : Fragment() {
    
    private lateinit var musicList:ArrayList<MusicData>

    private val viewModel by viewModels<PlayerViewModel> ()
    private  lateinit var binding: FragmentMusicPlayerBinding

    companion object{
        private var position:Int = 0
        private val clickOnSongs:String= "songs"
        private var isPlaying:Boolean = false
    }
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




        if (arguments?.getString("onSongClicked").equals(clickOnSongs)){
            Log.d(TAG, "onViewCreated: onsong")
            initializeLayout()
        }

        binding.playPauseMusicBtn.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }

        binding.prevMusicBtn.setOnClickListener {
            prevNextSong(false)
        }
        binding.nextMusicBtn.setOnClickListener {
            prevNextSong(true)
        }

        Log.d(TAG, "onViewCreated: $position")
    }

    private fun observer() {
        viewModel.firstSong.observe(viewLifecycleOwner){
            when(it){
                is UiState.Loading ->{
                    Log.d(TAG, "observer: loading...")
                    binding.playPauseMusicBtn.visibility = View.GONE
                    binding.playProgressBar.visibility = View.VISIBLE
                    binding.prevMusicBtn.isEnabled = false
                    binding.prevMusicBtn.imageAlpha = 75
                    binding.nextMusicBtn.isEnabled = false
                    binding.nextMusicBtn.imageAlpha = 75
                }
                is UiState.Success -> {
                    Log.d(TAG, "observer: Success")
                    binding.playPauseMusicBtn.visibility = View.VISIBLE
                    binding.playProgressBar.visibility = View.GONE
                    binding.prevMusicBtn.isEnabled = true   
                    binding.prevMusicBtn.imageAlpha = 255
                    binding.nextMusicBtn.isEnabled = true
                    binding.nextMusicBtn.imageAlpha = 255
                }
                is UiState.Failure -> {
                    Log.d(TAG, "observer: failure")
                }
            }
        }
    }

    private fun initializeLayout() {
        setContentLayout()
        Log.d(TAG, "initializeLayout: ")

        observer()
        Handler(Looper.getMainLooper()).post {
            viewModel.playFirstSong(musicList.get(position).songUrl)
        }

        isPlaying = true
        binding.playPauseMusicBtn.setImageResource(R.drawable.pause_music_icon)
        Log.d(TAG, "initializeLayout: finsh")
    }

    private fun setContentLayout() {
        Log.d(TAG, "setContentLayout: start")
        Glide.with(this)
            .load(musicList.get(position).imageUrl)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .error(com.google.android.material.R.drawable.mtrl_ic_error)
            .into(binding.imagePlayer)
        binding.songNamePlayer.text = musicList.get(position).songName
        binding.singerNamePlayer.text = musicList.get(position).singerName
        Log.d(TAG, "setContentLayout: finish")
    }

    private fun playMusic(){
        binding.playPauseMusicBtn.setImageResource(R.drawable.pause_music_icon)
        isPlaying = true
        viewModel.playMusic()
    }

    private fun pauseMusic(){
        binding.playPauseMusicBtn.setImageResource(R.drawable.play_music_icon)
        isPlaying = false
        viewModel.pauseMusic()
    }

    private fun prevNextSong(increment:Boolean){

        if (increment){
            setSongPosition(increment)
            initializeLayout()
        }else{
            setSongPosition(increment)
            initializeLayout()
        }
    }

    private fun setSongPosition(increment: Boolean){
        if (increment){
            if(musicList.size-1 == position) position = 0
            else ++position
        }else{
            if (position == 0) position = musicList.size - 1
            else --position
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isPlaying = false
        viewModel.destroy()
    }
}