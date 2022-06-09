package com.devapp.runningapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.navigation.fragment.findNavController
import com.devapp.runningapp.R
import com.devapp.runningapp.adapters.AwardAdapter
import com.devapp.runningapp.databinding.FragmentAwardBinding
import com.devapp.runningapp.model.ItemAward
import com.devapp.runningapp.util.AppHelper.findOnClickListener
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter
import jp.wasabeef.recyclerview.animators.OvershootInLeftAnimator
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator

class AwardFragment:Fragment(){

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
                ItemAward(R.drawable.ic_time_color,"Lord of time","Run time more than 16 day",true,29),
                ItemAward(R.drawable.ic_doctor,"Doctor strange","Run time more than 20 day",true,10),
            ))
        }

        binding.rvAvgSpeed.apply {
            itemAnimator = SlideInLeftAnimator()
            adapter = ScaleInAnimationAdapter(avgSpeedAwardAdapter).also { it.setDuration(300) }
            avgSpeedAwardAdapter.submitList(listOf(
                ItemAward(R.drawable.ic_warmup_speed,"Warm up","Average speed more than 8 km/h",false,100),
                ItemAward(R.drawable.ic_accleartion,"Acceleration","Average speed more than 18km/h",false,100),
                ItemAward(R.drawable.ic_usain_bolt,"Usain bolt","Average speed more than 26km/h",true,40),
                ItemAward(R.drawable.ic_ghost_rider,"Ghost rider","Average speed more than 32km/h",true,29),
                ItemAward(R.drawable.ic_the_flash,"The flash","Average speed more than 36km/h",true,10),
            ))
        }

        binding.rvDistance.apply {
            itemAnimator = SlideInLeftAnimator()
            adapter = ScaleInAnimationAdapter(distanceAwardAdapter).also { it.setDuration(300) }
            distanceAwardAdapter.submitList(listOf(
                ItemAward(R.drawable.ic_warmup_distance,"Warm up","Running distance more than 40km",false,100),
                ItemAward(R.drawable.ic_enduring,"Being enduring","Running distance more than 100km",false,100),
                ItemAward(R.drawable.ic_distance,"Distancener","Running distance more than 220km",true,40),
                ItemAward(R.drawable.ic_lord_distance,"Lord of distance","Running distance more than 500km",true,29),
                ItemAward(R.drawable.ic_hulk,"The Hulk","Running distance more then 1000km",true,10),
            ))
        }

        binding.rvCalory.apply {
            itemAnimator = SlideInLeftAnimator()
            adapter = ScaleInAnimationAdapter(caloriesBurnedAwardAdapter).also { it.setDuration(300) }
            caloriesBurnedAwardAdapter.submitList(listOf(
                ItemAward(R.drawable.ic_warm_up_calories,"Warm up","Total calories burned more than 10kcal",false,100),
                ItemAward(R.drawable.ic_calories_fire,"Being burned calories more","Total calories burned more than 30kcal",false,100),
                ItemAward(R.drawable.ic_calorieser,"Colorieser","Total calories burned more than 60kcal",true,40),
                ItemAward(R.drawable.ic_the_rock,"The rock","Total calories burned more than 90kcal",true,29),
                ItemAward(R.drawable.ic_saitama,"One punch man","Total calories burned more than 100kcal",true,10),
            ))
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