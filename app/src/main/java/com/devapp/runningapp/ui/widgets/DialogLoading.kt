package com.devapp.runningapp.ui.widgets

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.devapp.runningapp.R

class DialogLoading {
    companion object{
        private lateinit var progressDialog: Dialog
        fun show(context: Context){
            if(!::progressDialog.isInitialized) progressDialog = Dialog(context)
            //progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            progressDialog.setContentView(R.layout.custom_dialog_progress)
            val progressTv = progressDialog.findViewById(R.id.progress_tv) as TextView
            progressTv.text = context.resources.getString(R.string.loading)
            progressTv.setTextColor(ContextCompat.getColor(context, R.color.colorGray_2))
            progressTv.textSize = 16f
            progressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressDialog.setCancelable(false)
            progressDialog.show()

        }

        fun hide() = progressDialog.cancel()
    }
}