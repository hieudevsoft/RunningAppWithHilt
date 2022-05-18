package com.devapp.runningapp.util.firebase

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthClient private constructor(private val auth: FirebaseAuth){
    companion object{
        @Volatile
        var instance:FirebaseAuthClient?=null
        fun getInstance(auth:FirebaseAuth):FirebaseAuthClient{
            return instance ?: synchronized(this){
                instance?: synchronized(this){
                    instance = FirebaseAuthClient(auth)
                    instance
                }?: instance!!
            }
        }
    }

    fun getCurrentUser() = auth.currentUser

    suspend fun registerWithEmailAndPassword(email:String, password:String,onSuccess:(FirebaseUser?)->Unit,onFailure:(String?)->Unit){
        try {
            val user = auth.createUserWithEmailAndPassword(email,password).await()
            onSuccess(user.user)
        }catch (e:Exception){
            onFailure(e.message)
        }
    }

    suspend fun loginWithEmailAndPassword(email:String, password:String,onSuccess:(FirebaseUser?)->Unit,onFailure:(String?)->Unit){
        try {
            val user = auth.signInWithEmailAndPassword(email,password).await()
            onSuccess(user.user)
        }catch (e:Exception){
            onFailure(e.message)
        }
    }

    suspend fun loginWithCredential(credential: AuthCredential,onSuccess:(FirebaseUser?)->Unit,onFailure:(String?)->Unit){
        try {
            val user = auth.signInWithCredential(credential).await()
            onSuccess(user.user)
        }catch (e:Exception){
            onFailure(e.message)
        }
    }

    fun signOut() = auth.signOut()
}