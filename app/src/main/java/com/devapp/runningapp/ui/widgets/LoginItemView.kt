package com.devapp.runningapp.ui.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.ItemViewLoginBinding
import com.devapp.runningapp.util.DrawableHelper
import com.devapp.runningapp.util.SharedPreferenceHelper

class LoginItemView(context: Context, attributeSet: AttributeSet) :
    LinearLayout(context, attributeSet) {
    private val binding = ItemViewLoginBinding.inflate(LayoutInflater.from(context), this, true)
    private var icon: Drawable? = null
    private var iconRed: Drawable? = null

    init {
        val textHint: String?
        val isAsterisk: Boolean
        val inputType: Int?
        val focusable: Boolean

        val a = context.theme.obtainStyledAttributes(attributeSet, R.styleable.LoginItemView, 0, 0)
        try {
            icon = a.getDrawable(R.styleable.LoginItemView_item_icon)
            iconRed = a.getDrawable(R.styleable.LoginItemView_item_icon_red)
            textHint = a.getString(R.styleable.LoginItemView_item_hint)
            isAsterisk = a.getBoolean(R.styleable.LoginItemView_item_asterisk, false)
            inputType = a.getInt(
                R.styleable.LoginItemView_android_inputType, InputType.TYPE_TEXT_VARIATION_NORMAL
            )
            focusable = a.getBoolean(R.styleable.LoginItemView_android_focusable, true)
        } finally {
            a.recycle()
        }

        binding.apply {
            viewContent.background =
                DrawableHelper.rectangle(
                    context,
                        R.color.colorText_Day_2,
                    1f, 8f
                )

            icon?.apply { ivIcon.setImageDrawable(this) }
            etContent.hint = textHint
            tvAsterisk.visibility = if (isAsterisk) VISIBLE else GONE
            inputType?.apply { etContent.inputType = this }
            etContent.isFocusable = focusable
        }
    }

    fun showWarning(warning: String) {
        binding.apply {
            viewContent.background = DrawableHelper.rectangle(context, R.color.colorRed, 1f, 8f)
            iconRed?.apply { ivIcon.setImageDrawable(this) }
            tvWarning.visibility = VISIBLE
            tvWarning.text = warning
        }
    }

    fun hideWarning() {
        binding.apply {
            viewContent.background =
                DrawableHelper.rectangle(
                    context,
                        R.color.colorText_Day_2,
                    1f, 8f
                )
            icon?.apply { ivIcon.setImageDrawable(this) }
            tvWarning.visibility = GONE
        }
    }

    fun addTextChangedListener(textWatcher: TextWatcher) {
        binding.etContent.addTextChangedListener(textWatcher)
    }

    fun getContent(): String {
        return binding.etContent.text.toString()
    }

    fun setContent(content: String) {
        binding.etContent.setText(content)
    }

    fun isFocus(): Boolean {
        return binding.etContent.isFocused
    }

    fun getViewContent(): View {
        return binding.viewContent
    }

    fun getViewEditContent(): View {
        return binding.etContent
    }

}