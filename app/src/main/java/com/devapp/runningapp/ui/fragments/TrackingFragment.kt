package com.devapp.runningapp.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentTrackingBinding
import com.devapp.runningapp.db.Run
import com.devapp.runningapp.services.Polyline
import com.devapp.runningapp.services.TrackingService
import com.devapp.runningapp.ui.viewmodels.MainViewModels
import com.devapp.runningapp.util.Constant
import com.devapp.runningapp.util.Constant.ACTION_PAUSE_SERVICE
import com.devapp.runningapp.util.Constant.ACTION_START_OR_RESUME_SERVICE
import com.devapp.runningapp.util.Constant.ACTION_STOP_SERVICE
import com.devapp.runningapp.util.Constant.DEFAULT_ZOOM_CAMERA
import com.devapp.runningapp.util.Constant.POLYLINE_COLOR
import com.devapp.runningapp.util.Constant.POLYLINE_WIDTH
import com.devapp.runningapp.util.TrackingUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Math.round
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    @Inject
    lateinit var preferences: SharedPreferences
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private val mainViewModels: MainViewModels by viewModels()
    private var marker: Marker? = null
    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!
    private var googleMap: GoogleMap? = null
    private var currentTimeInMillis = 0L
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }
        binding.mapView.getMapAsync {
            googleMap = it
            addAllPolyline()
        }
        binding.btnCancelTracking.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Cancel Tracking?")
                .setMessage("Are you sure to cancel the current tracking and delete all data ?")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("YES") { _, _ ->
                    stopRun()
                }
                .setNegativeButton("NO") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    dialogInterface.cancel()
                }.create()
            dialog.show()
        }
        binding.btnFinishRun.setOnClickListener {
            moveCameraWholeTracking()
            endAndSaveTrackingToDb()
        }
        subscribeToObservers()
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }


    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, {
            pathPoints = it
            addLatestPolyline()
            moveCameraLatestPoint()
        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, {
            currentTimeInMillis = it
            if (currentTimeInMillis > 0) binding.btnCancelTracking.visibility = View.VISIBLE
            val timeInFormatted = TrackingUtils.getFormattedStopWatchTime(currentTimeInMillis, true)
            binding.tvTimer.text = timeInFormatted
        })
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

    var weight = preferences.getInt(Constant.KEY_WEIGHT,50)
    fun endAndSaveTrackingToDb(){
        googleMap?.snapshot { bitmap->
            val distanceInMeters = TrackingUtils.getDistanceForTracking(polylines = pathPoints)
            val avgSpeedInKMH = (distanceInMeters / 1000f).roundToInt() /(currentTimeInMillis/1000/60/60).toFloat()
            val timeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = round(distanceInMeters / 1000f) *weight
            val run = Run(bitmap,timeStamp,avgSpeedInKMH,distanceInMeters.toInt(),currentTimeInMillis,caloriesBurned.toInt())
            lifecycle.coroutineScope.launch(Dispatchers.Main) {
                val result = mainViewModels.insert(run)
                Timber.d(result.toString())
                if(result>-1) Snackbar.make(
                    binding.root,
                    "Save tracking successfully~ ",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            stopRun()
      }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else {
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
                    color(POLYLINE_COLOR)
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
                color(POLYLINE_COLOR)
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
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}