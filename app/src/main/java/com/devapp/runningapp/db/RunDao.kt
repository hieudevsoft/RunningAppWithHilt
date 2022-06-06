package com.devapp.runningapp.db

import androidx.room.*
import com.devapp.runningapp.model.Run
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run):Long

    @Delete
    suspend fun delete(run: Run)

    @Query("DELETE FROM running_table where uid=:uid")
    suspend fun deleteAllRunByUid(uid: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuns(run: List<Run>)

    @Query("SELECT * FROM running_table where uid=:uid  ORDER BY timeStamp ")
    fun getAllRunsSortedByDate(uid:String): Flow<List<Run>>

    @Query("SELECT * FROM running_table where uid=:uid ORDER BY timeInRun ")
    fun getAllRunsSortedByTime(uid:String): Flow<List<Run>>

    @Query("SELECT * FROM running_table where uid=:uid ORDER BY caloriesBurned ")
    fun getAllRunsSortedByCaloriesBurned(uid:String): Flow<List<Run>>

    @Query("SELECT * FROM running_table where uid=:uid ORDER BY avgSpeedInKMH ")
    fun getAllRunsSortedByAvgSpeed(uid:String): Flow<List<Run>>

    @Query("SELECT * FROM running_table where uid=:uid ORDER BY distanceInMeters ")
    fun getAllRunsSortedDistance(uid:String): Flow<List<Run>>

    @Query("SELECT SUM(timeInRun)  FROM running_table where uid=:uid")
    fun getTotalTimeInMillis(uid:String): Flow<Long>

    @Query("SELECT SUM(distanceInMeters) FROM running_table where uid=:uid")
    fun getTotalDistance(uid:String): Flow<Int>

    @Query("SELECT SUM(caloriesBurned) FROM running_table where uid=:uid")
    fun getTotalCaloriesBurned(uid:String): Flow<Int>

    @Query("SELECT AVG(avgSpeedInKMH) FROM running_table where uid=:uid")
    fun getTotalAvgSpeed(uid:String): Flow<Float>

}