package com.devapp.runningapp.ui.widgets

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.NumberPicker
import com.devapp.runningapp.R
import java.text.DateFormatSymbols
import java.util.*

class TimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    // state
    private var mCurrentHour = 0 // 0-23
    private var mCurrentMinute = 0 // 0-59
    private var mIs24HourView = false
    private var mIsAm: Boolean = false

    // ui components
    private val mHourPicker: NumberPicker
    private val mMinutePicker: NumberPicker
    private val mAmPmButton: Button
    private val mAmText: String
    private val mPmText: String

    // callbacks
    private var mOnTimeChangedListener: OnTimeChangedListener? = null

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    interface OnTimeChangedListener {
        /**
         * @param view      The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute    The current minute.
         */
        fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        mMinutePicker.isEnabled = enabled
        mHourPicker.isEnabled = enabled
        mAmPmButton.isEnabled = enabled
    }

    /**
     * Used to save / restore state of time picker
     */
    private class SavedState : BaseSavedState {
        val hour: Int
        val minute: Int

        constructor(superState: Parcelable?, hour: Int, minute: Int) : super(superState) {
            this.hour = hour
            this.minute = minute
        }

        private constructor(`in`: Parcel) : super(`in`) {
            hour = `in`.readInt()
            minute = `in`.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(hour)
            dest.writeInt(minute)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState?> = object :
                Parcelable.Creator<SavedState?> {
                override fun createFromParcel(`in`: Parcel): SavedState? {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, mCurrentHour, mCurrentMinute)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        currentHour = ss.hour
        currentMinute = ss.minute
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onTimeChangedListener the callback, should not be null.
     */
    fun setOnTimeChangedListener(onTimeChangedListener: OnTimeChangedListener?) {
        mOnTimeChangedListener = onTimeChangedListener
    }
    /**
     * @return The current hour (0-23).
     */
    /**
     * Set the current hour.
     */
    var currentHour: Int
        get() = mCurrentHour
        set(currentHour) {
            mCurrentHour = currentHour
            updateHourDisplay()
        }

    /**
     * Set whether in 24 hour or AM/PM mode.
     *
     * @param is24HourView True = 24 hour mode. False = AM/PM.
     */
    fun setIs24HourView(is24HourView: Boolean) {
        if (mIs24HourView !== is24HourView) {
            mIs24HourView = is24HourView
            configurePickerRanges()
            updateHourDisplay()
        }
    }

    /**
     * @return true if this is in 24 hour view else false.
     */
    fun is24HourView(): Boolean {
        return mIs24HourView
    }
    /**
     * @return The current minute.
     */
    /**
     * Set the current minute (0-59).
     */
    var currentMinute: Int
        get() = mCurrentMinute
        set(currentMinute) {
            mCurrentMinute = currentMinute
            updateMinuteDisplay()
        }

    override fun getBaseline(): Int {
        return mHourPicker.baseline
    }

    /**
     * Set the state of the spinners appropriate to the current hour.
     */
    private fun updateHourDisplay() {
        var currentHour = mCurrentHour
        if (!mIs24HourView) {
            // convert [0,23] ordinal to wall clock display
            if (currentHour > 12) currentHour -= 12 else if (currentHour == 0) currentHour = 12
        }
        mHourPicker.value = currentHour
        mIsAm = mCurrentHour < 12
        mAmPmButton.text = if (mIsAm) mAmText else mPmText
        onTimeChanged()
    }

    private fun configurePickerRanges() {
        if (mIs24HourView) {
            mHourPicker.minValue = 0
            mHourPicker.maxValue = 23
            mHourPicker.setFormatter(TWO_DIGIT_FORMATTER)
            mAmPmButton.visibility = GONE
        } else {
            mHourPicker.minValue = 1
            mHourPicker.maxValue = 12
            mHourPicker.setFormatter(null)
            mAmPmButton.visibility = VISIBLE
        }
    }

    private fun onTimeChanged() {
        mOnTimeChangedListener!!.onTimeChanged(this, currentHour, currentMinute)
    }

    /**
     * Set the state of the spinners appropriate to the current minute.
     */
    private fun updateMinuteDisplay() {
        mMinutePicker.value = mCurrentMinute
        mOnTimeChangedListener!!.onTimeChanged(this, currentHour, currentMinute)
    }

    companion object {
        /**
         * A no-op callback used in the constructor to avoid null checks
         * later in the code.
         */
        private val NO_OP_CHANGE_LISTENER: OnTimeChangedListener = object : OnTimeChangedListener {
            override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {}
        }
        val TWO_DIGIT_FORMATTER =
            NumberPicker.Formatter { value: Int -> String.format("%02d", value) }
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(
            R.layout.widget_time_picker,
            this,  // we are the parent
            true
        )

        // hour
        mHourPicker = findViewById(R.id.hour)
        mHourPicker.setOnValueChangedListener { picker, oldVal, newVal -> // TODO Auto-generated method stub
            mCurrentHour = newVal
            if (!mIs24HourView) {
                // adjust from [1-12] to [0-11] internally, with the times
                // written "12:xx" being the start of the half-day
                if (mCurrentHour == 12) {
                    mCurrentHour = 0
                }
                if (!mIsAm) {
                    // PM means 12 hours later than nominal
                    mCurrentHour += 12
                }
            }
            onTimeChanged()
        }

        // digits of minute
        mMinutePicker = findViewById<View>(R.id.minute) as NumberPicker
        mMinutePicker.minValue = 0
        mMinutePicker.maxValue = 59
        mMinutePicker.setFormatter(TWO_DIGIT_FORMATTER)
        mMinutePicker.setOnValueChangedListener { spinner, oldVal, newVal ->
            mCurrentMinute = newVal
            onTimeChanged()
        }

        // am/pm
        mAmPmButton = findViewById<View>(R.id.amPm) as Button

        // now that the hour/minute picker objects have been initialized, set
        // the hour range properly based on the 12/24 hour display mode.
        configurePickerRanges()

        // initialize to current time
        val cal = Calendar.getInstance()
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER)

        // by default we're not in 24 hour mode
        currentHour = cal[Calendar.HOUR_OF_DAY]
        currentMinute = cal[Calendar.MINUTE]
        mIsAm = mCurrentHour < 12

        /* Get the localized am/pm strings and use them in the spinner */
        val dfs = DateFormatSymbols()
        val dfsAmPm = dfs.amPmStrings
        mAmText = dfsAmPm[Calendar.AM]
        mPmText = dfsAmPm[Calendar.PM]
        mAmPmButton.text = if (mIsAm) mAmText else mPmText
        mAmPmButton.setOnClickListener {
            requestFocus()
            if (mIsAm) {

                // Currently AM switching to PM
                if (mCurrentHour < 12) {
                    mCurrentHour += 12
                }
            } else {

                // Currently PM switching to AM
                if (mCurrentHour >= 12) {
                    mCurrentHour -= 12
                }
            }
            mIsAm = !mIsAm
            mAmPmButton.text = if (mIsAm) mAmText else mPmText
            onTimeChanged()
        }
        if (!isEnabled) {
            isEnabled = false
        }
    }
}
