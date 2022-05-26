package com.devapp.runningapp.ui.base

import android.os.Bundle
import com.devapp.runningapp.R
import com.devapp.runningapp.model.EventBusState
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class BaseBSDF: BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        setStyle( STYLE_NORMAL, R.style.AppBottomSheetDialogThemeNormal)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onEventBus(event: EventBusState) {

    }


    fun isSafe(): Boolean {
        return !(this.isRemoving || this.activity == null || this.isDetached || !this.isAdded || this.view == null)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}