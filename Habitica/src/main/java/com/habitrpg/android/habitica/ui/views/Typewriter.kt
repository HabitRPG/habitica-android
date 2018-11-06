package com.habitrpg.android.habitica.ui.views


import android.content.Context
import android.os.Handler
import androidx.core.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet

import com.habitrpg.android.habitica.R

// http://stackoverflow.com/a/6700718/1315039
class Typewriter : androidx.appcompat.widget.AppCompatTextView {

    private var stringBuilder: SpannableStringBuilder? = null
    private var visibleSpan: Any? = null
    private var hiddenSpan: Any? = null
    private var index: Int = 0
    private var delay: Long = 30

    private val textHandler = Handler()
    private val characterAdder = object : Runnable {
        override fun run() {
            stringBuilder?.setSpan(visibleSpan, 0, index++, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            text = stringBuilder
            if (index <= stringBuilder?.length ?: 0) {
                textHandler.postDelayed(this, delay)
            }
        }
    }

    val isAnimating: Boolean
        get() = index < stringBuilder?.length ?: 0


    constructor(context: Context) : super(context) {
        setupTextColors(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setupTextColors(context)
    }

    private fun setupTextColors(context: Context) {
        visibleSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.textColorLight))
        hiddenSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.transparent))
    }

    fun animateText(text: CharSequence) {
        stringBuilder = SpannableStringBuilder(text)
        stringBuilder?.setSpan(hiddenSpan, 0, stringBuilder?.length ?: 0, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        index = 0

        setText(stringBuilder)
        textHandler.removeCallbacks(characterAdder)
        textHandler.postDelayed(characterAdder, delay)
    }

    fun setCharacterDelay(millis: Long) {
        delay = millis
    }

    fun stopTextAnimation() {
        index = stringBuilder?.length ?: 0
    }
}
