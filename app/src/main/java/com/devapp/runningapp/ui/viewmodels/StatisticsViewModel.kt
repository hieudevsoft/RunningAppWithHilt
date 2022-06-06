package com.devapp.runningapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.devapp.runningapp.repositories.MainRepository
import com.devapp.runningapp.util.SharedPreferenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val app:Application,
    mainRepository: MainRepository
): AndroidViewModel(app) {
    private val sharedPreferenceHelper:SharedPreferenceHelper by lazy { SharedPreferenceHelper(getApplication()) }
    val totalTime = mainRepository.getTotalTimeInMillis(sharedPreferenceHelper.accessUid!!).asLiveData()
    val totalDistance = mainRepository.getTotalDistance(sharedPreferenceHelper.accessUid!!).asLiveData()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed(sharedPreferenceHelper.accessUid!!).asLiveData()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned(sharedPreferenceHelper.accessUid!!).asLiveData()

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate(sharedPreferenceHelper.accessUid!!).asLiveData()


}

