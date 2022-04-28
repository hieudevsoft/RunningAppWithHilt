package com.devapp.runningapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.devapp.runningapp.R
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object NetworkHelper {
    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager?.activeNetwork
            val networkCap = connectivityManager?.getNetworkCapabilities(network)
            networkCap != null && networkCap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && networkCap.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )
        } else {
            val networkInfo = connectivityManager?.activeNetworkInfo
            networkInfo != null && networkInfo.isConnectedOrConnecting
        }
    }

    fun getImageBitmap(context: Context, imageUrl: String): Bitmap {
        return try {
            val url = URL(imageUrl)
            val httpsURLConnection = url.openConnection() as HttpsURLConnection
            httpsURLConnection.doInput = true
            BitmapFactory.decodeStream(httpsURLConnection.inputStream)
        } catch (e: Exception) {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        }
    }
}