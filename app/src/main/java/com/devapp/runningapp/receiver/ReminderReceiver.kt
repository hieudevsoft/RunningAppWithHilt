package com.devapp.runningapp.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.devapp.runningapp.R
import com.devapp.runningapp.ui.MainActivity
import com.devapp.runningapp.util.SharedPreferenceHelper
import java.util.*

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val preferenceHelper = SharedPreferenceHelper(context)
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingI = PendingIntent.getActivity(context, 0, notificationIntent, 0)
        val b = NotificationCompat.Builder(context, "default")
        b.setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(preferenceHelper.titleNotify)
            .setContentText(preferenceHelper.contentNotify)
            .setStyle(NotificationCompat.BigTextStyle())
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setContentIntent(pendingI)
            .setLights(Color.BLUE, 3000, 3000)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        try {
            NotificationManagerCompat.from(context).notify(1, b.build())
        } catch (ignored: NullPointerException) {
        }
        val nextNotifyTime = Calendar.getInstance()
        nextNotifyTime.add(Calendar.DATE, 1)
        preferenceHelper.nextTimeNotify = nextNotifyTime.timeInMillis
    }
}