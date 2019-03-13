package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.MultiAutoCompleteTextView
import android.text.InputType



class HabiticaAutocompleteTextView(context: Context?, attrs: AttributeSet?) : MultiAutoCompleteTextView(context, attrs) {

    init {
        val removed = this.inputType and (this.inputType xor InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
        this.inputType = removed
    }

}