package com.devapp.runningapp.util

import android.graphics.Color

object Constant {

    const val REQUEST_CODE_FOR_GET_PENDING_INTENT = 2
    const val REQUEST_CODE_PERMISSION = 1

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_TO_TRACKING_INTENT = "ACTION_TO_TRACKING_INTENT"

    const val CHANNEL_ID="APP_RUN_ID"
    const val NOTIFICATION_NAME = "RUNNING_APP_TRACKING"
    const val NOTIFICATION_ID = 1

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_UPDATE_INTERVAL = 2000L

    const val POLYLINE_WIDTH = 10f
    const val DEFAULT_ZOOM_CAMERA = 18f

    const val PREFERENCE_NAME ="pref_name"
    const val KEY_NAME = "name"
    const val KEY_WEIGHT = "weight"
    const val KEY_IS_FIRST_TIME = "is_first_time"
    const val API_KEY_WEATHER = "ce691c235536e7056880ad1a68877784"

    val LIST_TIME_AWARD = listOf(2,4,8,16,20)
    val LIST_AVG_SPEED_AWARD = listOf(8,18,26,32,36)
    val LIST_DISTANCE_AWARD = listOf(40,100,220,500,1000)
    val LIST_CALORIES_AWARD = listOf(10,30,60,90,100)

    val URL_FIREBASE_DB = "https://runapp-372b2-default-rtdb.asia-southeast1.firebasedatabase.app"
}