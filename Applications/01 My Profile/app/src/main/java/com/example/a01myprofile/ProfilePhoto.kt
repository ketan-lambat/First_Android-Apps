package com.example.a01myprofile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ProfilePhoto : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_photo)
        title = "My Profile Photo"
    }
}
