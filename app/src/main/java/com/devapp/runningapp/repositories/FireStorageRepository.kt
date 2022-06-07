package com.devapp.runningapp.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import com.devapp.runningapp.di.IoDispatcher
import com.devapp.runningapp.model.ResourceNetwork
import com.devapp.runningapp.util.AppHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FireStorageRepository @Inject constructor(@ApplicationContext private val context:Context) {
    private val storageReference : StorageReference by lazy {Firebase.storage.reference}

    suspend fun addImageById(
        uid: String,
        image: Bitmap,
        timeStamp:Long,
        @IoDispatcher dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): ResourceNetwork<Boolean> {
        val res = withContext(dispatcher) {
            try {
                storageReference.child("images/$uid/$timeStamp").putBytes(AppHelper.bitmapToBytes(image)).await()
                ResourceNetwork.Success(true)
            } catch (e: Exception) {
                ResourceNetwork.Error(false,e.message)
            }
        }
        return res
    }

    suspend fun downloadAllImagesById(
        uid: String,
        @IoDispatcher dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResourceNetwork<HashMap<String,Bitmap>> {
        val ref = Firebase.storage.reference
        val res = withContext(dispatcher) {
            try {
                val images = ref.child("images/$uid").listAll().await()
                val mapData = HashMap<String,Bitmap>()
                images.items.forEach {
                    val bytes = it.getBytes(10L*1024*1024).await()
                    mapData[it.name] = BitmapFactory.decodeByteArray(bytes,0, bytes.size)
                }
                ResourceNetwork.Success(mapData)
            } catch (e: Exception) {
                ResourceNetwork.Error(null,e.message)
            }
        }
        return res
    }
}