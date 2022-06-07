package com.devapp.runningapp.ui.fragments

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentStatisticsBinding
import com.devapp.runningapp.ui.viewmodels.StatisticsViewModel
import com.devapp.runningapp.ui.widgets.CustomMarkerView
import com.devapp.runningapp.util.TrackingUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {
    private val mainViewModels: StatisticsViewModel by viewModels()
    private var _binding: FragmentStatisticsBinding?=null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
        setUpBarChat()
    }

    private fun setUpBarChat(){
        binding.barChat.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = ContextCompat.getColor(requireContext(),R.color.colorPrimary)
            textColor = ContextCompat.getColor(requireContext(),R.color.colorPrimary)
            setDrawGridLines(false)
        }
        binding.barChat.axisLeft.apply {
            axisLineColor = ContextCompat.getColor(requireContext(),R.color.colorPrimary)
            textColor = ContextCompat.getColor(requireContext(),R.color.colorPrimary)
            setDrawGridLines(false)
        }
        binding.barChat.axisRight.apply {
            axisLineColor = ContextCompat.getColor(requireContext(),R.color.colorPrimary)
            textColor = ContextCompat.getColor(requireContext(),R.color.colorPrimary)
            setDrawGridLines(false)
        }
        binding.barChat.apply {
            description.text = "Speed"
            description.textColor = ContextCompat.getColor(requireContext(),R.color.black)
            description.textSize = 12f
            description.textAlign = Paint.Align.CENTER
            legend.isEnabled = false
        }
    }

    private fun subscribeObservers(){
        mainViewModels.totalTime.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvTotalTime.text = TrackingUtils.getFormattedStopWatchTime(it)
            }
        }
        mainViewModels.totalDistance.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvTotalDistance.text = "${((it / 1000f) * 10f).roundToInt() / 10f}km"
            }
        }
        mainViewModels.totalAvgSpeed.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvAverageSpeed.text = "${(it * 10f).roundToInt() / 10f}km/h"
            }
        }
        mainViewModels.totalCaloriesBurned.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvTotalCalories.text = "${it}kcal"
            }
        }
        mainViewModels.runsSortedByDate.observe(viewLifecycleOwner) {
            it?.let {
                val listReversed = it.reversed()
                val allAvgSpeed = listReversed.indices.map { i ->
                    BarEntry(
                        i.toFloat(),
                        listReversed[i].avgSpeedInKMH!!
                    )
                }
                val barDataSet = BarDataSet(allAvgSpeed, "Avg Speed Over Time").apply {
                    valueTextColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    valueTextSize = 16f
                    color = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                }
                binding.barChat.marker = CustomMarkerView(listReversed, requireContext(), R.layout.marker_view)
                binding.barChat.data = BarData(barDataSet)
                binding.barChat.invalidate()
            }
        }
    }


    override fun onDestroy() {
        _binding=null
        super.onDestroy()
    }

    override fun onDestroyView() {
        _binding =null
        super.onDestroyView()
    }

}