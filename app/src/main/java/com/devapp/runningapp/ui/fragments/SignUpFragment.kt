package com.devapp.runningapp.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.Patterns.EMAIL_ADDRESS
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.util.PatternsCompat
import androidx.core.util.PatternsCompat.*
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentSignUpBinding
import com.devapp.runningapp.model.ResourceNetwork
import com.devapp.runningapp.model.user.Gender
import com.devapp.runningapp.model.user.UserProfile
import com.devapp.runningapp.ui.viewmodels.FirebaseViewModel
import com.devapp.runningapp.ui.widgets.DatePickerView
import com.devapp.runningapp.ui.widgets.DialogLoading
import com.devapp.runningapp.util.AnimationHelper
import com.devapp.runningapp.util.AppHelper.toJson
import com.devapp.runningapp.util.DateCallback
import com.devapp.runningapp.util.NetworkHelper
import com.devapp.runningapp.util.TrackingUtils.hideSoftKeyboard
import com.devapp.runningapp.util.TrackingUtils.toGone
import com.devapp.runningapp.util.TrackingUtils.toVisible
import com.devapp.runningapp.util.VoidCallback
import com.devapp.runningapp.util.firebase.FirebaseAuthClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import io.github.muddz.styleabletoast.StyleableToast
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.tasks.await
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    val TAG = "SignUpFragment"
    private val firebaseViewModel:FirebaseViewModel by viewModels()
    private var _binding:FragmentSignUpBinding?=null
    private val binding get() = _binding!!
    private var isInitialized = false
    private var datePickerView: DatePickerView? = null
    private var dayOfBirth: Int = 0
    private var monthOfBirth: Int = 0
    private var yearOfBirth: Int = 0
    private var signInStatus = 0
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if(_binding==null) _binding = FragmentSignUpBinding.inflate(layoutInflater,container,false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        }catch (e:Exception){

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!isInitialized){
            isInitialized = true
            initUI()
        }
    }


    private fun initUI(){
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            tvPolicyRegister.apply {
                text = HtmlCompat.fromHtml(requireContext().getString(R.string.login_agree_text), HtmlCompat.FROM_HTML_MODE_COMPACT)
                movementMethod =LinkMovementMethod.getInstance()
            }

            itemBirthdayRegister.getViewEditContent().setOnClickListener {
                requireActivity().hideSoftKeyboard()
                datePickerView = DatePickerView(requireContext(),dayOfBirth,monthOfBirth,yearOfBirth,object :DateCallback{
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
                            itemBirthdayRegister.setContent(newDate)
                        }
                    }

                },object:VoidCallback{
                    override fun execute() {
                        if (datePickerView != null) {
                            viewContainer.removeView(datePickerView)
                            datePickerView = null
                        }
                    }
                })
                viewContainer.addView(datePickerView)
                datePickerView!!.animateView()
            }

            btnRegister.setOnClickListener {
                AnimationHelper.scaleAnimation(it, object : VoidCallback {
                    override fun execute() {
                        resetViewWarning()
                        if (itemNameRegister.getContent().trim().isEmpty()) {
                            AnimationHelper.shakeAnimation(context, itemNameRegister.getViewContent(), object : VoidCallback {
                                    override fun execute() {
                                        itemNameRegister.showWarning(requireContext().getString(R.string.can_not_null))
                                    }
                                })
                            return
                        }

                        if (itemEmailRegister.getContent().trim().isEmpty()) {
                            AnimationHelper.shakeAnimation(context, itemEmailRegister.getViewContent(), object : VoidCallback {
                                    override fun execute() {
                                        itemEmailRegister.showWarning(requireContext().getString(R.string.can_not_null))
                                    }
                                })
                            return
                        }

                        if (!PatternsCompat.EMAIL_ADDRESS.matcher((itemEmailRegister.getContent().trim())).matches())
                         {
                            AnimationHelper.shakeAnimation(context, itemEmailRegister.getViewContent(), object : VoidCallback {
                                    override fun execute() {
                                        itemEmailRegister.showWarning(requireContext().getString(R.string.invalid_email))
                                    }
                                })
                            return
                        }

                        if (!checkBirthDay()) {
                            AnimationHelper.shakeAnimation(
                                context,
                                itemBirthdayRegister.getViewContent(),
                                object : VoidCallback {
                                    override fun execute() {
                                        itemBirthdayRegister.showWarning(requireContext().getString(R.string.invalid_date_of_birth))
                                    }
                                })
                            return
                        }

                        if (itemPasswordRegister.getContent().trim().isEmpty()) {
                            AnimationHelper.shakeAnimation(context, itemPasswordRegister.getViewContent(),
                                object : VoidCallback {
                                    override fun execute() {
                                        itemPasswordRegister.showWarning(requireContext().getString(R.string.can_not_null))
                                    }
                                })
                            return
                        }

                        if (itemPasswordRegister.getContent().trim().length < 6) {
                            AnimationHelper.shakeAnimation(
                                context,
                                itemPasswordRegister.getViewContent(),
                                object : VoidCallback {
                                    override fun execute() {
                                        itemPasswordRegister.showWarning(requireContext().getString(R.string.invalid_password))
                                    }
                                })
                            return
                        }

                        if (itemConfirmPasswordRegister.getContent().trim().isEmpty()) {
                            AnimationHelper.shakeAnimation(
                                context, itemConfirmPasswordRegister.getViewContent(),
                                object : VoidCallback {
                                    override fun execute() {
                                        itemConfirmPasswordRegister.showWarning(
                                            requireContext().getString(
                                                R.string.can_not_null
                                            )
                                        )
                                    }
                                })
                            return
                        }

                        if (itemConfirmPasswordRegister.getContent().trim() !=
                            itemPasswordRegister.getContent().trim()
                        ) {
                            AnimationHelper.shakeAnimation(
                                context, itemConfirmPasswordRegister.getViewContent(),
                                object : VoidCallback {
                                    override fun execute() {
                                        itemConfirmPasswordRegister.showWarning(requireContext().getString(R.string.match_submit_password))
                                    }
                                })
                            return
                        }

                        clearItemFocus()

                        if (NetworkHelper.isInternetConnected(context!!)) {
                            if (!cbPolicyRegister.isChecked) {
                                tvWarningRegister.visibility = View.VISIBLE
                                tvWarningRegister.text = requireContext().getString(R.string.agree_term_and_condition)
                                return
                            }

                            isLoading = true
                            signInStatus = 1
                            btnRegister.toGone()
                            pbRegisterRegister.toVisible()
                            //call API register
                            lifecycleScope.launchWhenResumed {
                                FirebaseAuthClient.getInstance(Firebase.auth).registerWithEmailAndPassword(binding.itemEmailRegister.getContent(),binding.itemPasswordRegister.getContent(),{
                                    firebaseUser ->
                                    Log.d(TAG, "execute: $firebaseUser")
                                    val profileUpdate = UserProfileChangeRequest.Builder().setDisplayName(binding.itemNameRegister.getContent()).setPhotoUri("https://t4.ftcdn.net/jpg/00/23/72/59/360_F_23725944_W2aSrg3Kqw3lOmU4IAn7iXV88Rnnfch1.jpg".toUri()).build()
                                    firebaseUser?.updateProfile(profileUpdate)
                                        ?.addOnCompleteListener {
                                            if(it.isSuccessful){
                                                val userProfile = UserProfile()
                                                firebaseUser.let {
                                                    userProfile.uid = it.uid
                                                    userProfile.email = it.email?:""
                                                    userProfile.password = binding.itemPasswordRegister.getContent()
                                                    userProfile.phoneNumber = it.phoneNumber?:""
                                                    userProfile.userName = it.displayName?:""
                                                    userProfile.gender = Gender.OTHER
                                                    userProfile.dob = binding.itemBirthdayRegister.getContent()
                                                    userProfile.image= if(it.photoUrl==null) "" else it.photoUrl.toString()
                                                    firebaseViewModel.getStateFlowAdUser(userProfile)
                                                }
                                                loginWithEmailAndGetResponseAddUserProfile()
                                            } else StyleableToast.makeText(requireContext(),getString(R.string.please_try_again),R.style.toast_error)
                                        }
                                }){
                                    StyleableToast.makeText(requireContext(),getString(R.string.no_connect),R.style.toast_error)
                                }
                            }
                        } else {
                            tvWarningRegister.apply {
                                visibility = View.VISIBLE
                                text = requireContext().getString(R.string.no_connect)
                            }
                        }

                    }
                }, 0.96f)
            }

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    when {
                        itemNameRegister.isFocus() -> itemNameRegister.hideWarning()
                        itemEmailRegister.isFocus() -> itemEmailRegister.hideWarning()
                        itemPasswordRegister.isFocus() -> itemPasswordRegister.hideWarning()
                        itemConfirmPasswordRegister.isFocus() -> itemConfirmPasswordRegister.hideWarning()
                    }
                }
            }

            itemNameRegister.addTextChangedListener(textWatcher)
            itemEmailRegister.addTextChangedListener(textWatcher)
            itemPasswordRegister.addTextChangedListener(textWatcher)
            itemConfirmPasswordRegister.addTextChangedListener(textWatcher)
        }
    }

    private fun loginWithEmailAndGetResponseAddUserProfile() {
        lifecycleScope.launchWhenStarted {
            firebaseViewModel.stateFlowAddUser.collect(){
                when(it){
                    is ResourceNetwork.Loading->{
                        DialogLoading.show(requireContext())
                    }

                    is ResourceNetwork.Success->{
                        DialogLoading.hide()
                        it.data?.let {
                            FirebaseAuthClient.getInstance(Firebase.auth).loginWithEmailAndPassword(it["email"] as String,it["password"] as String,{
                                it?.let {
                                    binding.apply {
                                        btnRegister.toVisible()
                                        pbRegisterRegister.toGone()
                                    }
                                    Log.d(TAG, "loginWithEmailAndGetResponseAddUserProfile: $it")
                                    findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToSetupFragment(it.toJson()))
                                }
                            }){
                                StyleableToast.makeText(requireContext(),it.toString(),R.style.toast_error)
                            }
                        }?:StyleableToast.makeText(requireContext(),getString(R.string.please_try_again),R.style.toast_error)
                    }

                    is ResourceNetwork.Error->{
                        DialogLoading.hide()
                        StyleableToast.makeText(requireContext(),it.message,R.style.toast_error)
                    }

                    else->{

                    }
                }
            }
        }
    }

    private fun resetViewWarning() {
        binding.apply {
            tvWarningRegister.visibility = View.GONE
            itemNameRegister.hideWarning()
            itemEmailRegister.hideWarning()
            itemBirthdayRegister.hideWarning()
            itemPasswordRegister.hideWarning()
            itemConfirmPasswordRegister.hideWarning()
        }
    }

    private fun clearItemFocus() {

        binding.apply {
            when {
                itemNameRegister.isFocus() -> {
                    itemNameRegister.getViewEditContent().clearFocus()
                }
                itemEmailRegister.isFocus() -> {
                    itemEmailRegister.getViewEditContent().clearFocus()
                }
                itemPasswordRegister.isFocus() -> {
                    itemPasswordRegister.getViewEditContent().clearFocus()
                }
                itemConfirmPasswordRegister.isFocus() -> {
                    itemConfirmPasswordRegister.getViewEditContent().clearFocus()
                }
            }

        }
    }

    private fun checkBirthDay(): Boolean {
        if (yearOfBirth > 0 && monthOfBirth > 0 && dayOfBirth > 0) {
            val timeNow = Date().time
            val date = Date(timeNow * 1000)

            val calendar = Calendar.getInstance()
            calendar.time = date

            val yearServer = calendar[Calendar.YEAR]

            var monthServer = calendar[Calendar.MONTH]
            monthServer = if (monthServer == 12) 1 else monthServer + 1

            val dayServer = calendar[Calendar.DAY_OF_MONTH]

            if (yearServer > 0 && monthServer > 0 && dayServer > 0
                && yearServer < yearOfBirth || (yearServer == yearOfBirth && monthServer < monthOfBirth)
                || (yearServer == yearOfBirth && monthServer == monthOfBirth && dayServer <= dayOfBirth)
            ) return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
}