package com.devapp.runningapp.util

import android.content.Context
import android.util.DisplayMetrics

object AppHelper {
    fun Context.convertDpToPixel(dp: Float): Float {
        return dp * (this.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}