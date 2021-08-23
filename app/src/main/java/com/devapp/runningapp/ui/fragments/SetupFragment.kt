package com.devapp.runningapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentSetupBinding
import com.devapp.runningapp.util.Constant
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup){

    @Inject
    lateinit var preferences : SharedPreferences
    private var _binding:FragmentSetupBinding?=null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetupBinding.inflate(inflater,container,false)
        return binding.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isFirstTime = preferences.getBoolean(Constant.KEY_IS_FIRST_TIME,true)
        if(!isFirstTime){
            val navOptions = NavOptions.Builder().apply {
                setPopUpTo(R.id.setupFragment,true)
            }.build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }
        binding.tvContinue.setOnClickListener {
            if(checkInputInformation())
            {
               findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }
            else {
                if(binding.etName.text.toString().isEmpty()) binding.etName.error = "Name is require"
                else binding.etWeight.error = "Weight is require"
            }
        }
    }

    fun checkInputInformation():Boolean{
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if(name.isEmpty()||weight.isEmpty()) return false
        preferences.edit()
            .putString(Constant.KEY_NAME,name)
            .putInt(Constant.KEY_WEIGHT,weight.toInt())
            .putBoolean(Constant.KEY_IS_FIRST_TIME,false)
            .apply()
        return true
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