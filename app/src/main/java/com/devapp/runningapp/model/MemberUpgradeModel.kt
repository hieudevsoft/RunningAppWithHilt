package com.devapp.runningapp.model

data class MemberUpgradeModel(
    var uid:String,
    var isUpgrade:Long,
    var freeClick:Long,
    var isPremium:Boolean,
    var lastDate:Long,
    var upgradePackage:Long,
)