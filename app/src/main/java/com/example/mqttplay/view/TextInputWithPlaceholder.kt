package com.example.mqttplay.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText

class TextInputWithPlaceholder : TextInputEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val placeholder = hint

    init {
        revealOnFocusHint = true

        hint = ""
        onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                postDelayed({ hint = placeholder }, 100)
            } else {
                hint = ""
            }
        }
    }
}