package com.devapp.runningapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.FragmentPremiumBinding
import com.devapp.runningapp.ui.widgets.PremiumPackageItemView
import com.devapp.runningapp.util.AppHelper
import com.devapp.runningapp.util.AppHelper.showStyleableToast
import com.devapp.runningapp.util.VoidCallback

class PremiumFragment : Fragment(){
    private var _binding:FragmentPremiumBinding?=null
    private val binding get() = _binding!!
    private var hasInitRootView = false
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
                            AppHelper.showDialogPayment(activity,true)
                            requireContext().showStyleableToast("Wait for us to confirm your order",true)
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
                            AppHelper.showDialogPayment(activity,false)
                            requireContext().showStyleableToast("Wait for us to confirm your order",true)
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