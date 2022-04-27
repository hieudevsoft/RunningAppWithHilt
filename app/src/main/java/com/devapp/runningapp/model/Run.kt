package com.devapp.runningapp.model

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName= "running_table")
data class Run(
    var img: Bitmap?,
    var timeStamp:Long?,
    var avgSpeedInKMH:Float?,
    var distanceInMeters:Int?,
    var timeInRun:Long?,
    var caloriesBurned:Int?,
    @PrimaryKey(autoGenerate = true)
    var id:Int?
)


