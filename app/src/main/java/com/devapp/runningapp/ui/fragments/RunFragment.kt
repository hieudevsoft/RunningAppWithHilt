package com.devapp.runningapp.ui.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentRunBinding
import com.devapp.runningapp.databinding.FragmentSetupBinding
import com.devapp.runningapp.ui.viewmodels.MainViewModels
import com.devapp.runningapp.util.Constant.REQUEST_CODE_PERMISSION
import com.devapp.runningapp.util.TrackingUtils
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.jar.Manifest

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run),EasyPermissions.PermissionCallbacks {

    private val mainViewModels:MainViewModels by viewModels()
    private var _binding: FragmentRunBinding?=null
    private val binding get() = _binding!!
    private lateinit var requestPermissionResult:ActivityResultLauncher<String>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRunBinding.inflate(inflater,container,false)
        return binding.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission()
        binding.fab.setOnClickListener {
                findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
            }
        }

    private fun requestPermission(){
        if(TrackingUtils.hasLocationPermission(requireContext())) return
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                this,"You need to accept permission to use this app.",
                REQUEST_CODE_PERMISSION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }else{
            EasyPermissions.requestPermissions(
                this,"You need to accept permission to use this app.",
                REQUEST_CODE_PERMISSION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onDestroy() {
        _binding=null
        super.onDestroy()
    }

    override fun onDestroyView() {
        _binding =null
        super.onDestroyView()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(requestCode== REQUEST_CODE_PERMISSION){
            if(EasyPermissions.somePermissionPermanentlyDenied(this,perms))
            {
                AppSettingsDialog.Builder(this).build().show()
            }else requestPermission()
        }
    }
}