package com.devapp.runningapp.ui.base

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

open class BaseFullScreenBSDF : BaseBSDF() {
    var isDraggable = true
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val bottomSheet = dialog as BottomSheetDialog
        bottomSheet.setOnShowListener {
            val frame = bottomSheet.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet) ?: return@setOnShowListener
            val behavior = BottomSheetBehavior.from(frame)
            frame.layoutParams.height = resources.displayMetrics.heightPixels
            frame.requestLayout()
            behavior?.peekHeight = resources.displayMetrics.heightPixels
            behavior.isHideable = true
            behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(p0: View, p1: Float) {
                }

                override fun onStateChanged(p0: View, p1: Int) {
                    if (p1 == BottomSheetBehavior.STATE_DRAGGING && !isDraggable) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    if (p1 == BottomSheetBehavior.STATE_COLLAPSED) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    if (p1 == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss()
                    }
                }
            })
            behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }
}