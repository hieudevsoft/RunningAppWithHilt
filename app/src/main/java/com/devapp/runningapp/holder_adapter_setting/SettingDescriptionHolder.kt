package com.devapp.runningapp.holder_adapter_setting

import android.content.Context
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.ItemSettingDescriptionBinding
import com.devapp.runningapp.model.SettingTypes
import com.devapp.runningapp.util.AnimationHelper
import com.devapp.runningapp.util.AppHelper
import com.devapp.runningapp.util.SettingListener
import com.devapp.runningapp.util.VoidCallback

class SettingDescriptionHolder(
    private val binding: ItemSettingDescriptionBinding,
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
            SettingTypes.LANGUAGE_DEVICE -> {
                binding.tvTitle.text = context.getString(R.string.device_language)
                binding.tvDesc.text = HtmlCompat.fromHtml("<u>en</u>",
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
            }
            else -> {}
        }
    }

}