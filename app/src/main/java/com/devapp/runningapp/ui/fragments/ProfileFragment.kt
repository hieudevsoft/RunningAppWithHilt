package com.devapp.runningapp.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import com.bumptech.glide.Glide
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentProfileBinding
import com.devapp.runningapp.model.ResourceNetwork
import com.devapp.runningapp.model.user.Gender
import com.devapp.runningapp.model.user.UserProfile
import com.devapp.runningapp.ui.dialog.ChooseGenderDialog
import com.devapp.runningapp.ui.viewmodels.FirebaseViewModel
import com.devapp.runningapp.ui.widgets.DatePickerView
import com.devapp.runningapp.util.AppHelper.fromJson
import com.devapp.runningapp.util.AppHelper.setOnClickWithScaleListener
import com.devapp.runningapp.util.AppHelper.showErrorToast
import com.devapp.runningapp.util.AppHelper.showStyleableToast
import com.devapp.runningapp.util.AppHelper.showSuccessToast
import com.devapp.runningapp.util.AppHelper.showToastNotConnectInternet
import com.devapp.runningapp.util.DateCallback
import com.devapp.runningapp.util.NetworkHelper
import com.devapp.runningapp.util.SharedPreferenceHelper
import com.devapp.runningapp.util.TrackingUtils.hideSoftKeyboard
import com.devapp.runningapp.util.TrackingUtils.toGone
import com.devapp.runningapp.util.TrackingUtils.toVisible
import com.devapp.runningapp.util.VoidCallback
import com.devapp.runningapp.util.firebase.FirebaseAuthClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class ProfileFragment : Fragment(){
    private var _binding:FragmentProfileBinding?=null
    private val binding get() = _binding!!
    private var hasInitializedRootView = false
    private lateinit var userProfile: UserProfile
    private lateinit var datePickerView: DatePickerView
    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper
    private val firebaseViewModel:FirebaseViewModel by viewModels()
    private var clickDrawableEndPassword = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if(_binding==null) _binding= FragmentProfileBinding.inflate(inflater,container,false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        }catch (e:Exception){

        }
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(!sharedPreferenceHelper.userProfile.isNullOrEmpty())
        userProfile = sharedPreferenceHelper.userProfile!!.fromJson(UserProfile::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(hasInitializedRootView) return
        hasInitializedRootView = true
        onSetUpView()
        subscriberObserver()
    }

    private fun subscriberObserver() {
        lifecycle.coroutineScope.launchWhenResumed {
            firebaseViewModel.stateFlowUpdateUserProfile.collect(){
                when(it){
                    is ResourceNetwork.Loading->{
                        showLoading()
                    }

                    is ResourceNetwork.Success->{
                        requireContext().showSuccessToast("Update successfully~")
                        hideLoading()
                    }

                    is ResourceNetwork.Error->{
                        requireContext().showErrorToast("Update failure~")
                        hideLoading()
                    }

                    else->{

                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onSetUpView() {
        if(!::userProfile.isInitialized) return
        binding.apply {
            Glide.with(this@ProfileFragment).asBitmap().centerCrop().load(FirebaseAuthClient.getInstance(Firebase.auth).getCurrentUser()?.photoUrl).into(imgAvatar)
            tvNickName.text = userProfile.nickName
            tvSlogan.text = userProfile.slogan?:"I'm Runner"
            edtName.setText(userProfile.userName?:"")
            edtEmail.setText(userProfile.email)
            edtDob.setText(userProfile.dob?:"")
            edtWeight.setText(userProfile.weight?:"")
            edtPhone.setText(userProfile.phoneNumber?:"")
            edtSex.setText(userProfile.gender.name)
            val dob = userProfile.dob!!
            var dayOfBirth = dob.split("-")[0].toInt()
            var monthOfBirth = dob.split("-")[1].toInt()
            var yearOfBirth = dob.split("-")[2].toInt()
            edtDob.setOnClickListener {
                requireActivity().hideSoftKeyboard()
                datePickerView = DatePickerView(requireContext(), dayOfBirth, monthOfBirth, yearOfBirth,
                    object : DateCallback {
                        override fun execute(day: Int?, month: Int?, year: Int?) {
                            dayOfBirth = day ?: 0
                            monthOfBirth = month ?: 0
                            yearOfBirth = year ?: 0

                            val dateBirth = "$monthOfBirth-$dayOfBirth-$yearOfBirth"
                            val dateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH)
                            val targetFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                            var date: Date? = Date()
                            try {
                                date = dateFormat.parse(dateBirth)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }
                            if (date != null) {
                                val newDate = targetFormat.format(date)
                                binding.edtDob.setText(newDate)
                            }
                        }
                    },
                    object : VoidCallback {
                        override fun execute() {
                            nestedScrollView.removeView(datePickerView)
                        }
                    })
                nestedScrollView.addView(datePickerView)
                datePickerView.animateView()
            }
            edtSex.setOnClickListener {
                ChooseGenderDialog(requireActivity(),if(userProfile.gender==Gender.FEMALE) 0 else 1){
                    userProfile.gender = if(it==0) Gender.FEMALE else Gender.MALE
                    if(it==0) edtSex.setText("FEMALE") else edtSex.setText("MALE")
                }.show()
            }

            btnUpdate.setOnClickWithScaleListener {
                updateProfile()
            }

            edtPassword.setOnTouchListener(OnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= edtPassword.right - edtPassword.compoundDrawables[2].bounds.width()
                    ) {
                        clickDrawableEndPassword+=1
                        if(clickDrawableEndPassword%2==1){
                            edtNewPassword.toVisible()
                            edtConfirmNewPassword.toVisible()
                            edtPassword.setCompoundDrawablesWithIntrinsicBounds(null,null,ResourcesCompat.getDrawable(resources,R.drawable.ic_left,null),null)
                        } else {
                            edtNewPassword.toGone()
                            edtConfirmNewPassword.toGone()
                            edtPassword.setCompoundDrawablesWithIntrinsicBounds(null,null,ResourcesCompat.getDrawable(resources,R.drawable.ic_right,null),null)
                        }
                        return@OnTouchListener true
                    }
                }
                false
            })

            btnChangeAvatar.setOnClickWithScaleListener {
                imageChooser()
            }
        }
    }

    private fun imageChooser() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        launchSomeActivity.launch(i)
    }

    private var launchSomeActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK
        ) {
            val data = result.data
            // do your operation from here....
            if (data != null && data.data != null) {
                val selectedImageUri: Uri? = data.data
                val selectedImageBitmap: Bitmap
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImageUri)
                    binding.imgAvatar.setImageBitmap(selectedImageBitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateProfile() {
        binding.apply {
            if(!NetworkHelper.isInternetConnected(requireContext())){
                requireContext().showToastNotConnectInternet()
                return@apply
            }
            if(edtEmail.text.toString().isEmpty()||edtPhone.text.toString().isEmpty()||edtSex.text.toString().isEmpty()||edtWeight.text.toString().isEmpty()){
                requireContext().showErrorToast(getString(R.string.can_not_null))
                return@apply
            }
            val regexPhone = "^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*\$".toRegex()
            if(!regexPhone.matches(edtPhone.text.toString())){
                requireContext().showErrorToast("Phone number invalid")
                return@apply
            }
            userProfile.apply {
                userName = edtName.text.toString().trim()
                dob = edtDob.text.toString().trim()
                slogan = edtSlogan.text.toString().trim()
                phoneNumber = edtPhone.text.toString().trim()
                weight = edtWeight.text.toString().trim()
                gender = Gender.valueOf(edtSex.text.toString().trim())
            }
            if(clickDrawableEndPassword%2==1){
                if(sharedPreferenceHelper.getNumberEnterPassword(sharedPreferenceHelper.accessUid!!)==0){
                    requireContext().showErrorToast("You have entered wrong more than 5 times")
                    return@apply
                }
                if(userProfile.password!=userProfile.password){
                    sharedPreferenceHelper.setNumberEnterPassword(sharedPreferenceHelper.accessUid!!,sharedPreferenceHelper.getNumberEnterPassword(sharedPreferenceHelper.accessUid!!)-1)
                    requireContext().showStyleableToast("Password is not correct",false)
                    sharedPreferenceHelper
                    return@apply
                }
                if(edtConfirmNewPassword.text.toString().length<6){
                    requireContext().showStyleableToast(getString(R.string.invalid_password),false)
                    return@apply
                }
                if(edtConfirmNewPassword.text.toString()!=edtNewPassword.text.toString()){
                    requireContext().showStyleableToast(getString(R.string.match_submit_password),false)
                    return@apply
                }
                userProfile.password = edtNewPassword.text.toString()
            }
            firebaseViewModel.getStateUpdateUserProfile(userProfile)
        }
    }

    private fun showLoading(){
        binding.apply {
            circleLoading.toVisible()
            btnUpdate.toGone()
        }
    }

    private fun hideLoading(){
        binding.apply {
            circleLoading.toGone()
            btnUpdate.toVisible()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
}