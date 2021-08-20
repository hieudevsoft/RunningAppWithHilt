package com.devapp.runningapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.devapp.runningapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModels @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {
}

