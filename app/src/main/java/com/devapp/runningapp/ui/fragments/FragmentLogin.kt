package com.devapp.runningapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentLoginBinding
import com.devapp.runningapp.util.AnimationHelper
import com.devapp.runningapp.util.VoidCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentLogin: Fragment(R.layout.fragment_login) {
    private var _binding : FragmentLoginBinding ? =null
    private val binding get() = _binding!!
    private var isInitialized = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if(_binding==null) _binding = FragmentLoginBinding.inflate(layoutInflater,container,false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        }catch (e:Exception){

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!isInitialized){
            isInitialized = true
            binding.apply {
                btnBack.setOnClickListener {
                    findNavController().popBackStack()
                }
                btnRegisterNow.setOnClickListener {
                    AnimationHelper.scaleAnimation(it,object:VoidCallback{
                        override fun execute() {
                            findNavController().navigate(FragmentLoginDirections.actionFragmentLoginToSignUpFragment())
                        }

                    },0.96f)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

}