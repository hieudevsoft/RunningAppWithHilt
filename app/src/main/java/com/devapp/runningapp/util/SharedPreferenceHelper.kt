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
}