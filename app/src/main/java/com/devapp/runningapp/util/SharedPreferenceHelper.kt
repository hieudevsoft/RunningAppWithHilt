package com.devapp.runningapp.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.devapp.runningapp.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPreferenceHelper @Inject constructor(@ApplicationContext context: Context) {
    private val pref = context.getSharedPreferences(context.getString(R.string.app_name),MODE_PRIVATE)

    var isShowOnBoarding:Boolean
        get() = pref.getBoolean("IS_SHOW_ONBOARDING",true)
        set(value) = pref.edit().putBoolean("IS_SHOW_ONBOARDING",value).apply()

    var isNightMode:Boolean
        get() = pref.getBoolean("IS_NIGHT_MODE",true)
        set(value) = pref.edit().putBoolean("IS_NIGHT_MODE",value).apply()

    var statusSignIn:Int
        get() = pref.getInt("STATUS_SIGN_IN",1)
        set(value) = pref.edit().putInt("STATUS_SIGN_IN",value).apply()

    fun getNumberEnterPassword(uid:String) = pref.getInt("NUMBER_ENTER_PASSWORD_"+uid,5)
    fun setNumberEnterPassword(uid:String,value:Int) = pref.edit().putInt("NUMBER_ENTER_PASSWORD_"+uid,value).apply()

    var timeReminder: String
        get() = pref.getString("TIME_REMINDER", "") ?: ""
        set(value) = pref.edit().putString("TIME_REMINDER", value).apply()

    var isNotifyReminder: Boolean
        get() = pref.getBoolean("IS_NOTIFY_REMINDER", false)
        set(value) = pref.edit().putBoolean("IS_NOTIFY_REMINDER", value).apply()

    var titleNotify: String
        get() = pref.getString("TITLE_NOTIFY", "") ?: ""
        set(value) = pref.edit().putString("TITLE_NOTIFY", value).apply()

    var contentNotify: String
        get() = pref.getString("CONTENT_NOTIFY", "") ?: ""
        set(value) = pref.edit().putString("CONTENT_NOTIFY", value).apply()

    var weightUser: String
        get() = pref.getString("WEIGHT_USER", "") ?: ""
        set(value) = pref.edit().putString("WEIGHT_USER", value).apply()

    var nextTimeNotify: Long // nextTime notify Remind
        get() = pref.getLong("NEXT_TIME_NOTIFY", 0)
        set(value) = pref.edit().putLong("NEXT_TIME_NOTIFY", value).apply()

    var accessUid: String?
        get() = pref.getString("ACCESS_UID", "") ?: ""
        set(value) = pref.edit().putString("ACCESS_UID", value).apply()

    var userProfile:String?
        get() = pref.getString("USER_PROFILE","")
        set(value) = pref.edit().putString("USER_PROFILE", value).apply()

}