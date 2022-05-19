package com.devapp.runningapp.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.devapp.runningapp.util.SharedPreferenceHelper
import java.util.*

class DeviceBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val preferenceHelper = SharedPreferenceHelper(context)
            if (intent!!.action == "android.intent.action.BOOT_COMPLETED") {
                // on device boot complete, reset the alarm
                val alarmIntent = Intent(context, ReminderReceiver::class.java)
                val pendingFlags = if (Build.VERSION.SDK_INT >= 23) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val pendingIntent = PendingIntent.getBroadcast(context, 100, alarmIntent, pendingFlags)
                val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val timeString = preferenceHelper.timeReminder
                var hour = 0
                var minute = 0
                if (timeString.contains(":")) {
                    val list: Array<String> = timeString.split(":".toRegex()).toTypedArray()
                    if (list.size == 2) {
                        hour = try {
                            list[0].toInt()
                        } catch (ex: NumberFormatException) {
                            0
                        }
                        minute = try {
                            list[1].toInt()
                        } catch (ex: NumberFormatException) {
                            0
                        }
                    }
                }
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 1)
                val newC: Calendar = GregorianCalendar()
                newC.timeInMillis = preferenceHelper.nextTimeNotify
                if (calendar.after(newC)) {
                    calendar.add(Calendar.HOUR, 1)
                }
                manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
            }
        }

    }
}