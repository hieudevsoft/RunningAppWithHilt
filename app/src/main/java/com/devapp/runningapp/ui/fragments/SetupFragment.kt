package com.devapp.runningapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentSetupBinding
import com.devapp.runningapp.model.user.UserProfile
import com.devapp.runningapp.util.AnimationHelper
import com.devapp.runningapp.util.AppHelper.fromJson
import com.devapp.runningapp.util.Constant
import com.devapp.runningapp.util.VoidCallback
import com.google.firebase.firestore.auth.User
import dagger.hilt.android.AndroidEntryPoint
import io.github.muddz.styleabletoast.StyleableToast
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup){
    @Inject
    lateinit var preferences : SharedPreferences
    private var _binding:FragmentSetupBinding?=null
    private val binding get() = _binding!!
    private var hasInitRootView = false
    private val args:SetupFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if(_binding==null) _binding = FragmentSetupBinding.inflate(inflater,container,false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        }catch (e:Exception){

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(hasInitRootView) return
        hasInitRootView = true
        initView()
    }

    private fun initView() {
        val user = args.user.fromJson<UserProfile>(UserProfile::class.java)
        binding.apply {
            preferences.getString(Constant.KEY_NAME,"")?.let { etName.setContent(it) }?: user.userName?.let { etName.setContent(it) }
            btnContinue.setOnClickListener {
                AnimationHelper.scaleAnimation(it,object : VoidCallback{
                    override fun execute() {
                        if(checkInputInformation())
                        {
                            findNavController().navigate(R.id.action_setupFragment_to_runFragment)
                        }
                        else {
                            StyleableToast.makeText(requireContext(),"Please fill the fields",R.style.toast_error)
                        }
                    }
                },0.96f)
            }
        }
    }

    private fun checkInputInformation():Boolean{
        val name = binding.etName.getContent()
        val weight = binding.etWeight.getContent()
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