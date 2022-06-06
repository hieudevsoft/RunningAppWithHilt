package com.devapp.runningapp.ui.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.devapp.runningapp.R
import com.devapp.runningapp.adapters.ViewPager2Adapter
import com.devapp.runningapp.databinding.FragmentDetailTrackingBinding
import com.devapp.runningapp.databinding.FragmentViewPagerTrackingBinding
import com.devapp.runningapp.model.Run
import com.devapp.runningapp.services.Polyline
import com.devapp.runningapp.services.TrackingService
import com.devapp.runningapp.ui.viewmodels.FirebaseViewModel
import com.devapp.runningapp.ui.viewmodels.SharedViewModel
import com.devapp.runningapp.ui.widgets.CancelTrackingDialog
import com.devapp.runningapp.util.*
import com.devapp.runningapp.util.AppHelper.setOnClickWithScaleListener
import com.devapp.runningapp.util.AppHelper.showStyleableToast
import com.devapp.runningapp.util.AppHelper.showToastNotConnectInternet
import com.devapp.runningapp.util.TrackingUtils.toGone
import com.devapp.runningapp.util.TrackingUtils.toVisible
import com.shashank.sony.fancydialoglib.Animation
import com.shashank.sony.fancydialoglib.FancyAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import java.lang.StringBuilder
import java.util.*
import kotlin.math.roundToInt

@AndroidEntryPoint
class DetailTrackingFragment:Fragment(),RunCallBack {
    private lateinit var parentContext: Context
    private var _binding: FragmentDetailTrackingBinding?=null
    private val binding get() = _binding!!
    private var hasInitializedRootView = false
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var isTracking = false
    private var currentTimeInMillis = 0L
    private var currentTemp = 0L
    private var currentFeelslike = 0L
    private var currentHumidity = 0L
    private var currentSpeedWind = 0L
    private var stringBuilder = StringBuilder()
    private var currentWeatherFeature = 0
    private var isFirstGetApi = true
    private lateinit var onEndTrackingCallBack:LongCallback
    private val sharedPreferenceHelper:SharedPreferenceHelper by lazy { SharedPreferenceHelper(context=requireContext()) }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(!::parentContext.isInitialized) parentContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if(_binding==null) _binding = FragmentDetailTrackingBinding.inflate(inflater,container,false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        }catch (e:Exception){

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(hasInitializedRootView) return
        hasInitializedRootView = true
        if(savedInstanceState!=null){
            val cancelDialog = parentFragmentManager.findFragmentByTag("CANCEL_TRACKING_DIALOG") as CancelTrackingDialog?
            cancelDialog?.apply {
                setYesListener {
                    stopRun()
                }
            }
        }
        onSetupView()
        subscriberObserver()
    }

    private fun subscriberObserver() {
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            currentTimeInMillis = it
            if (currentTimeInMillis > 0) binding.ibClose.toVisible()
            val timeInFormatted = TrackingUtils.getFormattedStopWatchTime(currentTimeInMillis, false)
            binding.tvDuration.text = timeInFormatted
        }

        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }

        TrackingService.responseWeather.observe(viewLifecycleOwner){
            if(it==null) return@observe
            currentTemp = it.main.temp.toLong()
            currentFeelslike = it.main.feelsLike.toLong()
            currentHumidity = it.main.humidity.toLong()
            currentSpeedWind = it.wind.speed.toLong()
            binding.apply {
                Glide.with(requireContext()).asBitmap().load("http://openweathermap.org/img/w/${it.weather[0].icon}.png").centerInside().into(binding.imgWeather)
                stringBuilder.clear()
                it.weather.forEach {
                    stringBuilder.append("${it.main} ${it.description}\n")
                }
                tvDesWeather.text = stringBuilder.toString()
                if(!isFirstGetApi) return@observe
                isFirstGetApi = false
                tvTemperature.text = "$currentTemp°C"
            }
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && currentTimeInMillis>0L) {
            binding.imgToggleRun.setImageResource(R.drawable.ic_round_play)
            binding.btnFinish.toVisible()
        } else if(isTracking){
            binding.imgToggleRun.setImageResource(R.drawable.ic_round_pause)
            binding.btnFinish.toGone()
            binding.ibClose.toVisible()
        }
    }

    private fun onSetupView(){
        binding.apply {
            btnLockScreen.setOnClickWithScaleListener {
                if(imgLockScreen.drawable.constantState == ResourcesCompat.getDrawable(resources,R.drawable.ic_lock_close,null)?.constantState){
                    imgLockScreen.setImageResource(R.drawable.ic_lock_open)
                    sharedViewModel.emitValue(SharedViewModel.LOCK_SCREEN_TRACKING,true)
                }else{
                    imgLockScreen.setImageResource(R.drawable.ic_lock_close)
                    sharedViewModel.emitValue(SharedViewModel.LOCK_SCREEN_TRACKING,false)
                }
            }

            ibClose.setOnClickWithScaleListener {
                showDialogCancelTracking()
            }

            btnToggleRun.setOnClickWithScaleListener {
                toggleRun()
            }


            btnFinish.setOnClickWithScaleListener {
                if(NetworkHelper.isInternetConnected(requireContext())){
                    FancyAlertDialog.Builder
                        .with(requireContext())
                        .setTitle("Quit")
                        .setBackgroundColorRes(R.color.colorPrimary)
                        .setMessage("Do you really want to Exit ?")
                        .setNegativeBtnText("Cancel")
                        .setPositiveBtnBackgroundRes(R.color.black)
                        .setPositiveBtnText("Yes")
                        .setNegativeBtnBackgroundRes(R.color.colorRed_5)
                        .setAnimation(Animation.POP)
                        .isCancellable(true)
                        .setIcon(R.drawable.ic_run, View.VISIBLE)
                        .onPositiveClicked { dialog: Dialog? ->
                            stopRun()
                            onEndTrackingCallBack.execute(currentTimeInMillis)
                        }
                        .onNegativeClicked {}
                        .build()
                        .show()
                } else showToastNotConnectInternet()
            }

            tvTemperature.setOnClickWithScaleListener {
                currentWeatherFeature+=1
                changeModeWeatherFeature(currentWeatherFeature%4)
            }
        }
    }

    private fun changeModeWeatherFeature(currentWeatherFeature: Int) {
        if(!isFirstGetApi) return
        when(currentWeatherFeature){
            0->{
                binding.tvTemperature.apply {
                    text = "$currentTemp°C"
                    setCompoundDrawables(ResourcesCompat.getDrawable(resources,R.drawable.ic_sunny,null),null,null,null)
                }
            }

            1->{
                binding.tvTemperature.apply {
                    text = "$currentTemp°C"
                    setCompoundDrawables(ResourcesCompat.getDrawable(resources,R.drawable.ic_feels_like,null),null,null,null)
                }
            }

            2->{
                binding.tvTemperature.apply {
                    text = "${currentHumidity}atm"
                    setCompoundDrawables(ResourcesCompat.getDrawable(resources,R.drawable.ic_water,null),null,null,null)
                }
            }

            else->{
                binding.tvTemperature.apply {
                    text = "${currentSpeedWind}km/h"
                    setCompoundDrawables(ResourcesCompat.getDrawable(resources,R.drawable.ic_cloud,null),null,null,null)
                }
            }
        }
    }

    fun setOnEndTrackingCallBack(callBack:LongCallback){
        this.onEndTrackingCallBack = callBack
    }

    private fun showDialogCancelTracking() {
        CancelTrackingDialog().apply {
            setYesListener { stopRun() }
        }.show(parentFragmentManager,"CANCEL_TRACKING_DIALOG")
    }

    private fun stopRun() {
        sendCommandToService(Constant.ACTION_STOP_SERVICE)
        if (requireParentFragment() is ViewPagerTrackingFragment) (requireParentFragment() as ViewPagerTrackingFragment).navigateToRunFragment()
    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(Constant.ACTION_PAUSE_SERVICE)
            binding.ibClose.toVisible()
            binding.imgToggleRun.setImageResource(R.drawable.ic_round_play)
        } else {
            sendCommandToService(Constant.ACTION_START_OR_RESUME_SERVICE)
            binding.imgToggleRun.setImageResource(R.drawable.ic_round_pause)
        }
    }

    private fun sendCommandToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

    override fun execute(pathPoins: MutableList<Polyline>) {
        val distanceInMeters = TrackingUtils.getDistanceForTracking(polylines = pathPoins)
        val avgSpeedInKMH = ((distanceInMeters / 1000f) /(currentTimeInMillis/(1000f*60f*60f))*10f).roundToInt()/10f
        Calendar.getInstance().timeInMillis
        val mph = avgSpeedInKMH/1.61
        val MET:Float = if(mph<=6) 2f else if(mph<=10) 6f else 10f
        val caloriesBurned = String.format("%.2f",(distanceInMeters/1000)*sharedPreferenceHelper.weightUser.toFloat() * MET)
        binding.apply {
            tvAvgSpeed.text = "${avgSpeedInKMH}km/h"
            tvCalories.text = caloriesBurned+"kcal"
            tvDistance.text = String.format("%.2f",distanceInMeters/1000)
        }
    }
}