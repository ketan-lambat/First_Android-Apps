package com.example.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.echo.Databases.EchoDatabase
import com.example.echo.R
import com.example.echo.Songs
import com.example.echo.adapters.FavouriteAdapter
import kotlinx.android.synthetic.main.fragment_favourite.*
import java.lang.Exception


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class FavouriteFragment : Fragment() {
    var myActivity: Activity?=null

   // var getSongsList: ArrayList<Songs>?=null

    var noFavourites:TextView?=null
    var nowPLayingBottomBAr:RelativeLayout?=null
    var playPauseButton:ImageButton?=null
    var songTitle:TextView?=null
    var recyclerView:RecyclerView?=null
    var trackPosition: Int = 0
    var favouriteContent: EchoDatabase?=null

    var refreshList: ArrayList<Songs>? =null
    var getListFromDatabase: ArrayList<Songs>?=null

    object Statified{
        var mediaPlayer: MediaPlayer?=null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_favourite, container, false)
        noFavourites = view?.findViewById(R.id.noFavourites)
        nowPLayingBottomBAr = view.findViewById(R.id.hiddenBarFavScreen)
        songTitle = view.findViewById(R.id.songTitleFavScreen)
        playPauseButton = view.findViewById(R.id.playPauseButton)
        recyclerView = view.findViewById(R.id.favouriteRecycler)

        return view

        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_favourite, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        favouriteContent = EchoDatabase(myActivity)
        display_favourites_by_searching()
        bottomBarSetup()
       // getSongsList = getSongsFromPhone()
        /*if (getSongsList == null){
            recyclerView?.visibility = View.INVISIBLE
            noFavourites?.visibility = View.VISIBLE
        }else{
            var favouriteAdapter = FavouriteAdapter(getSongsList as ArrayList<Songs>, myActivity as Context)
            val mLayoutManager = LinearLayoutManager(activity)
            recyclerView?.layoutManager = mLayoutManager
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = favouriteAdapter
            recyclerView?.setHasFixedSize(true)
        }*/
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
    }

    fun getSongsFromPhone():ArrayList<Songs>{
        var arrayList = ArrayList<Songs>()

        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null)

        if (songCursor!=null && songCursor.moveToFirst()){
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            while (songCursor.moveToNext()){
                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)

                arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))
            }
        }
        return arrayList
    }

    fun bottomBarSetup(){
        try {
            bottomBarClickHandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener {
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                SongPlayingFragment.Staticated.onSongComplete()
            }
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                nowPLayingBottomBAr?.visibility = View.VISIBLE
            }else{
                nowPLayingBottomBAr?.visibility = View.INVISIBLE
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler(){
        nowPLayingBottomBAr?.setOnClickListener{
            Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment = SongPlayingFragment()
            val args = Bundle()
            args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putInt("songId", SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar", "success")

            songPlayingFragment.arguments = args

            fragmentManager?.beginTransaction()?.replace(R.id.details_fragment, songPlayingFragment)?.addToBackStack("SongPlayingFragment")
                ?.commit()
        }
        playPauseButton?.setOnClickListener {
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                trackPosition = SongPlayingFragment.Statified.mediaPlayer?.getCurrentPosition() as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }
    fun display_favourites_by_searching(){
        if (favouriteContent?.checkSize() as Int > 0){
            refreshList = ArrayList<Songs>()
            getListFromDatabase = favouriteContent?.queryDBList()
            var fetchListfromDevice = getSongsFromPhone()
            if (fetchListfromDevice != null){
                for (i in 0..fetchListfromDevice?.size-1){
                    for (j in 0..getListFromDatabase?.size as Int-1){
                        if ((getListFromDatabase?.get(j)?.songID === (fetchListfromDevice?.get(i)?.songID))){
                            refreshList?.add((getListFromDatabase as ArrayList<Songs>)[j])
                        }
                    }
                }
            }else{

            }

            if(refreshList==null){
                recyclerView?.visibility = View.INVISIBLE
                noFavourites?.visibility = View.VISIBLE
            }else{
                var favouriteAdapter = FavouriteAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                var mLayoutManager = LinearLayoutManager(activity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favouriteAdapter
                recyclerView?.setHasFixedSize(true)
            }
        }else{
            recyclerView?.visibility = View.INVISIBLE
            noFavourites?.visibility = View.VISIBLE
        }

    }
}
