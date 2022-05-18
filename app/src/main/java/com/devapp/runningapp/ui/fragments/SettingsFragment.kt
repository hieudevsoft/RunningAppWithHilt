package com.devapp.runningapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentSettingsBinding
import com.devapp.runningapp.databinding.FragmentSetupBinding
import com.devapp.runningapp.util.Constant
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    @Inject
    lateinit var preferences: SharedPreferences
    private var _binding: FragmentSettingsBinding?=null
    private val binding get() = _binding!!
    private var hasInitializedRootView = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if(_binding==null) _binding = FragmentSettingsBinding.inflate(inflater,container,false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        }catch (e:Exception){

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasInitializedRootView) return
        hasInitializedRootView = true
        initView()
    }

    private fun initView(){

    }

    override fun onDestroy() {
        _binding=null
        super.onDestroy()
    }

}