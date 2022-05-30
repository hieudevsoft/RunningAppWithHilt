package com.devapp.runningapp.repositories

import com.devapp.runningapp.model.Run
import com.devapp.runningapp.db.RunDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MainRepository @Inject constructor(val runDao: RunDao){

    suspend fun insertRun(run: Run):Long {
        return runDao.insertRun(run)
    }
    suspend fun delete(run: Run){
        runDao.delete(run)
    }

    fun getAllRunsSortedByDate(): Flow<List<Run>>
    {
        return runDao.getAllRunsSortedByDate()
    }

    fun getAllRunsSortedByTime(): Flow<List<Run>>
    {
        return runDao.getAllRunsSortedByTime()
    }

    fun getAllRunsSortedByCaloriesBurned(): Flow<List<Run>>
    {
        return runDao.getAllRunsSortedByCaloriesBurned()
    }

    fun getAllRunsSortedByAvgSpeed(): Flow<List<Run>>
    {
        return runDao.getAllRunsSortedByAvgSpeed()
    }

    fun getAllRunsSortedDistance(): Flow<List<Run>>
    {
        return runDao.getAllRunsSortedDistance()
    }

    fun getTotalTimeInMillis(): Flow<Long>
    {
        return runDao.getTotalTimeInMillis()
    }

    fun getTotalDistance(): Flow<Int>
    {
        return runDao.getTotalDistance()
    }

    fun getTotalCaloriesBurned(): Flow<Int>
    {
        return runDao.getTotalCaloriesBurned()
    }

    fun getTotalAvgSpeed(): Flow<Float>
    {
        return runDao.getTotalAvgSpeed()
    }
}