package com.habitrpg.android.habitica.ui.helpers

import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.widget.MultiAutoCompleteTextView

class AutocompleteTokenizer(val tokens: List<Char>) : MultiAutoCompleteTextView.Tokenizer {

    override fun findTokenStart(text: CharSequence, cursor: Int): Int {
        var i = cursor

        while (i > 0 && text[i - 1] != ' ' && !tokens.contains(text[i-1])) {
            i--
        }

        return if (i < 1 || !tokens.contains(text[i-1])) {
            cursor
        } else {
            i - 1
        }
    }

    override fun findTokenEnd(text: CharSequence, cursor: Int): Int {
        var i = cursor
        val len = text.length

        while (i < len) {
            if (text[i] == ' ') {
                return i
            } else {
                i++
            }
        }

        return len
    }

    override fun terminateToken(text: CharSequence): CharSequence {
        var i = text.length

        while (i > 0 && text[i - 1] == ' ') {
            i--
        }

        return if (i > 0 && text[i - 1] == ' ') {
            text
        } else {
            if (text is Spanned) {
                val sp = SpannableString("$text ")
                TextUtils.copySpansFrom(
                    text,
                    0,
                    text.length,
                    Any::class.java,
                    sp,
                    0
                )
                sp
            } else {
                "$text "
            }
        }
    }
}
