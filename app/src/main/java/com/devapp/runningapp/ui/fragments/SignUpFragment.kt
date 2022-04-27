package com.devapp.runningapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentSignUpBinding
import com.devapp.runningapp.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private var _binding:FragmentSignUpBinding?=null
    private val binding get() = _binding!!
    private var isInitialized = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if(_binding==null) _binding = FragmentSignUpBinding.inflate(layoutInflater,container,false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        }catch (e:Exception){

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!isInitialized){
            isInitialized = true
            initUI()
        }
    }

    private fun initUI(){
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
}