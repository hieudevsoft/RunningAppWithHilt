package com.devapp.runningapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentLoginBinding

class FragmentLogin: Fragment(R.layout.fragment_login) {
    private var _binding : FragmentLoginBinding ? =null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(_binding==null) _binding = FragmentLoginBinding.inflate(layoutInflater,container,false)
        else (binding.root.parent as ViewGroup).removeView(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}