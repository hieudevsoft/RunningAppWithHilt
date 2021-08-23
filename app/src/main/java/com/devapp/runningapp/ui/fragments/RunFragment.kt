package com.devapp.runningapp.ui.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devapp.runningapp.R
import com.devapp.runningapp.RunAdapter
import com.devapp.runningapp.databinding.FragmentRunBinding
import com.devapp.runningapp.ui.viewmodels.MainViewModels
import com.devapp.runningapp.util.Constant.REQUEST_CODE_PERMISSION
import com.devapp.runningapp.util.SortType
import com.devapp.runningapp.util.TrackingUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run),EasyPermissions.PermissionCallbacks {
    private val mainViewModels:MainViewModels by viewModels()
    private var _binding: FragmentRunBinding?=null
    private val binding get() = _binding!!
    private lateinit var requestPermissionResult: ActivityResultLauncher<String>
    private lateinit var runAdapter: RunAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRunBinding.inflate(inflater, container, false)
        return binding.root!!
    }

    fun setupRecyclerView() = binding.rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission()
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }


        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0->mainViewModels.sortRuns(SortType.DATE)
                    1->mainViewModels.sortRuns(SortType.TIME)
                    2->mainViewModels.sortRuns(SortType.DISTANCE)
                    3->mainViewModels.sortRuns(SortType.SPEED)
                    4->mainViewModels.sortRuns(SortType.CALORIES)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
        setupRecyclerView()
            mainViewModels.getAllRuns.observe(viewLifecycleOwner,{
                Log.d("RunFragment", "onViewCreated: $it")
                runAdapter.submitList(it)
        })

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