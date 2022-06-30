package com.devapp.runningapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.devapp.runningapp.databinding.ItemMemberUpgradeBinding
import com.devapp.runningapp.model.MemberUpgradeModel
import com.devapp.runningapp.util.AppHelper.setOnClickWithScaleListener
import java.text.SimpleDateFormat
import java.util.*


class MemberUpgradePremiumAdapter(private val onConfirmClickListener:(MemberUpgradeModel)->Unit,private val onCancelClickListener:(MemberUpgradeModel)->Unit): RecyclerView.Adapter<MemberUpgradePremiumAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMemberUpgradeBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item: MemberUpgradeModel){
            binding.apply {
                tvUid.text = "UID: ${item.uid} "
                tvPackageName.text = "Premium Package: ${if(item.upgradePackage==6L) "6 months" else if(item.isUpgrade==1L) "Lifetime" else "Undefine"} "
                tvTime.text = "Time: ${SimpleDateFormat("dd-MM-YYYY HH:mm:ss").format(Date(item.lastDate))} "
            }
        }
    }

    private val diffUtilItemCallBack = object:DiffUtil.ItemCallback<MemberUpgradeModel>(){

        override fun areItemsTheSame(oldItem: MemberUpgradeModel, newItem: MemberUpgradeModel): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: MemberUpgradeModel, newItem: MemberUpgradeModel): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    private val differ = AsyncListDiffer(this,diffUtilItemCallBack)
    fun getCurrentList() = differ.currentList


    fun submitList(list:List<MemberUpgradeModel>){
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMemberUpgradeBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    fun getItemAtPosition(position:Int) = differ.currentList[position]

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItemAtPosition(position)
        holder.bind(item)
        holder.binding.root.setOnClickListener {
            onClickItemListener?.let { it(item) }
        }
        holder.binding.btnConfirm.setOnClickWithScaleListener {
            onConfirmClickListener(item)
        }
        holder.binding.btnCancel.setOnClickWithScaleListener {
            onCancelClickListener(item)
        }
    }

    private var onClickItemListener:((MemberUpgradeModel)->Unit)?=null

    fun setOnItemClickListener(listener:(MemberUpgradeModel)->Unit){
        onClickItemListener= listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}