package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.text.InputType
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView

class HabiticaAutocompleteTextView(context: Context, attrs: AttributeSet?) :
    AppCompatMultiAutoCompleteTextView(context, attrs) {
    init {
        val removed = this.inputType and (this.inputType xor InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
        this.inputType = removed

        movementMethod = ScrollingMovementMethod.getInstance()
        isVerticalScrollBarEnabled = true
        setHorizontallyScrolling(false)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (hasFocus() && (canScrollVertically(1) || canScrollVertically(-1))) {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_MOVE -> {
                    if (lineCount * lineHeight > height - paddingTop - paddingBottom) {
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
