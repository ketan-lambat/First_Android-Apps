package com.example.echo.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Nullable
import com.example.echo.R


class AboutUsFragment : Fragment() {

    var aboutUs:TextView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater, @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_about_us, container, false)
        activity?.title = "About Us"
        aboutUs = view.findViewById(R.id.about_us_text)as TextView
        (aboutUs as TextView).text = "This app is made by Ketan Lambat as part of Internshala's Online Android Training project."

        return view
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val item = menu?.findItem(R.id.action_sort)
        if (item == null) {
        } else {
            item.isVisible = false
        }
    }


}
