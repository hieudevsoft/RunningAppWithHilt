package com.devapp.runningapp.util

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