package com.devapp.runningapp.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.devapp.runningapp.services.Polylines
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtils {
    fun hasLocationPermission(context: Context):Boolean{
        return if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }else{
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    fun getDistanceForTracking(polylines:Polylines):Float{
        var distance = 0f
        polylines.forEach {
            var distanceSinglePolyline = 0f
            for(i in 0..it.size-2){
                val result = FloatArray(1)
                 Location.distanceBetween(
                    it[i].latitude,
                    it[i].longitude,
                    it[i+1].latitude,
                    it[i+1].longitude,
                    result
                )
                distanceSinglePolyline+=result[0]
            }
            distance+=distanceSinglePolyline
        }
        return distance
    }

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        if(!includeMillis) {
            return "${if(hours < 10) "0" else ""}$hours:" +
                    "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10
        return "${if(hours < 10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds < 10) "0" else ""}$seconds:" +
                "${if(milliseconds < 10) "0" else ""}$milliseconds"
    }

    fun View.toGone() {
        this.visibility = View.GONE
    }

    fun View.toVisible(){
        this.visibility = View.VISIBLE
    }

    fun View.invisible(){
        this.visibility = View.INVISIBLE
    }

    fun Activity.hideSoftKeyboard() {
        val inputMethodManager = this.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        if (inputMethodManager.isAcceptingText) {
            inputMethodManager.hideSoftInputFromWindow(
                this.currentFocus!!.windowToken,
                0
            )
        }
    }
}