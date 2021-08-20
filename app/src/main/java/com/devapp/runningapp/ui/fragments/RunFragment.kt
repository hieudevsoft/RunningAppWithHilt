package com.devapp.runningapp.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devapp.runningapp.R
import com.devapp.runningapp.ui.viewmodels.MainViewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    private val mainViewModels:MainViewModels by viewModels()
}