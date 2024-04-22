package com.habitrpg.android.habitica.ui.views

/**
 * Author: Michael Ritchie, ThanksMister LLC
 * Date: 10/16/12
 * Web: thanksmister.com
 *
 * Extension of <code>TextView</code> that adds listener for ellipses changes.  This can be used to determine
 * if a TextView has an ellipses or not.
 *
 * Derived from discussion on StackOverflow:
 *
 * http://stackoverflow.com/questions/4005933/how-do-i-tell-if-my-textview-has-been-ellipsized
 */

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

    fun hadEllipses(): Boolean {
        return ellipses
    }

    override fun layout(
        l: Int,
        t: Int,
        r: Int,
        b: Int,
    ) {
        super.layout(l, t, r, b)

        ellipses = false
        val layout = layout
        if (layout != null) {
            val lines = layout.lineCount
            if (lines >= maxLines) {
                ellipses = true
            }
        }

        for (listener in ellipsesListeners) {
            listener.ellipsisStateChanged(ellipses)
        }
    }
}
