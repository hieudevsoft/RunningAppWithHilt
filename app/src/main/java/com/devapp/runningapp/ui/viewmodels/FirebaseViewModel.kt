package com.devapp.runningapp.ui.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devapp.runningapp.model.ResourceNetwork
import com.devapp.runningapp.model.Run
import com.devapp.runningapp.model.user.BaseUser
import com.devapp.runningapp.model.user.UserProfile
import com.devapp.runningapp.repositories.FireStorageRepository
import com.devapp.runningapp.repositories.FireStoreRepository
import com.devapp.runningapp.util.firebase.FirebaseAuthClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.core.FirestoreClient
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel @Inject constructor(
    app:Application,
    private val fireStoreRepository: FireStoreRepository,
    private val fireStorageRepository: FireStorageRepository,
):AndroidViewModel(app) {

    private val _stateFlowAddUser:MutableStateFlow<ResourceNetwork<Map<String,Any>?>> = MutableStateFlow(ResourceNetwork.Idle)
    val stateFlowAddUser = _stateFlowAddUser.asStateFlow()
    fun getStateFlowAdUser(user:UserProfile){
        _stateFlowAddUser.value = ResourceNetwork.Loading
        viewModelScope.launch {
            _stateFlowAddUser.value = fireStoreRepository.addUserProfile(user)
        }
    }

    private val _stateFlowGetUserProfile:MutableStateFlow<ResourceNetwork<UserProfile?>> = MutableStateFlow(ResourceNetwork.Idle)
    val stateFlowGetUserProfile = _stateFlowGetUserProfile.asStateFlow()
    fun getStateFlowUserProfile(uid:String){
        _stateFlowGetUserProfile.value = ResourceNetwork.Loading
        viewModelScope.launch {
            _stateFlowGetUserProfile.value = fireStoreRepository.getUserProfile(uid)
        }
    }

    private val _stateFlowAddRunInformation:MutableStateFlow<ResourceNetwork<Map<String,Any>?>> = MutableStateFlow(ResourceNetwork.Idle)
    val stateFlowAddRunInformation = _stateFlowAddRunInformation.asStateFlow()
    fun getStateFlowAddRunInformation(run: Run){
        _stateFlowAddRunInformation.value = ResourceNetwork.Loading
        viewModelScope.launch {
            _stateFlowAddRunInformation.value = fireStoreRepository.addRunInformation(run)
        }
    }

    private val _stateAddImageToStorage:MutableStateFlow<ResourceNetwork<Boolean?>> = MutableStateFlow(ResourceNetwork.Idle)
    val stateFlowAddImageToFirestore = _stateAddImageToStorage.asStateFlow()
    fun getStateAddImageToFireStore(uid:String,image:Bitmap){
        _stateAddImageToStorage.value = ResourceNetwork.Loading
        viewModelScope.launch {
            _stateAddImageToStorage.value = fireStorageRepository.addImageById(uid,image)
        }
    }

}