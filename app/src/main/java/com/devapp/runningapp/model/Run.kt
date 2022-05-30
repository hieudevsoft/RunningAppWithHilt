package com.devapp.runningapp.model

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.devapp.runningapp.model.user.Gender

@Entity(tableName= "running_table")
data class Run(
    var img: Bitmap?,
    var timeStamp:Long?,
    var avgSpeedInKMH:Float?,
    var distanceInMeters:Int?,
    var timeInRun:Long?,
    var caloriesBurned:Int?,
    @PrimaryKey
    var id:String
){
    fun toMap():Map<String,Any?>{
        return hashMapOf(
            "id" to id,
            "timeStamp" to timeStamp,
            "avgSpeedInKMH" to avgSpeedInKMH,
            "distanceInMeters" to distanceInMeters,
            "timeInRun" to timeInRun,
            "caloriesBurned" to caloriesBurned,
            "img" to img
        )
    }

    fun fromMap(map: Map<String,Any?>){
        if(map["id"]!=null) this.id = map["id"].toString()
        if(map["timeStamp"]!=null) this.timeStamp = map["email"].toString().toLong()
        if(map["avgSpeedInKMH"]!=null) this.avgSpeedInKMH = map["password"].toString().toFloat()
        if(map["distanceInMeters"]!=null) this.distanceInMeters = map["distanceInMeters"].toString().toInt()
        if(map["timeInRun"]!=null) this.timeInRun = map["timeInRun"].toString().toLong()
        if(map["caloriesBurned"]!=null) this.caloriesBurned = map["caloriesBurned"].toString().toInt()
        this.img = null
    }
}


