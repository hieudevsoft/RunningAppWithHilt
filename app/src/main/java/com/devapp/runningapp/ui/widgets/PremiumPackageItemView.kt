package com.devapp.runningapp.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.ItemViewPremiumPackageBinding
import com.devapp.runningapp.util.*
import com.devapp.runningapp.util.TrackingUtils.toGone
import com.devapp.runningapp.util.TrackingUtils.toVisible

class PremiumPackageItemView(context: Context, attrs: AttributeSet): FrameLayout(context,attrs) {
    private val binding = ItemViewPremiumPackageBinding.inflate(LayoutInflater.from(context),this,true)
    var purchaseClickListener: PurchaseClickListener? = null
    var percentSale: Int = 0
    init {
        binding.apply {
            viewChoose.background = DrawableHelper.rectangle(context,R.color.colorGray_11, R.color.colorPrimary, 1f, 12f)
            viewChooseChild.background = DrawableHelper.rectangle(context, R.color.colorPrimary, 2f, 12f)
            viewPurchase.setOnClickListener {
                AnimationHelper.scaleAnimation(itemView, object : VoidCallback {
                    override fun execute() {
                        purchaseClickListener?.execute(skuType, percentSale)
                    }
                }, 0.96f)
            }
        }
    }

    var isSelect = false
        set(value) {
            field = value
            binding.viewChoose.visibility = if (value) View.VISIBLE else View.GONE
        }

    fun updateView(percentSale:Int,originalPrice:Long) {
        binding.tvPercent.text = "$percentSale%"
        binding.tvPrice.text = "${AppHelper.addDotPrice(originalPrice)} ₫"
        this.percentSale = percentSale

        if (percentSale == 0) {
            binding.apply {
                viewPercent.toGone()
                tvPercent.toGone()
                viewPriceOrigin.toGone()
            }
        } else {
            binding.apply {
                viewPercent.toVisible()
                tvPercent.toVisible()
                viewPriceOrigin.toVisible()

                when (skuType) {
                    "every" -> {
                        viewPercent.setImageResource(R.drawable.img_percent_sale_best_choice)
                    }
                    else -> {
                        viewPercent.setImageResource(R.drawable.img_percent_sale)
                    }
                }
                "$percentSale%".let { tvPercent.text = it }

                var priceOrigin = originalPrice * 100 / (100 - percentSale)
                if (priceOrigin / 1000000 > 9999)
                    priceOrigin = (priceOrigin / 1000000000) * 1000
                else if (priceOrigin / 1000000 > 999)
                    priceOrigin /= 1000000

                tvPriceOrigin.text = "${AppHelper.addDotPrice(priceOrigin)} ₫"
            }
        }
    }

    var skuType = ""
        set(value) {
            field = value
            when (value) {
                "every" -> {
                    binding.tvPackageName.text = "Lifetime"
                }
                "6months" -> {
                    binding.tvPackageName.text = "6months"
                }
            }
        }

    interface PurchaseClickListener {
        fun execute(skuType: String, percentSale: Int)
    }

}