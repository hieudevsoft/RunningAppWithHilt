package com.devapp.runningapp.db

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName= "running_table")
data class Run(
    var img: Bitmap?=null,
    var timeStamp:Long?=0L,
    var avgSpeedInKMH:Float?=0F,
    var distanceInMeters:Int?=0,
    var timeInRun:Long?=0L,
    var caloriesBurned:Int?=0
){
    @PrimaryKey(autoGenerate = true)
    var id:Int?=null
}


