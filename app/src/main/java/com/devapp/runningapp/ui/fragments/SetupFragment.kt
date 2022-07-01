package com.devapp.runningapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devapp.runningapp.R
import com.devapp.runningapp.adapters.MemberUpgradePremiumAdapter
import com.devapp.runningapp.databinding.FragmentSetupBinding
import com.devapp.runningapp.model.MemberUpgradeModel
import com.devapp.runningapp.model.ResourceNetwork
import com.devapp.runningapp.model.user.UserProfile
import com.devapp.runningapp.ui.viewmodels.FirebaseViewModel
import com.devapp.runningapp.ui.widgets.DialogLoading
import com.devapp.runningapp.util.*
import com.devapp.runningapp.util.AppHelper.fromJson
import com.devapp.runningapp.util.AppHelper.showErrorToast
import com.devapp.runningapp.util.AppHelper.showStyleableToast
import com.devapp.runningapp.util.AppHelper.showToastNotConnectInternet
import com.devapp.runningapp.util.AppHelper.toJson
import com.devapp.runningapp.util.TrackingUtils.toGone
import com.devapp.runningapp.util.TrackingUtils.toVisible
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {
    private val prefs: SharedPreferenceHelper by lazy { SharedPreferenceHelper(requireContext()) }
    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!
    private var hasInitRootView = false
    private val args: SetupFragmentArgs by navArgs()
    private lateinit var currentUserProfile:UserProfile
    private val fireBaseViewModel: FirebaseViewModel by activityViewModels()
    private val firebaseDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance(Constant.URL_FIREBASE_DB) }
    private lateinit var memberUpgradePremiumAdapter:MemberUpgradePremiumAdapter
    private var isShowDialog = false
    private var listCurrentMember = mutableListOf<MemberUpgradeModel>()
    private var isSearch = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if (_binding == null) _binding =
                FragmentSetupBinding.inflate(inflater, container, false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        } catch (e: Exception) {

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasInitRootView) return
        hasInitRootView = true
        if(args.user.isEmpty()){
            handleAdmin()
        }
         else {
             binding.lyAdmin.toGone()
             binding.lyMember.toVisible()
             handleViewModel()
        }
    }

    private fun handleAdmin() {
        if(!::memberUpgradePremiumAdapter.isInitialized) memberUpgradePremiumAdapter = MemberUpgradePremiumAdapter({
            try {
                lifecycle.coroutineScope.launchWhenResumed {
                    firebaseDatabase.getReference("premium").child(it.uid).setValue(hashMapOf("freeClick" to it.freeClick,"isPremium" to true, "isUpgrade" to 2,"lastDate" to it.lastDate,"upgradePackage" to it.upgradePackage,"premiumExpired" to 0)).await()
                    requireContext().showStyleableToast("Confirm successfully for\n ${it.uid}",true)
                }
            }catch (e:Exception){
                requireContext().showErrorToast(e.message?:"Error not known")
            }
        }){
            try {
                lifecycle.coroutineScope.launchWhenResumed {
                    firebaseDatabase.getReference("premium").child(it.uid).setValue(hashMapOf("freeClick" to it.freeClick,"isPremium" to false, "isUpgrade" to 3,"lastDate" to it.lastDate,"upgradePackage" to it.upgradePackage,"premiumExpired" to it.lastDate+1000*60*60*24*6*30)).await()
                    requireContext().showStyleableToast("Cancel successfully for\n ${it.uid}",true)
                }
            }catch (e:Exception){
                requireContext().showErrorToast(e.message?:"Error not known")
            }
        }.also { it.setOnItemClickListener {
            fireBaseViewModel.getStateFlowUserProfile(it.uid)
            lifecycleScope.launchWhenResumed {
                fireBaseViewModel.stateFlowGetUserProfile.collect(){
                    when(it){
                        is ResourceNetwork.Success->{
                            if(it.data==null) {
                                requireContext().showErrorToast("User is hacker")
                                return@collect
                            }
                            if(isShowDialog) return@collect
                            isShowDialog = true
                            AppHelper.showDialogTransfer(requireActivity(),object:VoidCallback{
                                override fun execute() {
                                    isShowDialog = false
                                }
                            },it.data!!.email+"\n"+it.data!!.userName+"\n"+it.data!!.dob+"\n"+it.data!!.phoneNumber?.ifEmpty{"No phone number"}+"\n"+it.data!!.gender)
                        }

                        is ResourceNetwork.Error->{
                            requireContext().showToastNotConnectInternet()
                        }

                        else->{

                        }
                    }
                }
            }
        } }
        binding.rcListPremium.apply {
            adapter = memberUpgradePremiumAdapter
        }

        binding.searchView.setOnFocusChangeListener { _, b -> isSearch = b }
        binding.searchView.setOnQueryTextFocusChangeListener { _, b -> isSearch = b }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText.isNullOrEmpty()) {
                    getDataRealTime()
                    isSearch = false
                }
                else{
                    isSearch = true
                    memberUpgradePremiumAdapter.submitList(memberUpgradePremiumAdapter.getCurrentList().filter { it.uid.contains(newText)||SimpleDateFormat("dd-MM-YYYY HH:mm:ss").format(Date(it.lastDate)).contains(newText)})
                }
                return true
            }

        })
        getDataRealTime()
    }

    fun getDataRealTime(){
        firebaseDatabase.getReference("premium").addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("TAG", "onDataChange: $isSearch")
                if(!isSearch){
                    val listPremiumPackage = mutableListOf<MemberUpgradeModel>()
                    snapshot.children.forEach {
                        val data = it.value as HashMap<*,*>
                        val memberUpgradeModel = MemberUpgradeModel(it.key?:"",data["isUpgrade"] as Long,data["freeClick"] as Long,data["isPremium"] as Boolean,data["lastDate"] as Long,data["upgradePackage"] as Long)
                        listPremiumPackage.add(memberUpgradeModel)
                    }
                    memberUpgradePremiumAdapter.submitList(listPremiumPackage.toList().filter { it.isUpgrade==1L })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                requireContext().showErrorToast(error.message)
            }

        })
    }

    private fun handleViewModel() {
        fireBaseViewModel.getStateFlowUserProfile(prefs.accessUid!!)
        lifecycle.coroutineScope.launchWhenStarted {
            fireBaseViewModel.stateFlowGetUserProfile.collect() {
                when (it){
                    is ResourceNetwork.Loading->{
                        DialogLoading.show(requireContext())
                    }

                    is ResourceNetwork.Success->{
                        if(it.data==null) {
                            requireContext().showStyleableToast("Data is empty :(",false)
                            return@collect
                        }
                        currentUserProfile = it.data!!
                        prefs.userProfile = it.data!!.toJson()
                        initView()
                        DialogLoading.hide()
                    }
                    is ResourceNetwork.Error->{
                        if(prefs.userProfile!=null||prefs.userProfile!!.isNotEmpty()) currentUserProfile = prefs.userProfile!!.fromJson(UserProfile::class.java)
                        DialogLoading.hide()
                        initView()
                        requireContext().showStyleableToast(getString(R.string.please_try_again),false)
                    }
                    else->{}
                }
            }
        }
        lifecycle.coroutineScope.launchWhenStarted {
            fireBaseViewModel.stateFlowUpdateUserProfile.collect() {
                when (it){
                    is ResourceNetwork.Loading->{
                        DialogLoading.show(requireContext())
                    }

                    is ResourceNetwork.Success->{
                        if(it.data==null) {
                            requireContext().showStyleableToast("Error Opps :(",false)
                            return@collect
                        }
                        DialogLoading.hide()
                        prefs.userProfile = it.data!!.toJson()
                        prefs.weightUser = it.data!!.weight.toString()
                        prefs.userProfile = it.data!!.toJson()
                        delay(1000)
                        findNavController().navigate(SetupFragmentDirections.actionSetupFragmentToRunFragment())
                    }
                    is ResourceNetwork.Error->{
                        DialogLoading.hide()
                        requireContext().showStyleableToast(getString(R.string.please_try_again),false)
                    }
                    else->{}
                }
            }
        }
    }

    private fun initView() {
        val user = args.user.fromJson<UserProfile>(UserProfile::class.java)
        binding.apply {
            currentUserProfile.nickName.let {
                if (it != null) {
                    etName.setContent(it)
                } else currentUserProfile.userName?.let { etName.setContent(it) }?:"Runner"
            }
            currentUserProfile.weight.let {
                if (it != null) {
                    etWeight.setContent(it)
                } else user.weight?.let { etWeight.setContent(it) }
            }
            tvWelcome.text = String.format(getString(R.string.welcome), etName.getContent().ifEmpty { "Runner" })
            btnContinue.setOnClickListener {
                AnimationHelper.scaleAnimation(it, object : VoidCallback {
                    override fun execute() {
                        if (checkInputInformation()) {
                            if(!NetworkHelper.isInternetConnected(requireContext())){
                                requireContext().showStyleableToast("Mode offline enabled",true)
                                return
                            }
                            if(currentUserProfile.nickName==binding.etName.getContent().trim()&&currentUserProfile.weight==etWeight.getContent().trim()){
                                findNavController().navigate(SetupFragmentDirections.actionSetupFragmentToRunFragment())
                                return
                            }
                            currentUserProfile.nickName = binding.etName.getContent()
                            currentUserProfile.weight = binding.etWeight.getContent()
                            fireBaseViewModel.getStateUpdateUserProfile(currentUserProfile)
                        } else {
                            showStyleableToast("Please fill the fields", false)
                        }
                    }
                }, 0.96f)
            }
        }
    }

    private fun checkInputInformation(): Boolean {
        val name = binding.etName.getContent()
        val weight = binding.etWeight.getContent()
        try {
            if (name.isEmpty() || weight.isEmpty()) return false
            weight.toFloat()
        } catch (e: Exception) {
            showStyleableToast("Weight format is not valid", false)
            return false
        }
        return true
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}