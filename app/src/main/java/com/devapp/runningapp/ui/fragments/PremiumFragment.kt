package com.devapp.runningapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.coroutineScope
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentPremiumBinding
import com.devapp.runningapp.ui.widgets.PremiumPackageItemView
import com.devapp.runningapp.util.AppHelper
import com.devapp.runningapp.util.AppHelper.showStyleableToast
import com.devapp.runningapp.util.Constant
import com.devapp.runningapp.util.SharedPreferenceHelper
import com.devapp.runningapp.util.VoidCallback
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class PremiumFragment : Fragment(){
    private var _binding:FragmentPremiumBinding?=null
    private val binding get() = _binding!!
    private var hasInitRootView = false
    private val firebaseDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance(Constant.URL_FIREBASE_DB) }
    private val sharedPref:SharedPreferenceHelper by lazy { SharedPreferenceHelper(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            if(_binding==null) _binding = FragmentPremiumBinding.inflate(layoutInflater,container,false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        }catch (e:Exception){

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(hasInitRootView) return
        hasInitRootView = true
        initUi()
    }

    private fun initUi() {
        binding.apply {
            itemForever.skuType = "every"
            itemForever.isSelect = true
            itemForever.updateView(30,1000000)
            itemForever.purchaseClickListener = object:PremiumPackageItemView.PurchaseClickListener{
                override fun execute(skuType: String, percentSale: Int) {
                    itemForever.isSelect = true
                    item6months.isSelect = false
                    AppHelper.showDialogTransfer(requireActivity(),object:VoidCallback{
                        override fun execute() {
                            requireContext().showStyleableToast("Wait for us to confirm your order",true)
                            lifecycle.coroutineScope.launchWhenResumed {
                                firebaseDatabase.getReference("premium").child(sharedPref.accessUid?:"").setValue(hashMapOf("freeClick" to (sharedPref.freeClick),"isPremium" to sharedPref.isPremium, "isUpgrade" to 1,"lastDate" to sharedPref.lastDate,"upgradePackage" to 1)).await()
                            }
                        }
                    })
                }
            }
            item6months.skuType = "every"
            item6months.isSelect = false
            item6months.updateView(70,600000)
            item6months.purchaseClickListener = object:PremiumPackageItemView.PurchaseClickListener{
                override fun execute(skuType: String, percentSale: Int) {
                    item6months.isSelect = true
                    itemForever.isSelect = false
                    AppHelper.showDialogTransfer(requireActivity(),object:VoidCallback{
                        override fun execute() {
                            requireContext().showStyleableToast("Wait for us to confirm your order",true)
                            lifecycle.coroutineScope.launchWhenResumed {
                                firebaseDatabase.getReference("premium").child(sharedPref.accessUid?:"").setValue(hashMapOf("freeClick" to (sharedPref.freeClick),"isPremium" to sharedPref.isPremium, "isUpgrade" to 1,"lastDate" to sharedPref.lastDate,"upgradePackage" to 6)).await()
                            }
                        }
                    })
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
}