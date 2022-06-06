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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devapp.runningapp.R
import com.devapp.runningapp.adapters.RunAdapter
import com.devapp.runningapp.databinding.FragmentRunBinding
import com.devapp.runningapp.model.ResourceNetwork
import com.devapp.runningapp.model.Run
import com.devapp.runningapp.ui.MainActivity
import com.devapp.runningapp.ui.viewmodels.FirebaseViewModel
import com.devapp.runningapp.ui.viewmodels.MainViewModels
import com.devapp.runningapp.ui.widgets.DialogLoading
import com.devapp.runningapp.util.AppHelper.setOnClickWithScaleListener
import com.devapp.runningapp.util.AppHelper.showStyleableToast
import com.devapp.runningapp.util.AppHelper.showToastNotConnectInternet
import com.devapp.runningapp.util.Constant.REQUEST_CODE_PERMISSION
import com.devapp.runningapp.util.NetworkHelper
import com.devapp.runningapp.util.SharedPreferenceHelper
import com.devapp.runningapp.util.SortType
import com.devapp.runningapp.util.TrackingUtils
import dagger.hilt.android.AndroidEntryPoint
import io.github.muddz.styleabletoast.StyleableToast
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run),EasyPermissions.PermissionCallbacks {
    private val mainViewModels:MainViewModels by activityViewModels()
    private val firebaseViewModel:FirebaseViewModel by activityViewModels()
    private var _binding: FragmentRunBinding?=null
    private val binding get() = _binding!!
    private lateinit var runAdapter: RunAdapter
    private lateinit var currentListRun:MutableList<Run>
    private var isSyncDataWithServer = false
    private var isHasInitRootView = false
    private val sharedPreferenceHelper: SharedPreferenceHelper by lazy { SharedPreferenceHelper(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if(_binding==null) _binding = FragmentRunBinding.inflate(inflater, container, false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        }catch (e:Exception) {

        }
        return binding.root
    }

    private fun setupRecyclerView() = binding.rvRuns.apply {
        runAdapter = RunAdapter(childFragmentManager)
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(isHasInitRootView) return
        isHasInitRootView = true
        requestPermission()
        binding.fab.setOnClickListener {
            if(!NetworkHelper.isInternetConnected(requireContext())){
                showToastNotConnectInternet()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_runFragment_to_viewPagerTrackingFragment)
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
        binding.fabRefresh.setOnClickWithScaleListener {
            if(isSyncDataWithServer) return@setOnClickWithScaleListener
            firebaseViewModel.getStateAllRun(sharedPreferenceHelper.accessUid!!)
        }
        lifecycleScope.launchWhenResumed {
            firebaseViewModel.stateFlowGetAllRunByUid.collect(){
                when(it){
                    is ResourceNetwork.Loading->{
                        isSyncDataWithServer = true
                        DialogLoading.show(requireContext())
                    }

                    is ResourceNetwork.Success->{
                        if(it.data==null||it.data!!.isEmpty()) return@collect
                        if(!::currentListRun.isInitialized) currentListRun = mutableListOf()
                        if(currentListRun.size>=1) currentListRun.clear()
                        currentListRun.addAll(it.data!!)
                        firebaseViewModel.getStateAllImageById(sharedPreferenceHelper.accessUid!!)
                    }

                    is ResourceNetwork.Error->{
                        isSyncDataWithServer = false
                        Log.d("TAG", "onViewCreated: ${it.message}")
                        DialogLoading.hide()
                        requireContext().showStyleableToast(getString(R.string.please_try_again),false)
                    }

                    else->{}
                }
            }
        }
        lifecycleScope.launchWhenResumed {
            firebaseViewModel.stateFlowGetAllImageByUid.collect(){
                when(it){
                    is ResourceNetwork.Loading->{
                    }

                    is ResourceNetwork.Success->{
                        isSyncDataWithServer = false
                        DialogLoading.hide()
                        if(it.data==null||it.data!!.isEmpty()) return@collect
                        currentListRun.map { run->
                            run.img = it.data!!.get(run.timeStamp!!.toString())
                            run
                        }
                        Log.d("TAG", "onViewCreated: $currentListRun")
                        mainViewModels.insetAllRun(currentListRun)
                    }

                    is ResourceNetwork.Error->{
                        isSyncDataWithServer = false
                        Log.d("TAG", "onViewCreated: ${it.message}")
                        DialogLoading.hide()
                        requireContext().showStyleableToast(getString(R.string.please_try_again),false)
                    }

                    else->{}
                }
            }
        }

        mainViewModels.getAllRuns.observe(viewLifecycleOwner){
                if(!::currentListRun.isInitialized) currentListRun = mutableListOf()
                currentListRun = it.toMutableList()
                Log.d("TAG", "onViewCreated: $it")
                runAdapter.submitList(it)
        }

        binding.searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)){
                    if(newText.isNullOrEmpty()) runAdapter.submitList(currentListRun)
                    else runAdapter.submitList(currentListRun.filter {
                        SimpleDateFormat("dd-MM-YYYY").format(Date(it.timeStamp!!)).contains(newText.trim().lowercase())
                                ||it.distanceInMeters.toString().contains(newText.trim().lowercase())
                                ||TrackingUtils.getFormattedStopWatchTime(it.timeInRun!!,false).toString().contains(newText.toString().trim().lowercase())
                    })
                }
                return true
            }

        })

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