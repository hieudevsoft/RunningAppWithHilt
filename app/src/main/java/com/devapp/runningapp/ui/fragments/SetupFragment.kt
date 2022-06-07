package com.devapp.runningapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentSetupBinding
import com.devapp.runningapp.model.ResourceNetwork
import com.devapp.runningapp.model.user.UserProfile
import com.devapp.runningapp.ui.viewmodels.FirebaseViewModel
import com.devapp.runningapp.ui.widgets.DialogLoading
import com.devapp.runningapp.util.AnimationHelper
import com.devapp.runningapp.util.AppHelper.fromJson
import com.devapp.runningapp.util.AppHelper.showStyleableToast
import com.devapp.runningapp.util.AppHelper.showToastNotConnectInternet
import com.devapp.runningapp.util.AppHelper.toJson
import com.devapp.runningapp.util.NetworkHelper
import com.devapp.runningapp.util.SharedPreferenceHelper
import com.devapp.runningapp.util.VoidCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {
    private val prefs: SharedPreferenceHelper by lazy { SharedPreferenceHelper(requireContext()) }
    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!
    private var hasInitRootView = false
    private val args: SetupFragmentArgs by navArgs()
    private lateinit var currentUserProfile:UserProfile
    private val fireBaseViewModel: FirebaseViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if (_binding == null) _binding =
                FragmentSetupBinding.inflate(inflater, container, false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        } catch (e: Exception) {

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasInitRootView) return
        hasInitRootView = true
        handleViewModel()
    }

    private fun handleViewModel() {
        fireBaseViewModel.getStateFlowUserProfile(prefs.accessUid!!)
        lifecycle.coroutineScope.launchWhenStarted {
            fireBaseViewModel.stateFlowGetUserProfile.collect() {
                when (it){
                    is ResourceNetwork.Loading->{
                        DialogLoading.show(requireContext())
                    }

                    is ResourceNetwork.Success->{
                        if(it.data==null) {
                            requireContext().showStyleableToast("Data is empty :(",false)
                            return@collect
                        }
                        currentUserProfile = it.data!!
                        prefs.userProfile = it.data!!.toJson()
                        initView()
                        DialogLoading.hide()
                    }
                    is ResourceNetwork.Error->{
                        if(prefs.userProfile!=null||prefs.userProfile!!.isNotEmpty()) currentUserProfile = prefs.userProfile!!.fromJson(UserProfile::class.java)
                        DialogLoading.hide()
                        initView()
                        requireContext().showStyleableToast(getString(R.string.please_try_again),false)
                    }
                    else->{}
                }
            }
        }
        lifecycle.coroutineScope.launchWhenStarted {
            fireBaseViewModel.stateFlowUpdateUserProfile.collect() {
                when (it){
                    is ResourceNetwork.Loading->{
                        DialogLoading.show(requireContext())
                    }

                    is ResourceNetwork.Success->{
                        if(it.data==null) {
                            requireContext().showStyleableToast("Error Opps :(",false)
                            return@collect
                        }
                        DialogLoading.hide()
                        prefs.userProfile = it.data!!.toJson()
                        prefs.weightUser = it.data!!.weight.toString()
                        prefs.userProfile = it.data!!.toJson()
                        delay(1000)
                        findNavController().navigate(SetupFragmentDirections.actionSetupFragmentToRunFragment())
                    }
                    is ResourceNetwork.Error->{
                        DialogLoading.hide()
                        requireContext().showStyleableToast(getString(R.string.please_try_again),false)
                    }
                    else->{}
                }
            }
        }
    }

    private fun initView() {
        val user = args.user.fromJson<UserProfile>(UserProfile::class.java)
        binding.apply {
            currentUserProfile.nickName.let {
                if (it != null) {
                    etName.setContent(it)
                } else currentUserProfile.userName?.let { etName.setContent(it) }?:"Runner"
            }
            currentUserProfile.weight.let {
                if (it != null) {
                    etWeight.setContent(it)
                } else user.weight?.let { etWeight.setContent(it) }
            }
            tvWelcome.text = String.format(getString(R.string.welcome), etName.getContent().ifEmpty { "Runner" })
            btnContinue.setOnClickListener {
                AnimationHelper.scaleAnimation(it, object : VoidCallback {
                    override fun execute() {
                        Log.d("TAG", "execute: ${prefs.userProfile}")
                        if (checkInputInformation()) {
                            if(!NetworkHelper.isInternetConnected(requireContext())){
                                requireContext().showStyleableToast("Mode offline enabled",true)
                                return
                            }
                            if(currentUserProfile.nickName==binding.etName.getContent().trim()&&currentUserProfile.weight==etWeight.getContent().trim()){
                                findNavController().navigate(SetupFragmentDirections.actionSetupFragmentToRunFragment())
                                return
                            }
                            currentUserProfile.nickName = binding.etName.getContent()
                            currentUserProfile.weight = binding.etWeight.getContent()
                            fireBaseViewModel.getStateUpdateUserProfile(currentUserProfile)
                        } else {
                            showStyleableToast("Please fill the fields", false)
                        }
                    }
                }, 0.96f)
            }
        }
    }

    private fun checkInputInformation(): Boolean {
        val name = binding.etName.getContent()
        val weight = binding.etWeight.getContent()
        try {
            if (name.isEmpty() || weight.isEmpty()) return false
            weight.toFloat()
        } catch (e: Exception) {
            showStyleableToast("Weight format is not valid", false)
            return false
        }
        return true
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}