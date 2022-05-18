package com.devapp.runningapp.holder_adapter_setting

import androidx.recyclerview.widget.RecyclerView
import com.devapp.runningapp.BuildConfig
import com.devapp.runningapp.databinding.ItemSettingVersionAppBinding

class MoreVersionAppHolder(private val binding: ItemSettingVersionAppBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun setData() {
        "v ${BuildConfig.VERSION_NAME}".apply { binding.tvVersion.text = this }
    }

}