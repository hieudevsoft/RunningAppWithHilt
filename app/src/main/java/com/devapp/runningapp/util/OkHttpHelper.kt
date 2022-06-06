package com.devapp.runningapp.util


import com.devapp.runningapp.model.weather.ResponseWeather
import com.devapp.runningapp.util.AppHelper.fromJson
import com.squareup.okhttp.Callback
import com.squareup.okhttp.RequestBody
import com.squareup.okhttp.Response
import okhttp3.Call
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object OkHttpHelper {
    val TAG = "OkHttpHelper"
    private val CONNECT_TIME_OUT = 30L
    private val WRITE_TIME_OUT = 30L
    private val READ_TIME_OUT = 30L
    private val CALL_TIME_OUT = 30L

    private fun getOkHttpClient(): OkHttpClient {
        return okhttp3.OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(CONNECT_TIME_OUT, TimeUnit.MINUTES)
            .callTimeout(CALL_TIME_OUT, TimeUnit.MINUTES)
            .writeTimeout(WRITE_TIME_OUT, TimeUnit.MINUTES)
            .readTimeout(READ_TIME_OUT, TimeUnit.MINUTES)
            .build()
    }

    private fun createRequest(
        url: String,
        headers: Headers? = null,
        method: String,
        postRequestBody: okhttp3.RequestBody? = null
    ): Request {
        return if (headers != null)
            Request.Builder().url(url).headers(headers).method(method, postRequestBody).build()
        else Request.Builder().url(url).method(method, postRequestBody).build()
    }

    fun getWeatherFromOpenWeatherApi(lat:Float,long:Float,onResultCallBack:(ResponseWeather?)->Unit){
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$long&appid=${Constant.API_KEY_WEATHER}&units=metric"
        val request = createRequest(url,null,"GET")
        try {
            getOkHttpClient().newCall(request).enqueue(object:okhttp3.Callback{
                override fun onFailure(call: Call, e: IOException) {
                    onResultCallBack(null)
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    if(response.isSuccessful){
                        if(response.body!=null){
                            val jsonString = response.body!!.string()
                            onResultCallBack(jsonString.fromJson<ResponseWeather>(ResponseWeather::class.java))
                        }else onResultCallBack(null)
                    }else onResultCallBack(null)
                }

            })
        }catch (e:Exception){
            return
        }

    }
}