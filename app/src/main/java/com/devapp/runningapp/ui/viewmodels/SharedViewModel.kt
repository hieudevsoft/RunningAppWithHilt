package com.devapp.runningapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    app:Application,
):AndroidViewModel(app) {

    private val _lockScreenTracking:MutableSharedFlow<Boolean> = MutableSharedFlow(1)
    var lockScreenTracking = _lockScreenTracking.asSharedFlow()

    fun<T> emitValue(type:String,value:T){
        viewModelScope.launch {
            when(type){
                LOCK_SCREEN_TRACKING->_lockScreenTracking.emit(value as Boolean)
            }
        }
    }

    companion object{
        const val LOCK_SCREEN_TRACKING = "LOCK_SCREEN_TRACKING"
    }
}