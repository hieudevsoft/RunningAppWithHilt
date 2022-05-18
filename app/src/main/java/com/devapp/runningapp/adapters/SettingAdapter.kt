package com.devapp.runningapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.devapp.runningapp.databinding.*
import com.devapp.runningapp.holder_adapter_setting.*
import com.devapp.runningapp.model.SettingTypes
import com.devapp.runningapp.util.SettingListener
import com.devapp.runningapp.util.SharedPreferenceHelper

class SettingAdapter(
    private val preferenceHelper: SharedPreferenceHelper,
    private val settingListener: SettingListener?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {
                MoreTitleHolder(
                    ItemSettingTitleBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
            2 -> {
                SettingDescriptionHolder(
                    ItemSettingDescriptionBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), settingListener
                )
            }
            3 -> {
                SettingSwitchHolder(
                    ItemSettingSwitchBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), settingListener
                )
            }
            4 -> {
                SettingArrowHolder(
                    ItemSettingArrowBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), settingListener
                )
            }
            5 -> {
                SettingLogOutHolder(
                    ItemSettingLogoutBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), settingListener
                )
            }
            else -> {
                MoreVersionAppHolder(
                    ItemSettingVersionAppBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position >= itemCount) return

        when (holder.itemViewType) {
            0 -> {
                val versionAppHolder = holder as MoreVersionAppHolder
                versionAppHolder.setData()
            }
            1 -> {
                val titleHolder = holder as MoreTitleHolder
                titleHolder.setData(titleHolder.itemView.context, differ.currentList[position])
            }
            2 -> {
                val descriptionHolder = holder as SettingDescriptionHolder
                descriptionHolder.setData(descriptionHolder.itemView.context, differ.currentList[position])
            }
            3 -> {
                val switchHolder = holder as SettingSwitchHolder
                differ.currentList[position].let { type ->
                    when (type) {
                        SettingTypes.THEME_APP -> switchHolder.setData(switchHolder.itemView.context, type, preferenceHelper.isNightMode)
                        else -> {
                        }
                    }
                }
            }
            4 -> {
                val arrowHolder = holder as SettingArrowHolder
                arrowHolder.setData(arrowHolder.itemView.context, differ.currentList[position])
            }
            5 -> {
                val logOutHolder = holder as SettingLogOutHolder
                logOutHolder.setData(logOutHolder.itemView.context, preferenceHelper.isNightMode)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            SettingTypes.VERSION_APP -> 0
            SettingTypes.TITLE_ACCOUNT -> 1
            SettingTypes.LOG_OUT -> 5
            SettingTypes.TITLE_OVERVIEW -> 1
            SettingTypes.LANGUAGE_DEVICE -> 2
            SettingTypes.THEME_APP -> 3
            SettingTypes.REMINDER -> 4
            SettingTypes.POLICY -> 4
            SettingTypes.TITLE_SUPPORT -> 1
            SettingTypes.FEEDBACK -> 4
            SettingTypes.TELL_FRIEND -> 4
            SettingTypes.RATE -> 4
        }
    }

    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<SettingTypes>(){
        override fun areItemsTheSame(oldItem: SettingTypes, newItem: SettingTypes): Boolean {
            return oldItem.toString() == newItem.toString()
        }

        override fun areContentsTheSame(oldItem: SettingTypes, newItem: SettingTypes): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    private val differ = AsyncListDiffer(this,diffUtilItemCallback)

    fun setNewSections(sections: List<SettingTypes>) {
        differ.submitList(sections)
    }

}