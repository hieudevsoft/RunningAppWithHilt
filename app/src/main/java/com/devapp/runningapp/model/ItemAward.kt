package com.devapp.runningapp.model

import androidx.annotation.DrawableRes
import com.google.gson.annotations.SerializedName

data class ItemAward(
    @DrawableRes val img:Int,
    val title:String,
    val description:String,
    var isLock:Boolean = true,
    var progress:Int
)