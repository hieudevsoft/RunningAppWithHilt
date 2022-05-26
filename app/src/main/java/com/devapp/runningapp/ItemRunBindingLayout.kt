package com.devapp.runningapp

import android.graphics.Bitmap
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.devapp.runningapp.util.TrackingUtils
import com.makeramen.roundedimageview.RoundedImageView
import java.text.SimpleDateFormat
import java.util.*

object ItemRunBindingLayout {

    @BindingAdapter("loadingImageFromBitmap")
    @JvmStatic
    fun loadingImageFromBitmap(imageView:RoundedImageView,bitmap: Bitmap){
        imageView.setImageBitmap(bitmap)
    }

    @BindingAdapter("formatDate")
    @JvmStatic
    fun formatDate(textView:TextView,timeInMilliseconds:Long?){
        val date = Date(timeInMilliseconds!!)
        val text =  SimpleDateFormat("dd-MM-yy",Locale.getDefault()).format(date)
        textView.text = text
    }

    @BindingAdapter("formatAvgSpeed")
    @JvmStatic
    fun formatAvgSpeed(textView: TextView,speed:Float){
        textView.text="${speed}km/h"
    }

    @BindingAdapter("formatDistance")
    @JvmStatic
    fun formatDistance(textView: TextView,distance:Float){
        textView.text="${distance/1000f}km"
    }

    @BindingAdapter("formatTime")
    @JvmStatic
    fun formatTime(textView: TextView,time:Long){
        textView.text="${TrackingUtils.getFormattedStopWatchTime(time,false)}"
    }

    @BindingAdapter("formatCalories")
    @JvmStatic
    fun formatCalories(textView: TextView,calories:Int){
        textView.text="${calories}kcal"
    }
}