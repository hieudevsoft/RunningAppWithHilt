package com.devapp.runningapp.repositories

import com.devapp.runningapp.model.Run
import com.devapp.runningapp.db.RunDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MainRepository @Inject constructor(val runDao: RunDao){

    suspend fun insertRun(run: Run):Long {
        return runDao.insertRun(run)
    }

    suspend fun insertRuns(runs: List<Run>){
        runDao.insertRuns(runs)
    }

    suspend fun deleteRuns(uid: String){
        runDao.deleteAllRunByUid(uid)
    }

    suspend fun delete(run: Run){
        runDao.delete(run)
    }

    fun getAllRunsSortedByDate(uid:String): Flow<List<Run>>
    {
        return runDao.getAllRunsSortedByDate(uid)
    }

    fun getAllRunsSortedByTime(uid:String): Flow<List<Run>>
    {
        return runDao.getAllRunsSortedByTime(uid)
    }

    fun getAllRunsSortedByCaloriesBurned(uid:String): Flow<List<Run>>
    {
        return runDao.getAllRunsSortedByCaloriesBurned(uid)
    }

    fun getAllRunsSortedByAvgSpeed(uid:String): Flow<List<Run>>
    {
        return runDao.getAllRunsSortedByAvgSpeed(uid)
    }

    fun getAllRunsSortedDistance(uid:String): Flow<List<Run>>
    {
        return runDao.getAllRunsSortedDistance(uid)
    }

    fun getTotalTimeInMillis(uid:String): Flow<Long>
    {
        return runDao.getTotalTimeInMillis(uid)
    }

    fun getTotalDistance(uid:String): Flow<Int>
    {
        return runDao.getTotalDistance(uid)
    }

    fun getTotalCaloriesBurned(uid:String): Flow<Int>
    {
        return runDao.getTotalCaloriesBurned(uid)
    }

    fun getTotalAvgSpeed(uid:String): Flow<Float>
    {
        return runDao.getTotalAvgSpeed(uid)
    }
}