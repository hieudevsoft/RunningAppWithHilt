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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater,container,false)
        return binding.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDataFromPreferenceApp()
        binding.btnApplyChanges.setOnClickListener {
            if(checkInputInformation())
                Snackbar.make(
                    binding.root,
                    "Update Successfully",
                    Snackbar.LENGTH_LONG
                ).show()
            else {
                if(binding.etName.text.toString().isEmpty()) binding.etName.error = "Name is require"
                else binding.etWeight.error = "Weight is require"
            }
        }
    }

    private fun loadDataFromPreferenceApp() {
        val name = preferences.getString(Constant.KEY_NAME,"")
        val weight = preferences.getInt(Constant.KEY_WEIGHT,0)
        binding.etName.setText(name)
        binding.etWeight.setText(if (weight==0) "" else weight.toString())
    }


    fun checkInputInformation():Boolean{
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if(name.isEmpty()||weight.isEmpty()) return false
        preferences.edit()
            .putString(Constant.KEY_NAME,name)
            .putInt(Constant.KEY_WEIGHT,weight.toInt())
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