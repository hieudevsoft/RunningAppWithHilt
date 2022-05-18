package com.devapp.runningapp.repositories

import android.content.Context
import android.widget.Toast
import com.devapp.runningapp.di.IoDispatcher
import com.devapp.runningapp.model.ResourceNetwork
import com.devapp.runningapp.model.user.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FireStoreRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val firebaseStore = Firebase.firestore

    suspend fun addUserProfile(
        userProfile: UserProfile,
        @IoDispatcher dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResourceNetwork<Map<String,Any>?> {
        val collection = firebaseStore.collection("profiles")
        val res = withContext(dispatcher) {
            try {
                val result = collection.add(userProfile).await()
                ResourceNetwork.Success(result.get().await().data)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
                ResourceNetwork.Error(null)
            }
        }
        return res
    }

    suspend fun getUserProfile(
        id: String,
        @IoDispatcher dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResourceNetwork<UserProfile?> {
        val collection = Firebase.firestore.collection("profiles")
        val res = withContext(dispatcher) {
            try {
                val queriesSnapshot = collection
                    .whereEqualTo("id", id)
                    .orderBy("email")
                    .get()
                    .await()
                ResourceNetwork.Success(queriesSnapshot.documents[0].toObject<UserProfile>())

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
                ResourceNetwork.Error(null, e.message)
            }
        }
        return res
    }
}