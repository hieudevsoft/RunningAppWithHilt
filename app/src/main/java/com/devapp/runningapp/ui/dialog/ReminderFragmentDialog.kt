package com.devapp.runningapp.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.DialogReminderFragmentBinding
import com.devapp.runningapp.model.EventBusState
import com.devapp.runningapp.ui.widgets.TimePicker
import com.devapp.runningapp.util.AnimationHelper
import com.devapp.runningapp.util.DrawableHelper
import com.devapp.runningapp.util.SharedPreferenceHelper
import com.devapp.runningapp.util.TrackingUtils.toGone
import com.devapp.runningapp.util.TrackingUtils.toVisible
import com.devapp.runningapp.util.VoidCallback
import com.google.common.eventbus.EventBus
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus.getDefault
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ReminderFragmentDialog : AppCompatDialogFragment() {
    private var binding: DialogReminderFragmentBinding? = null
    @Inject
    lateinit var preferenceHelper: SharedPreferenceHelper

    private var dismissListener: VoidCallback? = null
    private var timesReminder = ""
    private var timeHour = 0
    private var timeMinute = 0
    private var isSaved = false

    companion object {
        fun newInstance(dismissListener: VoidCallback?): ReminderFragmentDialog {
            val fragment = ReminderFragmentDialog()
            fragment.setListener(dismissListener)
            return fragment
        }
    }

    private fun setListener(dismissListener: VoidCallback?) {
        this.dismissListener = dismissListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.top_top_dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogReminderFragmentBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (context == null) {
            dismiss()
            return
        }

        initUI(requireContext())
    }

    private fun initUI(context: Context) {

        binding?.apply {
            cardTime.background = DrawableHelper.rectangle(context, R.color.colorPrimary, 20f)
            btnSave.background = DrawableHelper.rectangle(context, R.color.white, 30f)

            var currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            if (preferenceHelper.timeReminder.isEmpty()) {
                if (currentTime.isEmpty())
                    currentTime = "00:00"
                timesReminder = currentTime
            } else timesReminder = preferenceHelper.timeReminder

            try {
                val a: Array<String> = timesReminder.split(":".toRegex()).toTypedArray()
                timeHour = a[0].toInt()
                timeMinute = a[1].toInt()
            } catch (e: NumberFormatException) {
                timeHour = 0
                timeMinute = 0
            }

            scTime.isChecked = preferenceHelper.isNotifyReminder
            if(preferenceHelper.isNotifyReminder){
                layoutCardTime.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorPrimary
                    )
                )
                bgCancel.toGone()
            }else {
                layoutCardTime.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorGray
                    )
                )
                bgCancel.toVisible()
            }
            bgCancel.visibility = if (preferenceHelper.isNotifyReminder) View.GONE else View.VISIBLE
            tvTime.text = timesReminder
            timePicker.currentHour = timeHour
            timePicker.currentMinute = timeMinute
            timePicker.setIs24HourView(true)
            timePicker.setOnTimeChangedListener(object :TimePicker.OnTimeChangedListener {
                override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {
                    timeHour = view?.currentHour ?: 0
                    timeMinute = view?.currentMinute ?: 0
                    timesReminder = "${if (timeHour < 10) "0$timeHour" else "$timeHour"}:${if (timeMinute < 10) "0$timeMinute" else "$timeMinute"}"
                    tvTime.text = timesReminder
                }

            })

            scTime.apply {
                setOnCheckedChangeListener { _, isChecked ->
                    if (!isChecked) {
                        layoutCardTime.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGray))
                        bgCancel.toVisible()
                    } else {
                        layoutCardTime.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        bgCancel.toGone()
                    }
                }
                setTrackResource(if (preferenceHelper.isNightMode) R.drawable.track_selecter_day else R.drawable.track_selecter_night)
            }

            bgCancel.setOnClickListener { }

            btnSave.setOnClickListener {
                AnimationHelper.scaleAnimation(it, object : VoidCallback {
                    override fun execute() {
                        if (!isSaved) {
                            isSaved = true
                            preferenceHelper.isNotifyReminder = scTime.isChecked
                            preferenceHelper.titleNotify = context.getString(R.string.content_notify)
                            preferenceHelper.contentNotify = context.getString(R.string.content_study_reminder)
                            preferenceHelper.timeReminder = timesReminder
                            getDefault().post(EventBusState.NOTIFICATION_REMINDER)
                            dismiss()
                        }
                    }
                }, 0.96f)
            }
        }
    }

    override fun onDestroy() {
        dismissListener?.execute()
        super.onDestroy()
    }

}