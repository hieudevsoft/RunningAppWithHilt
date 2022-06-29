package com.devapp.runningapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


@HiltAndroidApp
class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        createNotificationChannel()
        Timber.plant(Timber.DebugTree())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("default", "Daily Notification", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Daily Notification"
            val nm = NotificationManagerCompat.from(applicationContext)
            nm.createNotificationChannel(channel)
        }
    }
}