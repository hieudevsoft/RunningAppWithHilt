package com.devapp.runningapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentStatisticsBinding
import com.devapp.runningapp.databinding.FragmentTrackingBinding
import com.devapp.runningapp.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

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
        return binding.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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