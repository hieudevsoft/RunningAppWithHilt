package com.devapp.runningapp.ui.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.LayoutChooseGenderBinding

class ChooseGenderDialog constructor(
    private val context: Activity,
    private val currentSex: Int,
    private val itemClickListener: (Int) -> Unit
) {
    fun show() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context, R.style.top_top_dialog)
        val mView = context.layoutInflater.inflate(R.layout.layout_choose_gender, null)
        val binding = LayoutChooseGenderBinding.bind(mView)
        builder.setView(mView)
        val dialog = builder.create()
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.apply {
            when(currentSex){
                0->rdFemale.isChecked = true
                1->rdMale.isChecked = true
                else->{}
            }

            binding.rdMale.setOnCheckedChangeListener { _, b ->
                if(!b) return@setOnCheckedChangeListener
                rdFemale.isChecked = false
                itemClickListener.invoke(1)
                Handler(Looper.getMainLooper()).postDelayed({dialog.dismiss()},500)
            }

            binding.rdFemale.setOnCheckedChangeListener { _, b ->
                if(!b) return@setOnCheckedChangeListener
                rdMale.isChecked = false
                itemClickListener.invoke(0)
                Handler(Looper.getMainLooper()).postDelayed({dialog.dismiss()},500)
            }
        }

        dialog.show()
    }
}