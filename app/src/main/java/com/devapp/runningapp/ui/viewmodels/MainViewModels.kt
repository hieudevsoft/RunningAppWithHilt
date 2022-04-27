package com.devapp.runningapp.ui.viewmodels

import androidx.lifecycle.*
import com.devapp.runningapp.model.Run
import com.devapp.runningapp.repositories.MainRepository
import com.devapp.runningapp.util.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModels @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    private val getAllRunSortByDate: LiveData<List<Run>> = mainRepository.getAllRunsSortedByDate().asLiveData()
    private val getAllRunsSortedByTime: LiveData<List<Run>> = mainRepository.getAllRunsSortedByTime().asLiveData()
    private val getAllRunsSortedByAvgSpeed: LiveData<List<Run>> = mainRepository.getAllRunsSortedByAvgSpeed().asLiveData()
    private val getAllRunsSortedDistance: LiveData<List<Run>> = mainRepository.getAllRunsSortedDistance().asLiveData()
    private val getAllRunsSortedByCaloriesBurned: LiveData<List<Run>> = mainRepository.getAllRunsSortedByCaloriesBurned().asLiveData()

    val getAllRuns = MediatorLiveData<List<Run>>()

    private var sortType = SortType.DATE
    init {
       getAllRuns.addSource(getAllRunSortByDate){
            if(sortType==SortType.DATE) it?.let { getAllRuns.value = it }
       }
        getAllRuns.addSource(getAllRunsSortedByTime){
            if(sortType==SortType.TIME) it?.let { getAllRuns.value = it }
        }
        getAllRuns.addSource(getAllRunsSortedByAvgSpeed){
            if(sortType==SortType.SPEED) it?.let { getAllRuns.value = it }
        }
        getAllRuns.addSource(getAllRunsSortedDistance){
            Timber.d(sortType.toString())
            if(sortType==SortType.DISTANCE) it?.let { getAllRuns.value = it }
        }
        getAllRuns.addSource(getAllRunsSortedByCaloriesBurned){
            if(sortType==SortType.CALORIES) it?.let { getAllRuns.value = it }
        }
    }

    fun sortRuns(sortType: SortType) = when(sortType){
            SortType.DATE ->getAllRunSortByDate.value?.let { getAllRuns.value = it }
            SortType.TIME ->getAllRunsSortedByTime.value?.let { getAllRuns.value = it }
            SortType.SPEED ->getAllRunsSortedByAvgSpeed.value?.let { getAllRuns.value = it }
            SortType.DISTANCE ->{ getAllRunsSortedDistance.value?.let {
                    getAllRuns.value = it} }
            SortType.CALORIES ->getAllRunsSortedByCaloriesBurned.value?.let { getAllRuns.value = it }
        }.also { this.sortType = sortType }


    suspend fun insert(run: Run):Long {
        var result = 0L
        viewModelScope.launch {
            coroutineScope {
                result = mainRepository.insertRun(run)
            }
        }
        return result
    }
}

