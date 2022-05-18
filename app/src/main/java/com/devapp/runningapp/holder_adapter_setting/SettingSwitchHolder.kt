package com.devapp.runningapp.holder_adapter_setting

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.ItemSettingSwitchBinding
import com.devapp.runningapp.model.SettingTypes
import com.devapp.runningapp.util.SettingListener

class SettingSwitchHolder(
    private val binding: ItemSettingSwitchBinding,
    private val moreListener: SettingListener?
) :
    RecyclerView.ViewHolder(binding.root) {

    private var type: SettingTypes? = null

    init {
        binding.scCheck.setOnCheckedChangeListener { _, isChecked ->
            type?.let {
                moreListener?.itemSwitchExecute(it, isChecked)
            }
        }
    }

    fun setData(context: Context, type: SettingTypes, isOn: Boolean) {
        this.type = type
        when (type) {
            SettingTypes.THEME_APP -> {
                binding.tvTitle.text = context.getString(R.string.dark_mode)
                binding.scCheck.isChecked = isOn
            }
            else -> {
                binding.tvTitle.text = ""
                binding.scCheck.isChecked = false
            }
        }
    }

}