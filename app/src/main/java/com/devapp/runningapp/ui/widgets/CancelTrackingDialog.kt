package com.devapp.runningapp.ui.widgets

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.devapp.runningapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog:DialogFragment() {

    private var yesListener:(()->Unit)?=null

    fun setYesListener(listener:(()->Unit)){
        this.yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel Tracking?")
            .setMessage("Are you sure to cancel the current tracking and delete all data ?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("YES") { _, _ ->
                yesListener?.let {
                    it()
                }
            }
            .setNegativeButton("NO") { dialogInterface, _ ->
                dialogInterface.dismiss()
                dialogInterface.cancel()
            }.create()
    }
}