package com.devapp.runningapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.ItemRunBinding
import com.devapp.runningapp.model.Run
import com.devapp.runningapp.ui.dialog.bsdf.ViewPhotosBSDF
import java.text.SimpleDateFormat
import java.util.*


class RunAdapter(private val fm:FragmentManager):RecyclerView.Adapter<RunAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemRunBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(run: Run){
            binding.run = run
            binding.executePendingBindings()
        }
    }

    val diffUtilCallBack = object: DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    val differ = AsyncListDiffer(this,diffUtilCallBack)
    fun submitList(list:List<Run>){
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemRunBinding>(LayoutInflater.from(parent.context), R.layout.item_run,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.bind(item)
        holder.binding.cardIvRun.setOnClickListener {
            ViewPhotosBSDF.show(item.img,fm)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}