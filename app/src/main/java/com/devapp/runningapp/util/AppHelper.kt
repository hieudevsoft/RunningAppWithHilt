package com.devapp.runningapp.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.DisplayMetrics
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream


object AppHelper {
    fun Context.convertDpToPixel(dp: Float): Float {
        return dp * (this.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun Any.toJson() = GsonBuilder().excludeFieldsWithoutExposeAnnotation().setExclusionStrategies(object:ExclusionStrategy{
        override fun shouldSkipField(f: FieldAttributes?): Boolean {
            if(f?.name=="netAmountPcy")
                return true
            return false
        }

        override fun shouldSkipClass(clazz: Class<*>?): Boolean {
            return false
        }

    }).create().toJson(this)

    fun <T> String.fromJson(clazz: Class<*>): T {
        return Gson().fromJson(this, clazz) as T
    }

    fun <T> String.fromJson(): List<T> {
        return Gson().fromJson(this, object: TypeToken<List<T>>(){}.type) as List<T>
    }

    fun goStore(activity: Activity) {
        var appPackageName = ""
        try {
            appPackageName = activity.packageName // getPackageName() from Context or Activity object
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            return
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        goUrl(activity, "https://play.google.com/store/apps/details?id=$appPackageName")
    }

    fun goUrl(activity: Activity, url: String) {
        var webpage = Uri.parse(url)
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            webpage = Uri.parse("http://$url")
        }
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(activity.packageManager) != null) activity.startActivity(intent)
    }

    fun encodeToBase64(image: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 90, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun decodeBase64(input: String?): Bitmap? {
        val decodedByte = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
    }

}