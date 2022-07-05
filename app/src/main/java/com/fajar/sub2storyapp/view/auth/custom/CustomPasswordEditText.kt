package com.fajar.sub2storyapp.view.auth.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.fajar.sub2storyapp.R

class CustomPasswordEditText : AppCompatEditText {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                error = if (s.toString().length < MINIMUM_CHARACTER) {
                    resources.getString(R.string.error_password)
                } else {
                    null
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        transformationMethod = PasswordTransformationMethod.getInstance()
    }

    companion object {
        private const val MINIMUM_CHARACTER = 6
    }
}