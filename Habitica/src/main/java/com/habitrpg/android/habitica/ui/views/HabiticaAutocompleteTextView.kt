package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.text.InputType
import android.text.method.ArrowKeyMovementMethod
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import kotlin.math.abs

class HabiticaAutocompleteTextView(context: Context, attrs: AttributeSet?) :
    AppCompatMultiAutoCompleteTextView(context, attrs) {

    private var lastY = 0f
    private var isScrolling = false

    init {
        val removed = this.inputType and (this.inputType xor InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
        this.inputType = removed

        movementMethod = ArrowKeyMovementMethod.getInstance()
        isVerticalScrollBarEnabled = true
        setHorizontallyScrolling(false)
        isLongClickable = true
        setTextIsSelectable(true)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                lastY = event.y
                isScrolling = false
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = abs(event.y - lastY)
                if (deltaY > 10 && !isScrolling) {
                    isScrolling = true
                }

                if (isScrolling && lineCount * lineHeight > height - paddingTop - paddingBottom) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                isScrolling = false
            }
        }
        return super.onTouchEvent(event)
    }
}
