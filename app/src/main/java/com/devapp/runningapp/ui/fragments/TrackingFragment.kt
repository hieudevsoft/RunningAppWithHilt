package com.devapp.runningapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentTrackingBinding
import com.devapp.runningapp.model.ResourceNetwork
import com.devapp.runningapp.model.Run
import com.devapp.runningapp.services.Polyline
import com.devapp.runningapp.services.TrackingService
import com.devapp.runningapp.ui.viewmodels.FirebaseViewModel
import com.devapp.runningapp.ui.viewmodels.MainViewModels
import com.devapp.runningapp.ui.widgets.DialogLoading
import com.devapp.runningapp.util.AppHelper.showStyleableToast
import com.devapp.runningapp.util.AppHelper.showToastNotConnectInternet
import com.devapp.runningapp.util.Constant.DEFAULT_ZOOM_CAMERA
import com.devapp.runningapp.util.Constant.POLYLINE_WIDTH
import com.devapp.runningapp.util.NetworkHelper
import com.devapp.runningapp.util.RunCallBack
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.roundToInt


@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private var pathPoints = mutableListOf<Polyline>()
    private val mainViewModels: MainViewModels by activityViewModels()
    private val firebaseViewModel: FirebaseViewModel by activityViewModels()
    private var marker: Marker? = null
    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!
    private var googleMap: GoogleMap? = null
    private lateinit var run:Run
    private lateinit var runFirebase:Run
    private val sharedPreferenceHelper:SharedPreferenceHelper by lazy { SharedPreferenceHelper(requireContext()) }
    private lateinit var runCallBack: RunCallBack
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
        binding.mapView.getMapAsync {
            googleMap = it
            addAllPolyline()
        }
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()
            moveCameraLatestPoint()
        }

        lifecycle.coroutineScope.launchWhenCreated {
            firebaseViewModel.stateFlowAddImageToFirestore.collect(){
                when(it){
                    is ResourceNetwork.Loading->{
                        Log.d("Tracking", "subscribeToObservers: loading add Image")
                        DialogLoading.show(requireContext())
                    }

                    is ResourceNetwork.Success->{
                        Log.d("Tracking", "subscribeToObservers: $runFirebase")
                        if(::runFirebase.isInitialized) firebaseViewModel.getStateFlowAddRunInformation(runFirebase)
                    }

                    is ResourceNetwork.Error->{
                        DialogLoading.hide()
                        showStyleableToast(it.message?:"Opps",false)
                    }

                    else->{}
                }
            }
        }

        lifecycle.coroutineScope.launchWhenCreated{
            firebaseViewModel.stateFlowAddRunInformation.collect(){
                when(it){
                    is ResourceNetwork.Loading->{
                        Log.d("Tracking", "subscribeToObservers: loading")
                    }

                    is ResourceNetwork.Success->{
                        DialogLoading.hide()
                            val result = withContext(Dispatchers.Main) {
                                mainViewModels.insert(run)
                            }
                            if(result>-1) showStyleableToast("Save tracking successfully~ ",true)
                        delay(1000)
                        (requireParentFragment() as ViewPagerTrackingFragment).navigateToRunFragment()
                    }

                    is ResourceNetwork.Error->{
                        DialogLoading.hide()
                        showStyleableToast(it.message?:"Opps",false)
                    }

                    else->{}
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

    fun endAndSaveTrackingToDb(currentTimeInMillis:Long,mPathPoints:MutableList<Polyline>){
        if(!NetworkHelper.isInternetConnected(requireContext())){
            requireContext().showToastNotConnectInternet()
            return
        }
        googleMap?.snapshot { bitmap->
            val distanceInMeters = TrackingUtils.getDistanceForTracking(polylines = mPathPoints)
            val avgSpeedInKMH = ((distanceInMeters / 1000f) /(currentTimeInMillis/(1000f*60f*60f))*10f).roundToInt()/10f
            val timeStamp = Calendar.getInstance().timeInMillis
            val mph = avgSpeedInKMH/1.61
            val MET:Float = if(mph<=6) 2f else if(mph<=10) 6f else 10f
            val caloriesBurned = String.format("%.2f",(distanceInMeters/1000)*sharedPreferenceHelper.weightUser.toFloat() * MET).toFloat().toInt()
            if(sharedPreferenceHelper.accessUid.isNullOrEmpty()) return@snapshot
            run = Run(bitmap,timeStamp,avgSpeedInKMH,distanceInMeters.toInt(),currentTimeInMillis, caloriesBurned, sharedPreferenceHelper.accessUid!!)
            runFirebase = Run(null,timeStamp,avgSpeedInKMH,distanceInMeters.toInt(),currentTimeInMillis, caloriesBurned, sharedPreferenceHelper.accessUid!!)
            if(run.img==null || run.uid!!.isEmpty()) {
                showStyleableToast(getString(R.string.please_try_again),false)
                return@snapshot
            }
            firebaseViewModel.getStateAddImageToFireStore(run.uid!!,run.img!!,run.timeStamp!!)
      }
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
            runCallBack.execute(pathPoints)
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

    fun setRunCallBack(runCallBack: RunCallBack){
        this.runCallBack = runCallBack
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}