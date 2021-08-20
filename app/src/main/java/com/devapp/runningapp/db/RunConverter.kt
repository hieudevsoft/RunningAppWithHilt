package com.devapp.runningapp.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.BitmapCompat
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class RunConverter {

    @TypeConverter
    fun toBitmap(byteArray: ByteArray) = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)


    @TypeConverter
    fun fromBitmap(bitmap: Bitmap):ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream)
        return outputStream.toByteArray()
    }


}