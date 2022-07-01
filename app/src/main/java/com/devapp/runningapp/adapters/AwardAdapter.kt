package com.devapp.runningapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.devapp.runningapp.databinding.ItemAwardBinding
import com.devapp.runningapp.model.ItemAward
import com.devapp.runningapp.util.TrackingUtils.toGone
import com.devapp.runningapp.util.TrackingUtils.toVisible


class AwardAdapter : RecyclerView.Adapter<AwardAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAwardBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item: ItemAward){
            binding.apply {
                ivTrophy.setImageResource(item.img)
                tvTrophy.text = item.title
                tvContent.text = item.description
                pbProgress.setProgress(item.progress,true)
                if(item.isLock) viewLock.toVisible() else viewLock.toGone()
            }
        }
    }

    private val diffUtilItemCallBack = object:DiffUtil.ItemCallback<ItemAward>(){

        override fun areItemsTheSame(oldItem: ItemAward, newItem: ItemAward): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: ItemAward, newItem: ItemAward): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    private val differ = AsyncListDiffer(this,diffUtilItemCallBack)

    fun submitList(list:List<ItemAward>){
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAwardBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    private fun getItemAtPosition(position:Int) = differ.currentList[position]

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItemAtPosition(position)
        holder.bind(item)
        holder.binding.root.setOnClickListener {
            onClickItemListener?.let { it(item) }
        }
    }

    private var onClickItemListener:((ItemAward)->Unit)?=null

    fun setOnItemClickListener(listener:(ItemAward)->Unit){
        onClickItemListener= listener
    }

    fun updateItemListener(pos:Int,isLock:Boolean,progress:Float){
        getItemAtPosition(pos).isLock = isLock
        getItemAtPosition(pos).progress = progress.toInt()
        notifyItemChanged(pos)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}