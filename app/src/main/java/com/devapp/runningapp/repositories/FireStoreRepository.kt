package com.devapp.runningapp.repositories

import android.content.Context
import android.widget.Toast
import com.devapp.runningapp.di.IoDispatcher
import com.devapp.runningapp.model.ResourceNetwork
import com.devapp.runningapp.model.Run
import com.devapp.runningapp.model.user.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FireStoreRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val firebaseStore :FirebaseFirestore  by lazy { Firebase.firestore }

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
                ResourceNetwork.Error(null)
            }
        }
        return res
    }

    suspend fun getUserProfile(id: String, @IoDispatcher dispatcher: CoroutineDispatcher = Dispatchers.IO): ResourceNetwork<UserProfile?> {
        val collection = Firebase.firestore.collection("profiles")
        val res = withContext(dispatcher) {
            try {
                val queriesSnapshot = collection
                    .whereEqualTo("uid", id)
                    .orderBy("email")
                    .get()
                    .await()
                ResourceNetwork.Success(queriesSnapshot.documents[0].toObject<UserProfile>())

            } catch (e: Exception) {
                ResourceNetwork.Error(null, e.message)
            }
        }
        return res
    }

    suspend fun updateUserProfile(userProfile:UserProfile, @IoDispatcher dispatcher: CoroutineDispatcher = Dispatchers.IO): ResourceNetwork<UserProfile?> {
        val collection = Firebase.firestore.collection("profiles")
        val res = withContext(dispatcher) {
            try {
                val queriesSnapshot = collection
                    .whereEqualTo("uid", userProfile.uid)
                    .orderBy("email")
                    .get()
                    .await()
                collection.document(queriesSnapshot.documents[0].id).update(userProfile.toMap())
                ResourceNetwork.Success(userProfile)
            } catch (e: Exception) {
                ResourceNetwork.Error(null, e.message)
            }
        }
        return res
    }

    suspend fun addRunInformation(run: Run, @IoDispatcher dispatcher: CoroutineDispatcher = Dispatchers.IO): ResourceNetwork<Map<String,Any>?> {
        val collection = firebaseStore.collection("runs")
        val res = withContext(dispatcher) {
            try {
                val result = collection.add(run).await()
                ResourceNetwork.Success(result.get().await().data)
            } catch (e: Exception) {
                ResourceNetwork.Error(null)
            }
        }
        return res
    }

    suspend fun getAllRunInformation(
        id: String,
        @IoDispatcher dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResourceNetwork<List<Run>?> {
        val collection = Firebase.firestore.collection("runs")
        val res = withContext(dispatcher) {
            try {
                val queriesSnapshot = collection
                    .whereEqualTo("uid", id)
                    .orderBy("timeStamp")
                    .get()
                    .await()
                val listRun:MutableList<Run> = mutableListOf()
                queriesSnapshot.documents.forEach {
                    it.toObject<Run>()?.let { run -> listRun.add(run) }
                }
                ResourceNetwork.Success(listRun)

            } catch (e: Exception) {
                ResourceNetwork.Error(null, e.message)
            }
        }
        return res
    }



}