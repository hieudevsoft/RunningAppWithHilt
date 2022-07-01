package com.devapp.runningapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devapp.runningapp.R
import com.devapp.runningapp.adapters.AwardAdapter
import com.devapp.runningapp.databinding.FragmentAwardBinding
import com.devapp.runningapp.model.Award
import com.devapp.runningapp.model.ItemAward
import com.devapp.runningapp.ui.viewmodels.StatisticsViewModel
import com.devapp.runningapp.util.AppHelper.findOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlin.math.roundToInt

@AndroidEntryPoint
class AwardFragment:Fragment(){
    val TAG = "AwardFragment"
    private val mainViewModels: StatisticsViewModel by viewModels()
    private var _binding:FragmentAwardBinding?=null
    private val binding get() = _binding!!
    private var hasInitializedView = false
    private lateinit var timeAward: AwardAdapter
    private lateinit var avgSpeedAwardAdapter: AwardAdapter
    private lateinit var distanceAwardAdapter: AwardAdapter
    private lateinit var caloriesBurnedAwardAdapter: AwardAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(hasInitializedView) return
        hasInitializedView = true
        onSetupView()
    }

    private fun onSetupView() {
        findOnClickListener(binding.ibBack){
            when(it){
                binding.ibBack->findNavController().popBackStack()
            }
        }
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        if(!::timeAward.isInitialized) timeAward = AwardAdapter()
        if(!::avgSpeedAwardAdapter.isInitialized) avgSpeedAwardAdapter = AwardAdapter()
        if(!::distanceAwardAdapter.isInitialized) distanceAwardAdapter = AwardAdapter()
        if(!::caloriesBurnedAwardAdapter.isInitialized) caloriesBurnedAwardAdapter = AwardAdapter()
        binding.rvTime.apply {
            itemAnimator = SlideInLeftAnimator()
            adapter = ScaleInAnimationAdapter(timeAward).also { it.setDuration(300) }
            timeAward.submitList(listOf(
                ItemAward(R.drawable.ic_startup,"Warm up","Run time more than 2 day",false,100),
                ItemAward(R.drawable.ic_momentum,"Being run more","Run time more than 4 day",false,100),
                ItemAward(R.drawable.ic_hard,"Runner","Run time more than 8 day",true,40),
                ItemAward(R.drawable.ic_time_color,"Lord of time","Run time more than 16 day",false,29),
                ItemAward(R.drawable.ic_doctor,"Doctor strange","Run time more than 20 day",false,10),
            ))
        }

        binding.rvAvgSpeed.apply {
            itemAnimator = SlideInLeftAnimator()
            adapter = ScaleInAnimationAdapter(avgSpeedAwardAdapter).also { it.setDuration(300) }
            avgSpeedAwardAdapter.submitList(listOf(
                ItemAward(R.drawable.ic_warmup_speed,"Warm up","Average speed more than 8 km/h",false,0),
                ItemAward(R.drawable.ic_accleartion,"Acceleration","Average speed more than 18km/h",false,0),
                ItemAward(R.drawable.ic_usain_bolt,"Usain bolt","Average speed more than 26km/h",false,0),
                ItemAward(R.drawable.ic_ghost_rider,"Ghost rider","Average speed more than 32km/h",false,0),
                ItemAward(R.drawable.ic_the_flash,"The flash","Average speed more than 36km/h",false,0),
            ))
        }

        binding.rvDistance.apply {
            itemAnimator = SlideInLeftAnimator()
            adapter = ScaleInAnimationAdapter(distanceAwardAdapter).also { it.setDuration(300) }
            distanceAwardAdapter.submitList(listOf(
                ItemAward(R.drawable.ic_warmup_distance,"Warm up","Running distance more than 40km",false,0),
                ItemAward(R.drawable.ic_enduring,"Being enduring","Running distance more than 100km",false,0),
                ItemAward(R.drawable.ic_distance,"Distancener","Running distance more than 220km",false,0),
                ItemAward(R.drawable.ic_lord_distance,"Lord of distance","Running distance more than 500km",false,0),
                ItemAward(R.drawable.ic_hulk,"The Hulk","Running distance more then 1000km",false,0),
            ))
        }

        binding.rvCalory.apply {
            itemAnimator = SlideInLeftAnimator()
            adapter = ScaleInAnimationAdapter(caloriesBurnedAwardAdapter).also { it.setDuration(300) }
            caloriesBurnedAwardAdapter.submitList(listOf(
                ItemAward(R.drawable.ic_warm_up_calories,"Warm up","Total calories burned more than 10000kcal",false,0),
                ItemAward(R.drawable.ic_calories_fire,"Being burned calories more","Total calories burned more than 300000kcal",false,0),
                ItemAward(R.drawable.ic_calorieser,"Colorieser","Total calories burned more than 600000kcal",false,0),
                ItemAward(R.drawable.ic_the_rock,"The rock","Total calories burned more than 900000kcal",false,0),
                ItemAward(R.drawable.ic_saitama,"One punch man","Total calories burned more than 1000000kcal",false,0),
            ))
        }
        mainViewModels.totalTime.observe(viewLifecycleOwner) {
            it?.let {
                val day = it.toFloat()/(1000*60*60*24)
                timeAward.updateItemListener(0,day<2,Award.getProcess(2,day))
                timeAward.updateItemListener(1,day<4,Award.getProcess(4,day))
                timeAward.updateItemListener(2,day<8,Award.getProcess(8,day))
                timeAward.updateItemListener(3,day<16,Award.getProcess(16,day))
                timeAward.updateItemListener(4,day<20,Award.getProcess(20,day))
                Log.d(TAG, "setupRecyclerView DAY: $day")
            }
        }
        mainViewModels.totalDistance.observe(viewLifecycleOwner) {
            it?.let {
                val totalDistance = (it / 1000f) * 10f.roundToInt() / 10f
                distanceAwardAdapter.updateItemListener(0,totalDistance<40,Award.getProcess(40,totalDistance))
                distanceAwardAdapter.updateItemListener(1,totalDistance<100,Award.getProcess(100,totalDistance))
                distanceAwardAdapter.updateItemListener(2,totalDistance<220,Award.getProcess(220,totalDistance))
                distanceAwardAdapter.updateItemListener(3,totalDistance<500,Award.getProcess(500,totalDistance))
                distanceAwardAdapter.updateItemListener(4,totalDistance<1000,Award.getProcess(1000,totalDistance))
                Log.d(TAG, "setupRecyclerView DISTANCE: $totalDistance")
            }
        }
        mainViewModels.totalAvgSpeed.observe(viewLifecycleOwner) {
            it?.let {
                val avgSpeed = (it * 10f).roundToInt() / 10f
                avgSpeedAwardAdapter.updateItemListener(0,avgSpeed<8,Award.getProcess(8,avgSpeed))
                avgSpeedAwardAdapter.updateItemListener(1,avgSpeed<18,Award.getProcess(18,avgSpeed))
                avgSpeedAwardAdapter.updateItemListener(2,avgSpeed<26,Award.getProcess(26,avgSpeed))
                avgSpeedAwardAdapter.updateItemListener(3,avgSpeed<32,Award.getProcess(32,avgSpeed))
                avgSpeedAwardAdapter.updateItemListener(4,avgSpeed<36,Award.getProcess(36,avgSpeed))
                Log.d(TAG, "setupRecyclerView AVG_SPEED: $avgSpeed")
            }
        }
        mainViewModels.totalCaloriesBurned.observe(viewLifecycleOwner) {
            it?.let {
                caloriesBurnedAwardAdapter.updateItemListener(0,it<10000,Award.getProcess(10000,it.toFloat()))
                caloriesBurnedAwardAdapter.updateItemListener(1,it.toFloat()<30000,Award.getProcess(30000,it.toFloat()))
                caloriesBurnedAwardAdapter.updateItemListener(2,it.toFloat()<60000,Award.getProcess(60000,it.toFloat()))
                caloriesBurnedAwardAdapter.updateItemListener(3,it.toFloat()<90000,Award.getProcess(90000,it.toFloat()))
                caloriesBurnedAwardAdapter.updateItemListener(4,it.toFloat()<10000,Award.getProcess(10000,it.toFloat()))
                Log.d(TAG, "setupRecyclerView CALORY: $it kcal")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if(_binding==null) _binding=FragmentAwardBinding.inflate(layoutInflater,container,false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        }catch (e:Exception){

        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
}