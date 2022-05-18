package com.devapp.runningapp.util

import android.content.Context
import android.util.DisplayMetrics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AppHelper {
    fun Context.convertDpToPixel(dp: Float): Float {
        return dp * (this.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun Any.toJson() = Gson().toJson(this)

    fun <T> String.fromJson(clazz: Class<*>): T {
        return Gson().fromJson(this, clazz) as T
    }

    fun <T> String.fromJson(): List<T> {
        return Gson().fromJson(this, object: TypeToken<List<T>>(){}.type) as List<T>
    }
}