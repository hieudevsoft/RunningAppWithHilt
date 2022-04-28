package com.devapp.runningapp.ui.widgets

import android.content.Context
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.RelativeLayout
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.ViewDatePickerBinding
import com.devapp.runningapp.util.AnimationHelper
import com.devapp.runningapp.util.DateCallback
import com.devapp.runningapp.util.DrawableHelper
import com.devapp.runningapp.util.TrackingUtils.toGone
import com.devapp.runningapp.util.TrackingUtils.toVisible
import com.devapp.runningapp.util.VoidCallback
import java.util.*

class DatePickerView(context: Context) : RelativeLayout(context) {
    private val binding = ViewDatePickerBinding.inflate(LayoutInflater.from(context), this, true)
    private var day = 0
    private var month = 0
    private var year = 0
    private var dateCallback: DateCallback? = null
    private var dismissListener: VoidCallback? = null

    constructor(
        context: Context, day: Int, month: Int, year: Int,
        dateCallback: DateCallback?, dismissListener: VoidCallback?
    ) : this(context) {
        this.day = day
        this.month = month
        this.year = year
        this.dateCallback = dateCallback
        this.dismissListener = dismissListener

        setupData()
    }

    init {
        binding.apply {
            cardPickerDate.background = DrawableHelper.rectangle(
                context, R.color.colorBackgroundDay,
                floatArrayOf(44f, 44f, 44f, 44f, 0f, 0f, 0f, 0f)
            )
            btnOk.background = DrawableHelper.rectangle(context, R.color.colorPrimary, 30f)

            viewBackground.setOnClickListener {
                handleDismiss()
            }

            cardPickerDate.setOnClickListener { }

            btnClose.setOnClickListener {
                AnimationHelper.scaleAnimation(
                    it, object : VoidCallback {
                        override fun execute() {
                            handleDismiss()
                        }
                    },
                    0.96f
                )
            }
            btnOk.setOnClickListener {
                AnimationHelper.scaleAnimation(it, object : VoidCallback {
                    override fun execute() {
                        dateCallback?.execute(day, month, year)
                        handleDismiss()
                    }
                }, 0.96f)
            }
        }
    }

    private fun setupData() {
        val calendar = Calendar.getInstance()
        if (day == 0) day = calendar[Calendar.DAY_OF_MONTH]
        if (month == 0) {
            month = calendar[Calendar.MONTH]
            month = if (month == 12) 1 else month + 1
        }
        if (year == 0) year = calendar[Calendar.YEAR]

        binding.datePicker.init(year, month - 1, day)
        { _: DatePicker?, yearDate: Int, monthOfYear: Int, dayOfMonth: Int ->
            day = dayOfMonth
            month = if (monthOfYear == 12) 1 else monthOfYear + 1

            year = yearDate
        }
    }

    fun animateView() {
        binding.cardPickerDate.toVisible()
        AnimationHelper.bottomIn(binding.cardPickerDate, null, 300)
    }

    private fun handleDismiss() {
        AnimationHelper.bottomOut(binding.cardPickerDate, object : VoidCallback {
            override fun execute() {
                binding.cardPickerDate.toGone()
                dismissListener?.execute()
            }
        }, 300)
    }

}