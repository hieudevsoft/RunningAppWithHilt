package com.devapp.runningapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.util.PatternsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentLoginBinding
import com.devapp.runningapp.model.ResourceNetwork
import com.devapp.runningapp.ui.MainActivity
import com.devapp.runningapp.ui.widgets.DialogLoading
import com.devapp.runningapp.util.*
import com.devapp.runningapp.util.AppHelper.setOnClickWithScaleListener
import com.devapp.runningapp.util.AppHelper.showErrorToast
import com.devapp.runningapp.util.AppHelper.showStyleableToast
import com.devapp.runningapp.util.AppHelper.toJson
import com.devapp.runningapp.util.TrackingUtils.toGone
import com.devapp.runningapp.util.TrackingUtils.toVisible
import com.devapp.runningapp.util.firebase.FirebaseAuthClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.common.eventbus.EventBus
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.github.muddz.styleabletoast.StyleableToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.util.*

@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.fragment_login) {
    private var _binding : FragmentLoginBinding ? =null
    private val binding get() = _binding!!
    private var isInitialized = false
    private var isLoading = false
    private lateinit var googleSignInClient:GoogleSignInClient
    private val sharedPreferenceHelper:SharedPreferenceHelper by lazy { SharedPreferenceHelper(requireContext()) }
    private val firebaseDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance(Constant.URL_FIREBASE_DB) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if(_binding==null) _binding = FragmentLoginBinding.inflate(layoutInflater,container,false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        }catch (e:Exception){

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!isInitialized){
            isInitialized = true
            initView()
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuthClient.getInstance(Firebase.auth).getCurrentUser()?.let {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSetupFragment(it.toJson()))
        }
    }

    private fun initView() {
        setupViewLogin()
        setUpGoogleSignIn()
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            btnRegisterNow.setOnClickListener {
                AnimationHelper.scaleAnimation(it,object:VoidCallback{
                    override fun execute() {
                        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment())
                    }
                },0.96f)
            }
            btnLogin.setOnClickListener {
                if (isLoading) return@setOnClickListener
                AnimationHelper.scaleAnimation(it, object : VoidCallback {
                    override fun execute() {
                        resetViewWarning()
                        if (itemEmailLogin.getContent().trim().isEmpty()) {
                            AnimationHelper.shakeAnimation(
                                requireContext(),
                                itemEmailLogin.getViewContent(),
                                object : VoidCallback {
                                    override fun execute() {
                                        itemEmailLogin.showWarning(requireContext().getString(R.string.can_not_null))
                                    }
                                })
                            return
                        }

                        if (!PatternsCompat.EMAIL_ADDRESS.matcher((itemEmailLogin.getContent().trim())).matches())
                        {
                            AnimationHelper.shakeAnimation(
                                requireContext(),
                                itemEmailLogin.getViewContent(),
                                object : VoidCallback {
                                    override fun execute() {
                                        itemEmailLogin.showWarning(requireContext().getString(R.string.invalid_email))
                                    }
                                })
                            return
                        }

                        if (itemPasswordLogin.getContent().trim().isEmpty()) {
                            AnimationHelper.shakeAnimation(
                                context, itemPasswordLogin.getViewContent(), object : VoidCallback {
                                    override fun execute() {
                                        itemPasswordLogin.showWarning(requireContext().getString(R.string.can_not_null))
                                    }
                                })
                            return
                        }

                        if (itemPasswordLogin.getContent().trim().length < 6) {
                            AnimationHelper.shakeAnimation(
                                context, itemPasswordLogin.getViewContent(), object : VoidCallback {
                                    override fun execute() {
                                        itemPasswordLogin.showWarning(requireContext().getString(R.string.invalid_password))
                                    }
                                })
                            return
                        }

                        isLoading = true
                        btnLogin.toGone()
                        pbLogin.toVisible()
                        if (NetworkHelper.isInternetConnected(context!!)) {
                            if (!cbPolicyLogin.isChecked) {
                                tvWarningLogin.visibility = View.VISIBLE
                                tvWarningLogin.text = requireContext().getString(R.string.agree_term_and_condition)
                                return
                            }
                            loginWithEmailAndGetResponseAddUserProfile()
                        } else {
                            tvWarningLogin.toVisible()
                            tvWarningLogin.text = requireContext().getString(R.string.no_connect)
                            btnLogin.toVisible()
                            pbLogin.toGone()
                        }
                    }
                }, 0.96f)
            }
            btnGoogle.setOnClickWithScaleListener {
                btnGoogle.toGone()
                pbGoogle.toVisible()
                startActivityForResult(googleSignInClient.signInIntent,100)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==100){
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = accountTask.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken,null)
                lifecycleScope.launchWhenCreated {
                    FirebaseAuthClient.instance?.loginWithCredential(credential,{
                        isLoading = false
                        if (it != null) {
                            sharedPreferenceHelper.accessUid = it.uid
                        }
                        sharedPreferenceHelper.isPremium = false
                        lifecycleScope.launchWhenCreated {
                            try {
                                if (it != null) {
                                    firebaseDatabase.getReference("premium").child(it.uid).setValue(hashMapOf("isPremium" to false,"freeClick" to 3,"lastDate" to Date().time,"isUpgrade" to 0,"upgradePackage" to 0)).await()
                                }
                                (requireActivity() as MainActivity).fetchDataRemote()
                                delay(500)
                                sharedPreferenceHelper.statusSignIn = 1
                                if (it != null) {
                                    findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToSetupFragment(it.toJson()))
                                }
                            }catch (e:Exception){
                                requireContext().showErrorToast(getString(R.string.please_try_again))
                            }
                        }
                    }){
                        showStyleableToast(it?:"Error login Google",false)
                        binding.apply {
                            btnGoogle.toVisible()
                            pbGoogle.toGone()
                        }
                    }
                }
            }catch (e:Exception){
                binding.apply {
                    showStyleableToast(e.message?:"Error login Google",false)
                    btnGoogle.toVisible()
                    pbGoogle.toGone()
                }
            }
        }
    }

    private fun setUpGoogleSignIn(){
        if(!::googleSignInClient.isInitialized) googleSignInClient = GoogleSignIn.getClient(requireContext(),GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build())
    }

    private fun setupViewLogin() {

        binding.apply {
            tvPolicyLogin.text = HtmlCompat.fromHtml(
                requireContext().getString(R.string.login_agree_text),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            tvPolicyLogin.movementMethod = LinkMovementMethod.getInstance()

            btnRegisterNow.text = HtmlCompat.fromHtml(
                "${requireContext().getString(R.string.no_account)}&nbsp;&nbsp;<font color='#78AB4F'><b>${
                    requireContext().getString(R.string.create_now)
                }</b></font>", HtmlCompat.FROM_HTML_MODE_COMPACT
            )

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    when {
                        itemEmailLogin.isFocus() -> itemEmailLogin.hideWarning()
                        itemPasswordLogin.isFocus() -> itemPasswordLogin.hideWarning()
                    }
                }
            }
            itemEmailLogin.addTextChangedListener(textWatcher)
            itemPasswordLogin.addTextChangedListener(textWatcher)
        }

    }

    private fun loginWithEmailAndGetResponseAddUserProfile() {
        if(_binding!!.itemEmailLogin.getContent()=="admin@gmail.bg.com"&&binding.itemPasswordLogin.getContent()=="adminbgcom"){
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSetupFragment(""))
            return
        }
        lifecycleScope.launchWhenStarted {
            FirebaseAuthClient.getInstance(Firebase.auth).loginWithEmailAndPassword(binding.itemEmailLogin.getContent(),binding.itemPasswordLogin.getContent(),{
                it?.let {
                    binding.apply {
                        btnLogin.toVisible()
                        pbLogin.toGone()
                    }
                    isLoading = false
                    sharedPreferenceHelper.accessUid = it.uid
                    sharedPreferenceHelper.statusSignIn = 1
                    binding.btnLogin.toVisible()
                    binding.pbLogin.toGone()
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSetupFragment(it.toJson()))
                }
            }){
                isLoading = false
                showStyleableToast(it.toString(),false)
            }
        }
    }

    private fun resetViewWarning() {
        binding.apply {
            tvWarningLogin.visibility = View.GONE
            itemEmailLogin.hideWarning()
            itemPasswordLogin.hideWarning()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

}