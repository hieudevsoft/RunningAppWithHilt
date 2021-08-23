package com.devapp.runningapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.devapp.runningapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    mainRepository: MainRepository
): ViewModel() {
    val totalTime = mainRepository.getTotalTimeInMillis().asLiveData()
    val totalDistance = mainRepository.getTotalDistance().asLiveData()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed().asLiveData()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned().asLiveData()

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate().asLiveData()


}

