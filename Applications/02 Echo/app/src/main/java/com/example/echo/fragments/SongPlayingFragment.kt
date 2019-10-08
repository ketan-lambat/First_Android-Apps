package com.example.echo.fragments

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.core.content.ContextCompat
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.echo.CurrentSongHelper
import com.example.echo.Databases.EchoDatabase
import com.example.echo.R
import com.example.echo.Songs
import com.example.echo.fragments.FavouriteFragment.Statified.mediaPlayer
import com.example.echo.fragments.SongPlayingFragment.Staticated.onSongComplete
import com.example.echo.fragments.SongPlayingFragment.Staticated.playNext
import com.example.echo.fragments.SongPlayingFragment.Staticated.processInformation
//import com.example.echo.fragments.SongPlayingFragment.Staticated.updateTextView
import com.example.echo.fragments.SongPlayingFragment.Statified.MY_PREFS_LOOP
import com.example.echo.fragments.SongPlayingFragment.Statified.MY_PREFS_NAME
import com.example.echo.fragments.SongPlayingFragment.Statified.MY_PREFS_SHUFFLE
import com.example.echo.fragments.SongPlayingFragment.Statified.audioVisualization
import com.example.echo.fragments.SongPlayingFragment.Statified.currentPosition
import com.example.echo.fragments.SongPlayingFragment.Statified.currentSongHelper
import com.example.echo.fragments.SongPlayingFragment.Statified.endTimeText
import com.example.echo.fragments.SongPlayingFragment.Statified.fab
import com.example.echo.fragments.SongPlayingFragment.Statified.favouriteContent
import com.example.echo.fragments.SongPlayingFragment.Statified.fetchSongs
import com.example.echo.fragments.SongPlayingFragment.Statified.loopImageButton
import com.example.echo.fragments.SongPlayingFragment.Statified.mSensonManager
import com.example.echo.fragments.SongPlayingFragment.Statified.myActivity
import com.example.echo.fragments.SongPlayingFragment.Statified.nextImageButton
import com.example.echo.fragments.SongPlayingFragment.Statified.playPauseImageButton
import com.example.echo.fragments.SongPlayingFragment.Statified.previousImageButton
import com.example.echo.fragments.SongPlayingFragment.Statified.seekBar
import com.example.echo.fragments.SongPlayingFragment.Statified.shuffleImageButton
import com.example.echo.fragments.SongPlayingFragment.Statified.songTitle
import com.example.echo.fragments.SongPlayingFragment.Statified.startTimeText
import kotlinx.android.synthetic.main.fragment_song_playing.*
import java.io.IOException
import java.lang.Exception
import kotlin.random.Random
import java.util.*
import java.util.concurrent.TimeUnit

import com.example.echo.fragments.SongPlayingFragment.Statified.songArtist as SongArtist
import com.example.echo.fragments.SongPlayingFragment.Statified.songTitle as songPlayingFragmentStatifiedSongTitle
import kotlinx.android.synthetic.main.fragment_song_playing.seekBar as seekBar1
import kotlinx.android.synthetic.main.fragment_song_playing.songArtist as songArtist1
import kotlinx.android.synthetic.main.fragment_song_playing.songTitle as songTitle1


class SongPlayingFragment : Fragment() {

    object Statified {
        var MY_PREFS_NAME = "ShakeFeature"
        var MY_PREFS_SHUFFLE = "ShuffleSave"
        var MY_PREFS_LOOP = "LoopSave"
        var seekBar: SeekBar? = null
        var mediaPlayer: MediaPlayer? = null
        var fetchSongs: ArrayList<Songs>? = arrayListOf()
        var currentTrackHelper:String?=null
        var favouriteContent: EchoDatabase? = null
        var currentSongHelper = CurrentSongHelper()
        var currentPosition: Int = 0
        var fab: ImageButton? = null
        var mSensonManager: SensorManager? = null
        var audioVisualization: AudioVisualization? = null
        var myActivity: Activity? = null
        var songArtist: TextView? = null
        var songTitle: TextView? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playPauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var rewindImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var fastforwardImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var shuffleImageButton: ImageButton? = null

        var updateSongTime = object : Runnable {
            override fun run() {
                val getCurrent = mediaPlayer?.currentPosition
                startTimeText?.setText(String.format("%d:%d",
                    getCurrent?.toLong()?.let { TimeUnit.MILLISECONDS.toMinutes(it) },
                    getCurrent?.toLong()?.let { TimeUnit.MILLISECONDS.toMinutes(it) }?.let {
                        TimeUnit.MILLISECONDS.toSeconds(
                            it
                        )
                    }))
                seekBar?.setProgress(getCurrent?.toInt() as Int)
                Handler().postDelayed(this, 1000)
            }
        }

        /*
        var updateSongTime = object : Runnable {
            override fun run() {
                val getCurrent = mediaPlayer?.getCurrentPosition()
                startTimeText!!.text = String.format("%d: %d",
                    TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong()!!),
                    TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong()!!) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong())))

                seekBar?.setProgress(getCurrent.toInt())

                Handler().postDelayed(this, 1000)
            }
        }*/
    }

    var glView: GLAudioVisualizationView? = null
    private var mAcceleration: Float = 0f
    private var mAccelerationCurrent: Float = 0f
    private var mAcceletationLast: Float = 0f
    var mSensorListener: SensorEventListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        SongPlayingFragment.Statified.myActivity = context as Activity?
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        SongPlayingFragment.Statified.myActivity = activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        activity?.title = "Now playing"
        currentSongHelper.isLoop = false
        currentSongHelper.isShuffle = false
        currentSongHelper.isPlaying = true
        seekBar = view?.findViewById(R.id.seekBar) as SeekBar
        startTimeText = view.findViewById(R.id.startTime) as TextView
        endTimeText = view.findViewById(R.id.endTime) as TextView
        playPauseImageButton = view.findViewById(R.id.playPauseButton) as ImageButton
        nextImageButton = (view.findViewById(R.id.nextButton) as ImageButton)
        previousImageButton = (view.findViewById(R.id.previousButton) as ImageButton)
        loopImageButton = (view.findViewById(R.id.loopButton) as ImageButton)
        shuffleImageButton = (view.findViewById(R.id.shuffleButton) as ImageButton)
        fab = view.findViewById(R.id.favouriteIcon) as ImageButton
        glView = view.findViewById(R.id.visualizer_view) as GLAudioVisualizationView
        SongArtist = view.findViewById(R.id.songTitle) as TextView
        songTitle = view.findViewById(R.id.songArtist) as TextView
        fab?.setAlpha(0.8f)
        return view
    }

    override fun onResume() {
        super.onResume()
        audioVisualization?.onResume()
        if (mediaPlayer?.isPlaying as Boolean) {
            currentSongHelper.isPlaying = true
            playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            currentSongHelper.isPlaying = false
            playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }

        /*
        Statified.mSensonManager?.registerListener(
            Statified.mSensorListener,
            Statified.mSensonManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mSensonManager =
            activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensonManager?.registerListener(
            mSensorListener,
            mSensonManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAcceletationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onPause() {
        super.onPause()
        audioVisualization?.onPause()
        mSensonManager?.unregisterListener(mSensorListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioVisualization?.release()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_redirect -> {
                myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        audioVisualization = glView as AudioVisualization
        favouriteContent = EchoDatabase(myActivity)

        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long = 0
        try {
            path = arguments?.getString("path")
            currentPosition = arguments?.getInt("songPosition") as Int
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            fetchSongs = arguments?.getParcelableArrayList("songData")
            songId = arguments?.getInt("songId")?.toLong() as Long

            if (_songArtist.equals("<unknown>", true)) {
                _songArtist = "unknown"
            }
            currentSongHelper.songArtist = _songArtist
            currentSongHelper.songTitle = _songTitle
            currentSongHelper.songPath = path
            currentSongHelper.currentPosition = currentPosition
            currentSongHelper.songId = songId

        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (favouriteContent?.checkifIdExists(currentSongHelper.songId.toInt()) as Boolean) {

            fab?.setImageDrawable(
                ContextCompat.getDrawable(
                    myActivity as Context,
                    R.drawable.favorite_on
                )
            )
        } else {
            fab?.setImageDrawable(
                ContextCompat.getDrawable(
                    myActivity as Context,
                    R.drawable.favorite_off
                )
            )
        }


        SongArtist?.text = currentSongHelper.songArtist
        songTitle?.text = currentSongHelper.songTitle
        SongPlayingFragment.Statified.currentTrackHelper = currentSongHelper.songTitle

        val fromBottomBar = arguments?.get("BottomBar") as? String
        val fromFavBottomBar = arguments?.get("FavBottomBar") as? String

        if (fromBottomBar != null) {
            mediaPlayer = MainScreenFragment.Statified.mMediaPlayer
        } else if (fromFavBottomBar != null) {
            mediaPlayer = FavouriteFragment.Statified.mediaPlayer
        } else {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                mediaPlayer?.setDataSource(activity as Context, Uri.parse(path))
                mediaPlayer?.prepare()
            } catch (e: Exception) {
                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
            mediaPlayer?.start()
        }


        audioVisualization?.linkTo(
            DbmHandler.Factory.newVisualizerHandler(
                activity as Context,
                mediaPlayer?.audioSessionId as Int
            )
        )

        if (mediaPlayer?.isPlaying as Boolean) {
            playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        SongPlayingFragment.Staticated.processInformation(mediaPlayer as MediaPlayer)
        clickHandler()

        mediaPlayer?.setOnCompletionListener {
            SongPlayingFragment.Staticated.onSongComplete()
        }

        var prefs = activity?.getSharedPreferences(MY_PREFS_SHUFFLE, MODE_PRIVATE)
        var isShuffleAllowed = prefs?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            currentSongHelper.isShuffle = true
            currentSongHelper.isLoop = false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            currentSongHelper.isShuffle = false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsforLoop = activity?.getSharedPreferences(MY_PREFS_LOOP, MODE_PRIVATE)
        var isLoopAllowed = prefsforLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            currentSongHelper.isShuffle = false
            currentSongHelper.isLoop = true
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        } else {
            currentSongHelper.isLoop = false
            loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }

    }
    object Staticated {

        fun onSongComplete() {
            if (currentSongHelper.isShuffle) {
                currentSongHelper.isPlaying = true
                playNext("PlayNextLikeNormalShuffle")
            } else {
                if (currentSongHelper.isLoop) {
                    currentSongHelper.isPlaying = true
                    var nextSong = fetchSongs?.get(currentPosition)
                    SongPlayingFragment.Statified.currentTrackHelper = nextSong?.songTitle

                    if (nextSong?.artist.equals("<unknown>", true)) {
                        currentSongHelper.songArtist = "unknown"
                    } else {
                        currentSongHelper.songArtist = nextSong?.artist
                    }

                    currentSongHelper.songTitle = nextSong?.songTitle
                    currentSongHelper.songPath = nextSong?.songData
                    currentSongHelper.currentPosition = currentPosition
                    currentSongHelper.songId = nextSong?.songID as Long
                    // currentSongHelper.songArtist = nextSong?.artist
                    /*
                    updateTextView(
                        currentSongHelper?.songTitle as String,
                        currentSongHelper?.songArtist as String
                    )
                    */
                    if (favouriteContent?.checkifIdExists(currentSongHelper.songId.toInt()) as Boolean) {
                        fab?.setImageDrawable(
                            ContextCompat.getDrawable(
                                myActivity as Context,
                                R.drawable.favorite_on
                            )
                        )
                    }
                    mediaPlayer?.reset()
                    try {
                        mediaPlayer?.setDataSource(
                            myActivity as Context,
                            Uri.parse(nextSong.songData)
                        )
                        mediaPlayer?.prepare()
                        mediaPlayer?.start()
                        SongArtist?.text = nextSong.artist
                        songTitle?.text = nextSong.songTitle
                        processInformation(mediaPlayer as MediaPlayer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    currentSongHelper.isPlaying = true
                    playNext("PlayNextNormal")
                }
            }
        }

        fun playNext(check: String) {
            if (!(currentSongHelper.isPlaying as Boolean)) {
                playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }

            if (check.equals("PlayNextNormal", true)) {
                currentPosition += 1
                if (currentPosition == fetchSongs?.size) {
                    currentPosition = 0
                }
            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = Random
                var randomPosition = randomObject.nextInt(fetchSongs?.size?.plus(1) as Int)
                currentPosition = randomPosition
                if (currentPosition == fetchSongs?.size) {
                    currentPosition = 0
                }
            }

            var nextSong = fetchSongs?.get(currentPosition)
            SongPlayingFragment.Statified.currentTrackHelper = nextSong?.songTitle
            if (nextSong?.artist.equals("<unknown>", true)) {
                currentSongHelper.songArtist = "unknown"
            } else {
                currentSongHelper.songArtist = nextSong?.artist
            }

            currentSongHelper.songTitle = nextSong?.songTitle
            currentSongHelper.songPath = nextSong?.songData
            currentSongHelper.currentPosition = currentPosition
            currentSongHelper.songId = nextSong?.songID as Long

            try {
                if (favouriteContent?.checkifIdExists(currentSongHelper.songId.toInt()) as Boolean) {
                    fab?.setImageDrawable(
                        ContextCompat.getDrawable(
                            myActivity as Activity,
                            R.drawable.favorite_on
                        )
                    )
                } else {
                    fab?.setImageDrawable(
                        ContextCompat.getDrawable(
                            myActivity as Activity,
                            R.drawable.favorite_off
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mediaPlayer?.reset()
            try {
                mediaPlayer?.setDataSource(
                    myActivity as Context,
                    Uri.parse(currentSongHelper.songPath)
                )
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                if (nextSong.artist.equals("<unknown>", true)) {
                    SongArtist?.text = "unknown"
                } else {
                    SongArtist?.text = nextSong.artist
                }
                songTitle?.text = nextSong.songTitle
                processInformation(mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            seekBar?.max = finalTime

            startTimeText?.setText(
                String.format(
                    "%d: %d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())
                    )
                )
            )
            endTimeText?.setText(
                String.format(
                    "%d: %d",
                    TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())
                    )
                )
            )

            seekBar?.setProgress(startTime)
            Handler().postDelayed(Statified.updateSongTime, 1000)
        }
    }
    /*
        fun updateTextView(songTitle: String, songArtist: String) {
            var songTitleUpdated = songTitle
            var songArtistUpdated = songArtist
            if (songTitle.equals("<unknown>", true)) {
                songTitleUpdated = "unknown"
            }
            if (songArtist.equals("<unknown>", true)) {
                songArtistUpdated = "unknown"
            }
            songTitle?.setText(songTitleUpdated)
            songArtist?.setText(songArtistUpdated)
        }
    */

    fun bindShakeListener() {
        mSensorListener = object : SensorEventListener {

            override fun onSensorChanged(p0: SensorEvent) {
                val x = p0.values[0]
                val y = p0.values[1]
                val z = p0.values[2]

                mAcceletationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt(((x * x + y * y + z * z).toDouble())).toFloat()
                val delta = mAccelerationCurrent - mAcceletationLast
                mAcceleration = mAcceleration * 0.9f + delta // perform low-cut filter

                if (mAcceleration > 12) {
                    val prefs = myActivity?.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean) {
                        SongPlayingFragment.Staticated.playNext("PlayNextNormal")
                    }
                }
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
        }
    }

    private fun clickHandler() {
        fab?.setOnClickListener {
            if (favouriteContent?.checkifIdExists(currentSongHelper.songId.toInt()) as Boolean) {
                favouriteContent?.deleteFavourite(currentSongHelper.songId.toInt())
                Toast.makeText(myActivity, "Removed from Favourites", Toast.LENGTH_SHORT).show()
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_off))
            } else {
                Toast.makeText(myActivity, "Added to Favourites", Toast.LENGTH_SHORT).show()
                favouriteContent?.storeasFavourite(
                    currentSongHelper.songId.toInt(),
                    currentSongHelper.songArtist,
                    currentSongHelper.songTitle,
                    currentSongHelper.songPath
                )
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_on))
            }
        }
        shuffleImageButton?.setOnClickListener {
            val editorShuffle =
                myActivity?.getSharedPreferences(MY_PREFS_SHUFFLE, MODE_PRIVATE)
                    ?.edit()
            val editorLoop =
                myActivity?.getSharedPreferences(MY_PREFS_LOOP, MODE_PRIVATE)
                    ?.edit()
            if (currentSongHelper.isShuffle as Boolean) {
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                currentSongHelper.isShuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                currentSongHelper.isShuffle = true
                currentSongHelper.isLoop = false
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }
        }

        nextImageButton?.setOnClickListener {
            currentSongHelper.isPlaying = true
           // playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if (currentSongHelper.isShuffle) {
                playNext("PlayNextLikeNormalShuffle")
            } else {
                playNext("PlayNextNormal")
            }
        }

        previousImageButton?.setOnClickListener {
            currentSongHelper.isPlaying = true
            if (currentSongHelper.isLoop) {
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            currentSongHelper.isLoop=false
            playPrevious()
        }

        loopImageButton?.setOnClickListener {
            val editorShuffle =
                myActivity?.getSharedPreferences(MY_PREFS_SHUFFLE, MODE_PRIVATE)
                    ?.edit()
            val editorLoop =
                myActivity?.getSharedPreferences(MY_PREFS_LOOP, MODE_PRIVATE)
                    ?.edit()
            if (currentSongHelper.isLoop) {
                currentSongHelper.isLoop = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                currentSongHelper.isLoop = true
                currentSongHelper.isShuffle = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }
        }

        playPauseImageButton?.setOnClickListener {
            if (mediaPlayer?.isPlaying as Boolean) {
                currentSongHelper.isPlaying = true
                playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                mediaPlayer?.pause()
            } else {
                currentSongHelper.isPlaying = false
                playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                mediaPlayer?.seekTo(seekBar?.progress as Int)
                mediaPlayer?.start()
            }
        }

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBarget: SeekBar?) {
                seekBar?.setProgress(seekBar?.getProgress() as Int)
                mediaPlayer?.seekTo(seekBar?.getProgress() as Int)
            }
        })

    }

/*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioVisualization = glView as AudioVisualization
    }

*/
    fun playPrevious() {
        currentPosition = currentPosition - 1

        if (currentPosition == -1) {
            currentPosition = 0
        }
        if (currentSongHelper.isPlaying) {
            playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }

        var nextSong = fetchSongs?.get(currentPosition)
        currentSongHelper.songTitle = nextSong?.songTitle
        currentSongHelper.songPath = nextSong?.songData
        currentSongHelper.songId = nextSong?.songID as Long
        currentSongHelper.currentPosition = currentPosition
        currentSongHelper.isLoop = false

      /*  updateTextView(
            currentSongHelper?.songTitle as String,
            currentSongHelper?.songArtist as String
        )
*/

    SongPlayingFragment.Statified.currentTrackHelper = currentSongHelper.songTitle

    if (nextSong.artist.equals("<unknown>", true)) {
        currentSongHelper.songArtist = "unknown"
    } else {
        currentSongHelper.songArtist = nextSong.artist
    }
    try {
        if (favouriteContent?.checkifIdExists(currentSongHelper.songId.toInt()) as Boolean) {
            fab?.setImageDrawable(ContextCompat.getDrawable(activity as Context, R.drawable.favorite_on))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
        Statified.mediaPlayer?.reset()
    try {
        mediaPlayer?.setDataSource(myActivity as Context, Uri.parse(nextSong.songData))
        mediaPlayer?.prepare()
        mediaPlayer?.start()

        SongArtist?.setText(nextSong.artist)
        songTitle?.setText(nextSong.songTitle)
        SongPlayingFragment.Staticated.processInformation(mediaPlayer as MediaPlayer)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    }
}
