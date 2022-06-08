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
    private lateinit var awardAdapter: AwardAdapter
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
        if(!::awardAdapter.isInitialized) awardAdapter = AwardAdapter()
        binding.rvTime.apply {
            itemAnimator = SlideInLeftAnimator()
            adapter = ScaleInAnimationAdapter(awardAdapter).also { it.setDuration(300) }
            awardAdapter.submitList(listOf(
                ItemAward(R.drawable.ic_startup,"Warm up","Run time from 2 to 4 weeks",false,100),
                ItemAward(R.drawable.ic_momentum,"Being run more","Run time from 4 to 8 weeks",false,100),
                ItemAward(R.drawable.ic_hard,"Runner","Run time from 8 to 16 weeks",true,40),
                ItemAward(R.drawable.ic_time_color,"Lord of time","Run time from 16 to 24 weeks",true,29),
                ItemAward(R.drawable.ic_doctor,"Doctor strange","Run time more than 24 weeks",true,10),
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