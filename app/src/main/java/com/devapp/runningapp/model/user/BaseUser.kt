package com.devapp.runningapp.model.user

import com.google.gson.annotations.SerializedName

abstract class BaseUser(
    @SerializedName(value = "uid")
    open val uid:String,
    @SerializedName(value = "email")
    open var email:String,
    @SerializedName(value = "password")
    open var password:String)