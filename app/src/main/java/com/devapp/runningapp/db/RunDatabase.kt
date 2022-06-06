package com.devapp.runningapp.db

import android.content.Context
import androidx.room.*
import com.devapp.runningapp.model.Run


@Database(entities = [Run::class],version = 2,exportSchema = false)
@TypeConverters(RunConverter::class)
abstract class RunDatabase:RoomDatabase(){
    abstract fun getRunDao():RunDao
    companion object{
        @Volatile
        private var INSTANCE:RunDatabase?=null
        operator fun invoke(context: Context):RunDatabase {
            if(INSTANCE!=null) return INSTANCE!!
            else synchronized(this){
                INSTANCE = createDatabase(context)
                return INSTANCE!!
            }
        }

        private fun createDatabase(context: Context):RunDatabase{
            return Room.databaseBuilder(context,RunDatabase::class.java,"db_Run")
                .fallbackToDestructiveMigration().build()
        }
    }
}