package com.devapp.runningapp.util

import com.devapp.runningapp.model.SettingTypes

interface VoidCallback {
    fun execute()
}

interface FloatCallback {
    fun execute(data: Float)
}

interface IntCallback {
    fun execute(data: Int)
}

interface LongCallback {
    fun execute(data: Int)
}

interface DoubleCallback {
    fun execute(data: Double)
}

interface StringCallback {
    fun execute(string: String)
}

interface String2Callback {
    fun execute(string1: String, string2: String)
}

interface BooleanCallback {
    fun execute(boolean: Boolean)
}

interface DateCallback {
    fun execute(day: Int?, month: Int?, year: Int?)
}

interface SettingListener {
    fun itemSwitchExecute(type: SettingTypes, isChecked: Boolean)
    fun itemClickListener(type: SettingTypes)
    fun goUrl(url: String)
    fun logOutListener()
}
