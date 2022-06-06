package com.devapp.runningapp.model

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.devapp.runningapp.model.user.Gender

@Entity(tableName= "running_table")
data class Run(
    var img: Bitmap?=null,
    var timeStamp:Long?=null,
    var avgSpeedInKMH:Float?=null,
    var distanceInMeters:Int?=null,
    var timeInRun:Long?=null,
    var caloriesBurned: Int?=null,
    var uid: String?=null,
    @PrimaryKey(autoGenerate = true)
    var id:Int?=null
){
    fun toMap():Map<String,Any?>{
        return hashMapOf(
            "id" to id,
            "uid" to uid,
            "timeStamp" to timeStamp,
            "avgSpeedInKMH" to avgSpeedInKMH,
            "distanceInMeters" to distanceInMeters,
            "timeInRun" to timeInRun,
            "caloriesBurned" to caloriesBurned,
            "img" to img
        )
    }

    fun fromMap(map: Map<String,Any?>){
        if(map["id"]!=null) this.id = map["id"].toString().toInt()
        if(map["uid"]!=null) this.uid = map["uid"].toString()
        if(map["timeStamp"]!=null) this.timeStamp = map["email"].toString().toLong()
        if(map["avgSpeedInKMH"]!=null) this.avgSpeedInKMH = map["password"].toString().toFloat()
        if(map["distanceInMeters"]!=null) this.distanceInMeters = map["distanceInMeters"].toString().toInt()
        if(map["timeInRun"]!=null) this.timeInRun = map["timeInRun"].toString().toLong()
        if(map["caloriesBurned"]!=null) this.caloriesBurned = map["caloriesBurned"].toString().toInt()
        this.img = null
    }
}


