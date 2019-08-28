package com.example.echo.activities

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.echo.R
import com.example.echo.fragments.MainScreenFragment

class MainActivity : AppCompatActivity() {

    var drawerLayout: DrawerLayout?=null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this@MainActivity,
            drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout?.setDrawerListener(toggle)
        toggle.syncState()
        val mainScreenFragment = MainScreenFragment()
        this.supportFragmentManager
            .beginTransaction()
            .add(R.id.details_fragment, mainScreenFragment, "MainScceenFragment")
            .commit()
    }

    override fun onStart() {
        super.onStart()
    }
}
