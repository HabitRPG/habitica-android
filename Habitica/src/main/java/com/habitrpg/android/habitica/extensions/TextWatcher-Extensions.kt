package com.habitrpg.android.habitica.extensions

import android.text.Editable
import android.text.TextWatcher

class OnChangeTextWatcher(private var function: (CharSequence?, Int, Int, Int) -> Unit) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        function(s, start, before, count)
    }
}

class BeforeChangeTextWatcher(private var function: (CharSequence?, Int, Int, Int) -> Unit) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        function(s, start, count, after)
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}

class AfterChangeTextWatcher(private var function: (Editable?) -> Unit) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        function(s)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}