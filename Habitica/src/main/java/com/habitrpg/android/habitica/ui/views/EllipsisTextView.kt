package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class EllipsisTextView : AppCompatTextView {
    private val ellipsesListeners = ArrayList<EllipsisListener>()

    private var ellipses: Boolean = false

    interface EllipsisListener {
        fun ellipsisStateChanged(ellipses: Boolean)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle,
    )

    fun addEllipsesListener(listener: EllipsisListener?) {
        if (listener == null) {
            throw NullPointerException()
        }
        ellipsesListeners.add(listener)
    }

    fun removeEllipsesListener(listener: EllipsisListener) {
        ellipsesListeners.remove(listener)
    }

    fun hadEllipses(): Boolean = ellipses

    override fun layout(
        l: Int,
        t: Int,
        r: Int,
        b: Int,
    ) {
        super.layout(l, t, r, b)
        checkEllipsis()
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int,
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        post { checkEllipsis() }
    }

    private fun checkEllipsis() {
        val newEllipsis = layout?.let { it.lineCount >= maxLines } ?: false
        if (newEllipsis != ellipses) {
            ellipses = newEllipsis
            for (listener in ellipsesListeners) {
                listener.ellipsisStateChanged(ellipses)
            }
        }
    }
}
