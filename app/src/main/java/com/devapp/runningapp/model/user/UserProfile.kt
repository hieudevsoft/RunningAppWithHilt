package com.devapp.runningapp.model.user

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName(value = "uid")
    var uid:String="",
    @SerializedName(value = "email")
    var email:String="",
    @SerializedName(value = "password")
    var password:String="",
    @SerializedName(value = "phoneNumber")
    var phoneNumber:String?=null,
    @SerializedName(value = "userName")
    var userName:String?=null,
    @SerializedName(value = "weight")
    var weight:String?=null,
    @SerializedName(value = "gender")
    var gender:Gender=Gender.OTHER,
    @SerializedName(value = "dob")
    var dob:String?=null,
    @SerializedName(value = "image")
    var image:String?=null,
){
    fun toMap():Map<String,Any?>{
        return hashMapOf(
            "uid" to uid,
            "email" to email,
            "password" to password,
            "phoneNumber" to phoneNumber,
            "userName" to userName,
            "weight" to weight,
            "gender" to gender.name,
            "dob" to dob,
            "image" to image
        )
    }

    fun fromMap(map: Map<String,Any?>){
        if(map["uid"]!=null) this.uid = map["uid"].toString()
        if(map["email"]!=null) this.email = map["email"].toString()
        if(map["password"]!=null) this.password = map["password"].toString()
        if(map["phoneNumber"]!=null) this.phoneNumber = map["phoneNumber"].toString()
        if(map["userName"]!=null) this.userName = map["userName"].toString()
        if(map["gender"]!=null) this.gender = Gender.valueOf(map["gender"].toString())
        if(map["dob"]!=null) this.dob = map["dob"].toString()
        if(map["image"]!=null) this.image = map["image"].toString()
        if(map["weight"]!=null) this.weight = map["weight"].toString()
    }
}