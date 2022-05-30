package com.devapp.runningapp.ui.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devapp.runningapp.R
import com.devapp.runningapp.adapters.RunAdapter
import com.devapp.runningapp.databinding.FragmentRunBinding
import com.devapp.runningapp.ui.MainActivity
import com.devapp.runningapp.ui.viewmodels.MainViewModels
import com.devapp.runningapp.util.AppHelper.showToastNotConnectInternet
import com.devapp.runningapp.util.Constant.REQUEST_CODE_PERMISSION
import com.devapp.runningapp.util.NetworkHelper
import com.devapp.runningapp.util.SortType
import com.devapp.runningapp.util.TrackingUtils
import dagger.hilt.android.AndroidEntryPoint
import io.github.muddz.styleabletoast.StyleableToast
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run),EasyPermissions.PermissionCallbacks {
    private val mainViewModels:MainViewModels by viewModels()
    private var _binding: FragmentRunBinding?=null
    private val binding get() = _binding!!
    private lateinit var runAdapter: RunAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRunBinding.inflate(inflater, container, false)
        return binding.root!!
    }

    private fun setupRecyclerView() = binding.rvRuns.apply {
        runAdapter = RunAdapter(childFragmentManager)
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission()
        binding.fab.setOnClickListener {
            if(!NetworkHelper.isInternetConnected(requireContext())){
                showToastNotConnectInternet()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }

        setUpSearchView()

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
        mainViewModels.getAllRuns.observe(viewLifecycleOwner){
                runAdapter.submitList(it)
        }

    }

    private fun setUpSearchView() {
        val searchAutoComplete = binding.searchView.findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text)
        searchAutoComplete.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
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