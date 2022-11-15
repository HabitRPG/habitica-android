package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// http://stackoverflow.com/a/6700718/1315039
class Typewriter : androidx.appcompat.widget.AppCompatTextView {
    private var job: Job? = null

    private var stringBuilder: SpannableStringBuilder? = null
    private var visibleSpan: Any? = null
    private var hiddenSpan: Any? = null
    private var index: Int = 0
    private var delay: Long = 30

    val isAnimating: Boolean
        get() = index < stringBuilder?.length ?: 0

    constructor(context: Context) : super(context) {
        setupTextColors(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setupTextColors(context)
    }

    override fun onDetachedFromWindow() {
        job?.cancel()
        super.onDetachedFromWindow()
    }

    private fun setupTextColors(context: Context) {
        visibleSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_primary))
        hiddenSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.transparent))
    }

    fun animateText(text: CharSequence) {
        stringBuilder = SpannableStringBuilder(text)
        stringBuilder?.setSpan(hiddenSpan, 0, stringBuilder?.length ?: 0, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        index = 0

        setText(stringBuilder)
        job?.cancel()
        job = MainScope().launch(Dispatchers.Main) {
            while (index <= (stringBuilder?.length ?: 0)) {
                stringBuilder?.setSpan(visibleSpan, 0, index++, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                setText(stringBuilder)
                delay(delay)
            }
        }
    }

    fun stopTextAnimation() {
        index = stringBuilder?.length ?: 0
    }
}
