package com.example.online_music.ui

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.DynamicsProcessing.Eq
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.online_music.R
import com.example.online_music.databinding.FragmentMusicPlayerBinding
import com.example.online_music.model.MusicData
import com.example.online_music.util.UiState
import com.example.online_music.util.formatDuration
import com.example.online_music.util.setSongPosition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_music_player.*
import kotlinx.android.synthetic.main.fragment_music_player.view.*
import kotlinx.coroutines.*
import kotlin.math.log
import kotlin.system.exitProcess

private const val TAG = "MusicPlayer"

@AndroidEntryPoint
class MusicPlayer : Fragment() {


    private val viewModel by viewModels<PlayerViewModel>()
    companion object {
        lateinit var musicList: ArrayList<MusicData>
        var position: Int = 0
        val clickOnSongs: String = "songs"
        val clickOnShuffle: String = "shuffleSongs"
        val clickOnNowPlayer:String = "nowplaying"
        var isPlaying: Boolean = false
        var repeat: Boolean = false
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentMusicPlayerBinding

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMusicPlayerBinding.inflate(layoutInflater)
        viewModel.bindToService()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: ")
        super.onViewCreated(view, savedInstanceState)

        if ((arguments?.getString("onSongClicked").equals(clickOnSongs)) ||
            (arguments?.getString("onShuffleClicked").equals(clickOnShuffle))) {
            viewModel.startMyService(requireContext())
            musicList = (arguments?.getSerializable("musicList") as? ArrayList<MusicData>)!!
            position = arguments?.getInt("pos")!!
            //Start the first song after binding the service
            sendDataToPlayerView()
        }

        //when we clicked on now playing fragment
        if (arguments?.getString("onNowPlayedClicked").equals(clickOnNowPlayer)){
            Log.d(TAG, "sendDataToPlayerView: onNowPlayedClicked")
            viewModel.setContentLayout(requireContext())
            if (isPlaying){
                binding.playPauseMusicBtn.setImageResource(R.drawable.pause_music_icon)
            }else{
                binding.playPauseMusicBtn.setImageResource(R.drawable.play_music_icon)
            }
            binding.seekBarStart.text = formatDuration(PlayerViewModel.musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekBarEnd.text = formatDuration(PlayerViewModel.musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = PlayerViewModel.musicService!!.mediaPlayer!!.currentPosition
            binding.seekBarPA.max = PlayerViewModel.musicService!!.mediaPlayer!!.duration
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


        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    PlayerViewModel.musicService!!.mediaPlayer!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                PlayerViewModel.musicService!!.mediaPlayer!!.seekTo(seekBar?.progress ?: 0)
            }
        })

        binding.repeatBtnPA.setOnClickListener {
            if (!repeat){
                repeat = true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(requireContext(),R.color.icon_color))
            }else{
                repeat = false
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(requireContext(),R.color.text_color))
            }
        }

        binding.equalizerBtnPA.setOnClickListener {
            try {
                val EqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                EqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION,PlayerViewModel.musicService!!.mediaPlayer!!.audioSessionId)
                EqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, requireContext().packageName)
                EqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE,AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(EqIntent,13)
            }catch (e:Exception){
                Toast.makeText(requireContext(),"Equalizer feature not supported ",Toast.LENGTH_SHORT).show()
            }
        }


        binding.backPlayerBtn.setOnClickListener { findNavController().popBackStack() }

    }



    private fun sendDataToPlayerView() {
        Log.d(TAG, "sendDataToPlayerView: ")
        viewModel.isServiceBound.observe(viewLifecycleOwner) {
            when (it) {
                is UiState.Success -> {
                    Log.d(TAG, "observer: binding service Success")
                    binding.playPauseMusicBtn.visibility = View.VISIBLE
                    binding.playProgressBar.visibility = View.GONE
                    binding.prevMusicBtn.isEnabled = true
                    binding.prevMusicBtn.imageAlpha = 255
                    binding.nextMusicBtn.isEnabled = true
                    binding.nextMusicBtn.imageAlpha = 255
                    binding.seekBarPA.isEnabled = true
                    if (arguments?.getString("onSongClicked").equals(clickOnSongs)) {
                        Log.d(TAG, "sendDataToPlayerView: on songClicked")
                        initializeLayout()
                    }
                    if (arguments?.getString("onShuffleClicked").equals(clickOnShuffle)) {
                        Log.d(TAG, "sendDataToPlayerView: on shuffle clicked")
                        musicList.shuffle()
                        initializeLayout()
                    }

                }
                is UiState.Loading -> {
                    Log.d(TAG, "observer: service loading...")
                    binding.playPauseMusicBtn.visibility = View.GONE
                    binding.playProgressBar.visibility = View.VISIBLE
                    binding.prevMusicBtn.isEnabled = false
                    binding.prevMusicBtn.imageAlpha = 75
                    binding.nextMusicBtn.isEnabled = false
                    binding.nextMusicBtn.imageAlpha = 75
                    binding.seekBarPA.isEnabled = false

                }
                is UiState.Failure -> {}
            }
        }
    }

    private fun observer() {
        viewModel.firstSong.observe(viewLifecycleOwner) {
            when (it) {
                is UiState.Loading -> {
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
        Log.d(TAG, "initializeLayout: ")

        observer()

        Handler(Looper.getMainLooper()).post {
            viewModel.createMediaPlayer(musicList.get(position).songUrl)
        }
        viewModel.setContentLayout(requireContext())
        if (repeat){
            binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(requireContext(),R.color.icon_color))
        }
        // initializing the seek bar


        isPlaying = true
        binding.playPauseMusicBtn.setImageResource(R.drawable.pause_music_icon)
        Log.d(TAG, "initializeLayout: finsh")
    }


    private fun playMusic() {
        binding.playPauseMusicBtn.setImageResource(R.drawable.pause_music_icon)
        isPlaying = true
        viewModel.playMusic()
    }

    private fun pauseMusic() {
        binding.playPauseMusicBtn.setImageResource(R.drawable.play_music_icon)
        isPlaying = false
        viewModel.pauseMusic()
    }

    private fun prevNextSong(increment: Boolean) {
        Log.d(TAG, "prevNextSong: setsongposition")
        if (increment) {
            setSongPosition(increment)
            initializeLayout()
        } else {
            setSongPosition(increment)
            initializeLayout()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")

    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 || resultCode == RESULT_OK){
            return
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "onDestroy: ")
    }


}