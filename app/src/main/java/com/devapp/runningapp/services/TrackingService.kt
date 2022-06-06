package com.devapp.runningapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.devapp.runningapp.R
import com.devapp.runningapp.model.weather.ResponseWeather
import com.devapp.runningapp.ui.MainActivity
import com.devapp.runningapp.util.Constant.ACTION_PAUSE_SERVICE
import com.devapp.runningapp.util.Constant.ACTION_START_OR_RESUME_SERVICE
import com.devapp.runningapp.util.Constant.ACTION_STOP_SERVICE
import com.devapp.runningapp.util.Constant.ACTION_TO_TRACKING_INTENT
import com.devapp.runningapp.util.Constant.CHANNEL_ID
import com.devapp.runningapp.util.Constant.FASTEST_LOCATION_UPDATE_INTERVAL
import com.devapp.runningapp.util.Constant.LOCATION_UPDATE_INTERVAL
import com.devapp.runningapp.util.Constant.NOTIFICATION_ID
import com.devapp.runningapp.util.Constant.NOTIFICATION_NAME
import com.devapp.runningapp.util.OkHttpHelper
import com.devapp.runningapp.util.TrackingUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder : NotificationCompat.Builder

    private lateinit var currentNotificationBuilder : NotificationCompat.Builder

    private val timeRunInSeconds = MutableLiveData<Long>()
    private var isUnLockCallApi = true
    private var isServiceKill = false
    private var currentLatitude = 0f
    private var currentLongitude = 0f

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        private var isFirstRun = true
        var isTracking = MutableLiveData<Boolean>()
        var pathPoints = MutableLiveData<Polylines>()
        var responseWeather = MutableLiveData<ResponseWeather>()
    }

    private fun postInitialValue(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        Timber.d("ServiceKill: $isServiceKill")
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialValue()
        isTracking.observe(this) {
            updateLocationTracking(it)
            updateTrackingNotification(it)
        }
    }

    private fun updateTrackingNotification(isTracking: Boolean){
        val actionText = if(isTracking) "Pause" else "Resume"
        val pendingIntent:PendingIntent = if(isTracking) {
            val pauseIntent = Intent(this,TrackingService::class.java)
            pauseIntent.action = ACTION_PAUSE_SERVICE
            PendingIntent.getService(this,2,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        }else{
            val resumeIntent = Intent(this,TrackingService::class.java)
            resumeIntent.action = ACTION_START_OR_RESUME_SERVICE
            PendingIntent.getService(this,3,resumeIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val joinAppIntent = Intent(this,MainActivity::class.java)
        joinAppIntent.action = ACTION_TO_TRACKING_INTENT
        val trackingIntent = PendingIntent.getActivity(this,4,joinAppIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder,ArrayList<NotificationCompat.Action>())
        }
        if(!isServiceKill){
            currentNotificationBuilder = baseNotificationBuilder.addAction(R.drawable.ic_pause,actionText,pendingIntent).setContentIntent(trackingIntent)
            notificationManager.notify(NOTIFICATION_ID,currentNotificationBuilder.build())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        startTimer()
                        Timber.d("Resuming Service ....")
                    }

                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused Service...")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stop Service!")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun killService() {
        isServiceKill = true
        isTimerEnabled = false
        isFirstRun = true
        pauseService()
        postInitialValue()
        stopForeground(true)
        stopSelf()
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer(){
        if(isServiceKill) return
        isTracking.postValue(true)
        addEmptyPolyline()
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        lifecycleScope.launchWhenCreated {
            withContext(Dispatchers.Main){
                while (isTracking.value!!){
                    lapTime = System.currentTimeMillis()-timeStarted
                    timeRunInMillis.postValue(lapTime+timeRun)
                    if(timeRunInMillis.value!!>=lastSecondTimeStamp+1000L){
                        lastSecondTimeStamp+=1000
                        timeRunInSeconds.postValue(timeRunInSeconds.value!!+1)
                    }
                    delay(50L)
                }
                timeRun+=lapTime
            }
        }
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun addPathPoint(location: Location?){
        location?.let{
            val pos = LatLng(location.latitude,location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking:Boolean){
        if(isServiceKill) return
        if(isTracking){
            if(TrackingUtils.hasLocationPermission(this)){
                val requestLocation = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_UPDATE_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(requestLocation,locationCallback, Looper.getMainLooper())
            }
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }


    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            if(isTracking.value!!){
                p0?.locations?.let{
                    for(location in it){
                        addPathPoint(location)
                        currentLatitude = location.latitude.toFloat()
                        currentLongitude = location.longitude.toFloat()
                    }
                }
                lifecycle.coroutineScope.launch{
                    if(isUnLockCallApi){
                        OkHttpHelper.getWeatherFromOpenWeatherApi(currentLatitude,currentLongitude){
                            Log.d("TAG", "onLocationResult: $it")
                            responseWeather.postValue(it)
                        }
                    }
                    isUnLockCallApi = false
                    delay(1000*60*10)
                    isUnLockCallApi = true
                }
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    }?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        startTimer()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) onCreateNotificationChannel(
            notificationManager
        )

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.EFFECT_HEAVY_CLICK))
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE))
        }else vibrator.vibrate(500)

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
        timeRunInSeconds.observe(this) {
            if (!isServiceKill) {
                val notification = currentNotificationBuilder.setContentText(
                    TrackingUtils.getFormattedStopWatchTime(
                        it * 1000,
                        false
                    )
                )
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        }

    }



    private fun onCreateNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val channel = NotificationChannel(CHANNEL_ID, NOTIFICATION_NAME, IMPORTANCE_LOW)
            channel.lightColor = Color.GRAY
            channel.enableLights(true)
            channel.setSound(sound,
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
            )
            notificationManager.createNotificationChannel(channel)
        } else {
            return
        }

    }
}