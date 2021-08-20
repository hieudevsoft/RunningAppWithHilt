package com.devapp.runningapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.devapp.runningapp.R
import com.devapp.runningapp.db.RunDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var runDao: RunDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}