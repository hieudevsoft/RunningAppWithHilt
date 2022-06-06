package com.devapp.runningapp.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.devapp.runningapp.R
import com.devapp.runningapp.adapters.ViewPager2Adapter
import com.devapp.runningapp.databinding.FragmentViewPagerTrackingBinding
import com.devapp.runningapp.services.Polyline
import com.devapp.runningapp.ui.viewmodels.SharedViewModel
import com.devapp.runningapp.util.EndTrackingCallBack
import com.devapp.runningapp.util.LongCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ViewPagerTrackingFragment : Fragment(),EndTrackingCallBack{
    private lateinit var parentContext:Context
    private lateinit var trackingAdapter:ViewPager2Adapter
    private var _binding:FragmentViewPagerTrackingBinding?=null
    private val binding get() = _binding!!
    private var hasInitializedRootView = false
    private val sharedViewModel:SharedViewModel by activityViewModels()
    private lateinit var trackingFragment:TrackingFragment
    private lateinit var detailTrackingFragment:DetailTrackingFragment
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(::parentContext.isInitialized) parentContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if(_binding==null) _binding = FragmentViewPagerTrackingBinding.inflate(inflater,container,false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        }catch (e:Exception){

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(hasInitializedRootView) return
        hasInitializedRootView = true
        onSetupView()
        subscriberObserver()
    }

    private fun subscriberObserver() {
        lifecycleScope.launchWhenResumed {
            sharedViewModel.lockScreenTracking.collect(){
                binding.vpTracking.isUserInputEnabled  = it
            }
        }
    }

    private fun onSetupView(){
        binding.apply {
            if(!::trackingAdapter.isInitialized) trackingAdapter = ViewPager2Adapter(childFragmentManager,lifecycle)
            if(!::detailTrackingFragment.isInitialized) detailTrackingFragment = DetailTrackingFragment().also {
                it.setOnEndTrackingCallBack(this@ViewPagerTrackingFragment)

            }
            if(!::trackingFragment.isInitialized) trackingFragment = TrackingFragment().also { it.setRunCallBack(detailTrackingFragment) }
            trackingAdapter.addFragment(trackingFragment)
            trackingAdapter.addFragment(detailTrackingFragment)
            vpTracking.apply {
                adapter = trackingAdapter
                isUserInputEnabled = true
                offscreenPageLimit = 2
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                    }
                })
            }
        }
    }

    fun navigateToRunFragment(){
        findNavController().navigate(R.id.action_viewPagerTrackingFragment_to_runFragment)
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

    override fun execute(currentTimeInMilis: Long, pathPoints: MutableList<Polyline>) {
        trackingFragment.moveCameraWholeTracking()
        trackingFragment.endAndSaveTrackingToDb(currentTimeInMilis,pathPoints)
    }
}