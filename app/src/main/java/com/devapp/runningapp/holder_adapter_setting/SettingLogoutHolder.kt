package com.devapp.runningapp.holder_adapter_setting

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.ItemSettingLogoutBinding
import com.devapp.runningapp.util.AnimationHelper
import com.devapp.runningapp.util.DrawableHelper
import com.devapp.runningapp.util.SettingListener
import com.devapp.runningapp.util.VoidCallback

class SettingLogOutHolder(
    private val binding: ItemSettingLogoutBinding, private val moreListener: SettingListener?
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.btnLogOut.setOnClickListener {
            AnimationHelper.scaleAnimation(it, object : VoidCallback {
                override fun execute() {
                    moreListener?.logOutListener()
                }
            }, 0.96f)
        }
    }

    fun setData(context: Context, isNightMode: Boolean) {
        binding.btnLogOut.background = if (isNightMode) DrawableHelper.rectangle(context, R.color.colorGray_3, 30f)
            else DrawableHelper.rectangle(context, R.color.white, 30f)
    }

}