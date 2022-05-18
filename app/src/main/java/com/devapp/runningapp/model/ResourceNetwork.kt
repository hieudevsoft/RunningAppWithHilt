package com.devapp.runningapp.model

sealed class ResourceNetwork<out T> {
    object Idle:ResourceNetwork<Nothing>()
    object Loading:ResourceNetwork<Nothing>()
    object Empty:ResourceNetwork<Nothing>()
    class Success<T>(var data: T) : ResourceNetwork<T>()
    class Error<T>(var data:T?=null,var message:String?=null) : ResourceNetwork<T>()
}