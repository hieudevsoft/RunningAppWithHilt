package com.devapp.runningapp.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentTrackingBinding
import com.devapp.runningapp.model.ResourceNetwork
import com.devapp.runningapp.model.Run
import com.devapp.runningapp.services.Polyline
import com.devapp.runningapp.services.TrackingService
import com.devapp.runningapp.ui.viewmodels.FirebaseViewModel
import com.devapp.runningapp.ui.viewmodels.MainViewModels
import com.devapp.runningapp.ui.widgets.CancelTrackingDialog
import com.devapp.runningapp.ui.widgets.DialogLoading
import com.devapp.runningapp.util.AppHelper.showStyleableToast
import com.devapp.runningapp.util.AppHelper.showToastNotConnectInternet
import com.devapp.runningapp.util.Constant.ACTION_PAUSE_SERVICE
import com.devapp.runningapp.util.Constant.ACTION_START_OR_RESUME_SERVICE
import com.devapp.runningapp.util.Constant.ACTION_STOP_SERVICE
import com.devapp.runningapp.util.Constant.DEFAULT_ZOOM_CAMERA
import com.devapp.runningapp.util.Constant.POLYLINE_WIDTH
import com.devapp.runningapp.util.NetworkHelper
import com.devapp.runningapp.util.SharedPreferenceHelper
import com.devapp.runningapp.util.TrackingUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.shashank.sony.fancydialoglib.Animation
import com.shashank.sony.fancydialoglib.FancyAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import io.github.muddz.styleabletoast.StyleableToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.roundToInt


@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private val mainViewModels: MainViewModels by activityViewModels()
    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    private var marker: Marker? = null
    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!
    private var googleMap: GoogleMap? = null
    private var currentTimeInMillis = 0L
    private lateinit var run:Run
    private val sharedPreferenceHelper:SharedPreferenceHelper by lazy { SharedPreferenceHelper(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        if(savedInstanceState!=null){
            val cancelDialog = parentFragmentManager.findFragmentByTag("CANCEL_TRACKING_DIALOG") as CancelTrackingDialog?
            cancelDialog?.apply {
                setYesListener {
                    stopRun()
                }
            }
        }

        binding.mapView.getMapAsync {
            googleMap = it
            addAllPolyline()
        }
        binding.btnCancelTracking.setOnClickListener {
           showDialogCancelTracking()
        }
        binding.btnFinishRun.setOnClickListener {
            moveCameraWholeTracking()
            endAndSaveTrackingToDb()
        }
        subscribeToObservers()
    }

    private fun showDialogCancelTracking() {
        CancelTrackingDialog().apply {
            setYesListener { stopRun() }
        }.show(parentFragmentManager,"CANCEL_TRACKING_DIALOG")
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }


    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }
        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()
            moveCameraLatestPoint()
        }
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            currentTimeInMillis = it
            if (currentTimeInMillis > 0) binding.btnCancelTracking.visibility = View.VISIBLE
            val timeInFormatted = TrackingUtils.getFormattedStopWatchTime(currentTimeInMillis, true)
            binding.tvTimer.text = timeInFormatted
        }

        lifecycle.coroutineScope.launchWhenResumed {
            firebaseViewModel.stateFlowAddImageToFirestore.collect(){
                when(it){
                    is ResourceNetwork.Loading->{
                        DialogLoading.show(requireContext())
                    }

                    is ResourceNetwork.Success->{
                        firebaseViewModel.getStateFlowAddRunInformation(run)
                    }

                    is ResourceNetwork.Error->{
                        DialogLoading.hide()
                        showStyleableToast(it.message?:"Opps",false)
                    }
                }
            }
        }

        lifecycle.coroutineScope.launchWhenResumed {
            firebaseViewModel.stateFlowAddRunInformation.collect(){
                when(it){
                    is ResourceNetwork.Loading->{
                    }

                    is ResourceNetwork.Success->{
                        DialogLoading.hide()
                        lifecycle.coroutineScope.launch(Dispatchers.Main) {
                            val result = withContext(Dispatchers.IO) {
                                mainViewModels.insert(run)
                            }
                            if(result>-1) showStyleableToast("Save tracking successfully~ ",true)
                        }
                    }

                    is ResourceNetwork.Error->{
                        DialogLoading.hide()
                        showStyleableToast(it.message?:"Opps",false)
                    }
                }
            }
        }
    }

    private fun moveCameraLatestPoint() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    DEFAULT_ZOOM_CAMERA
                )
            )
        }
    }

    fun moveCameraWholeTracking(){
        val bounds = LatLngBounds.Builder()
        pathPoints.forEach {
            it.forEach {
                bounds.include(it)
            }
        }
        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height*0.005f).roundToInt()
            )
        )
    }

    private fun endAndSaveTrackingToDb(){
        googleMap?.snapshot { bitmap->
            val distanceInMeters = TrackingUtils.getDistanceForTracking(polylines = pathPoints)
            val avgSpeedInKMH = ((distanceInMeters / 1000f) /(currentTimeInMillis/(1000f*60f*60f))*10f).roundToInt()/10f
            val timeStamp = Calendar.getInstance().timeInMillis
            val mph = avgSpeedInKMH/1.61
            val MET:Float = if(mph<=6) 10f else if(mph<=10) 13.5f else 20f
            val caloriesBurned = (distanceInMeters / 1000f).roundToInt() * sharedPreferenceHelper.weightUser.toFloat() * MET
            if(sharedPreferenceHelper.accessUid.isNullOrEmpty()) return@snapshot
            run = Run(bitmap,timeStamp,avgSpeedInKMH,distanceInMeters.toInt(),currentTimeInMillis, caloriesBurned.roundToInt(), sharedPreferenceHelper.accessUid!!)
            if(NetworkHelper.isInternetConnected(requireContext())){
                FancyAlertDialog.Builder
                    .with(requireContext())
                    .setTitle("Quit")
                    .setBackgroundColorRes(R.color.colorPrimary)
                    .setMessage("Do you really want to Exit ?")
                    .setNegativeBtnText("Cancel")
                    .setPositiveBtnBackgroundRes(R.color.black)
                    .setPositiveBtnText("Yes")
                    .setNegativeBtnBackgroundRes(R.color.colorRed_5)
                    .setAnimation(Animation.POP)
                    .isCancellable(true)
                    .setIcon(R.drawable.ic_run, View.VISIBLE)
                    .onPositiveClicked { dialog: Dialog? ->
                        if(run.img==null || run.id.isEmpty()) {
                            showStyleableToast(getString(R.string.please_try_again),false)
                            return@onPositiveClicked
                        }
                        stopRun()
                        firebaseViewModel.getStateAddImageToFireStore(run.id,run.img!!)
                    }
                    .onNegativeClicked {}
                    .build()
                    .show()
            } else showToastNotConnectInternet()
      }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && currentTimeInMillis>0L) {
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else if(isTracking){
            binding.btnToggleRun.text = "Stop"
            binding.btnFinishRun.visibility = View.GONE
            binding.btnCancelTracking.visibility = View.VISIBLE
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
            binding.btnCancelTracking.visibility = View.VISIBLE
        } else sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
    }

    private fun addAllPolyline() {
        if (pathPoints.isNotEmpty()) {
            for (polyline in pathPoints) {
                val polylineOptions = PolylineOptions().apply {
                    color(ContextCompat.getColor(requireContext(),R.color.colorPrimary))
                    width(POLYLINE_WIDTH)
                    addAll(polyline)
                }
                googleMap?.addPolyline(polylineOptions)
            }
//            googleMap?.addMarker(MarkerOptions()
//                .position(pathPoints.last().last())
//                .title("Right here...")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
//            )
        }
    }

    private fun addLatestPolyline() {
        marker?.let { marker!!.remove() }
        if (pathPoints.isNotEmpty() && pathPoints.last().size >= 2) {
            val preLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions().apply {
                color(ContextCompat.getColor(requireContext(),R.color.colorPrimary))
                width(POLYLINE_WIDTH)
                add(preLatLng)
                add(lastLatLng)
            }
            googleMap?.addPolyline(polylineOptions)

            marker = googleMap?.addMarker(
                MarkerOptions()
                    .position(lastLatLng)
                    .title("Right here...")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
            )
        }
    }

    private fun sendCommandToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    override fun onResume() {
        binding.mapView.onResume()
        super.onResume()
    }

    override fun onStart() {
        binding.mapView.onStart()
        super.onStart()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.mapView.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        binding.mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            if(_binding!=null) binding.mapView.onSaveInstanceState(outState)
        }catch (e:Exception){

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}