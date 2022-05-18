package com.devapp.runningapp.holder_adapter_setting

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.ItemSettingTitleBinding
import com.devapp.runningapp.model.SettingTypes

class MoreTitleHolder(private val binding: ItemSettingTitleBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun setData(context: Context, type: SettingTypes) {
        when (type) {
            SettingTypes.TITLE_ACCOUNT -> {
                binding.ivIcon.setImageResource(R.drawable.ic_account)
                binding.tvTitle.text = context.getString(R.string.account)
            }
            SettingTypes.TITLE_OVERVIEW -> {
                binding.ivIcon.setImageResource(R.drawable.ic_overview)
                binding.tvTitle.text = context.getString(R.string.overview)
            }
            SettingTypes.TITLE_SUPPORT -> {
                binding.ivIcon.setImageResource(R.drawable.ic_support)
                binding.tvTitle.text = context.getString(R.string.support)
            }
            else -> {
            }
        }
    }

}