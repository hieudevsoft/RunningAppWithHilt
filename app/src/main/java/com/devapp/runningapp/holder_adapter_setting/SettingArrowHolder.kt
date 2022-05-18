package com.devapp.runningapp.holder_adapter_setting

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.ItemSettingArrowBinding
import com.devapp.runningapp.model.SettingTypes
import com.devapp.runningapp.util.AnimationHelper
import com.devapp.runningapp.util.SettingListener
import com.devapp.runningapp.util.VoidCallback

class SettingArrowHolder(
    private val binding: ItemSettingArrowBinding,
    private val moreListener: SettingListener?
) : RecyclerView.ViewHolder(binding.root) {

    private var type: SettingTypes? = null

    init {
        binding.itemView.setOnClickListener {
            type?.let { type ->
                AnimationHelper.scaleAnimation(it, object : VoidCallback {
                    override fun execute() {
                        moreListener?.itemClickListener(type)
                    }
                }, 0.96f)
            }
        }
    }

    fun setData(context: Context, type: SettingTypes) {
        this.type = type
        when (type) {
            SettingTypes.REMINDER -> binding.tvTitle.text = context.getString(R.string.reminder)
            SettingTypes.POLICY -> binding.tvTitle.text = context.getString(R.string.policy)
            SettingTypes.FEEDBACK -> binding.tvTitle.text = context.getString(R.string.feedback)
            SettingTypes.TELL_FRIEND -> binding.tvTitle.text = context.getString(R.string.tell_friend)
            SettingTypes.RATE -> binding.tvTitle.text = context.getString(R.string.rate)
            else -> binding.tvTitle.text = ""
        }
    }

}