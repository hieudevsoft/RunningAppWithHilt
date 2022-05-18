package com.devapp.runningapp.model.user

data class UserProfile(
    override var uid:String="",
    override var email:String="",
    override var password:String="",
    var phoneNumber:String?=null,
    var userName:String?=null,
    var gender:Gender=Gender.FEMALE,
    var dob:String?=null,
    var image:String?=null,
):BaseUser(uid,email,password){
    fun toMap():Map<String,Any?>{
        return hashMapOf(
            "uid" to uid,
            "email" to email,
            "password" to password,
            "phoneNumber" to phoneNumber,
            "userName" to userName,
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
    }
}