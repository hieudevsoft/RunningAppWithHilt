package com.devapp.runningapp.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.devapp.runningapp.R
import com.devapp.runningapp.ui.MainActivity
import com.devapp.runningapp.util.Constant
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {
    @Provides
    @ServiceScoped
    fun provideFusedLocationProviderClient(@ApplicationContext app: Context) = FusedLocationProviderClient(app)

    @Provides
    @ServiceScoped
    fun provideGetMainActivityPendingIntent(@ApplicationContext app: Context) = PendingIntent.getActivity(
        app,
        Constant.REQUEST_CODE_FOR_GET_PENDING_INTENT,
        Intent(app, MainActivity::class.java).also {
            it.action = Constant.ACTION_TO_TRACKING_INTENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    @Provides
    @ServiceScoped
    fun provideBaseNotificationBuilder(@ApplicationContext app:Context,pendingIntent: PendingIntent)
    =  NotificationCompat.Builder(app, Constant.CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_run)
        .setContentTitle("Tracking Time")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)
}