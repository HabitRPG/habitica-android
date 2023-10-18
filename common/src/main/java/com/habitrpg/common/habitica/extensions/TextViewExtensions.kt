package com.habitrpg.common.habitica.extensions

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView

class HabiticaClickableSpan(val onClickAction: () -> Unit): ClickableSpan() {
    override fun onClick(widget: View) {
        onClickAction()
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }
}

fun TextView.handleUrlClicks(onClicked: ((String) -> Unit)? = null) {
    // create span builder and replaces current text with it
    text = SpannableStringBuilder.valueOf(text).apply {
        // search for all URL spans and replace all spans with our own clickable spans
        getSpans(0, length, URLSpan::class.java).forEach {
            // add new clickable span at the same position
            setSpan(
                HabiticaClickableSpan {
                    onClicked?.invoke(it.url)
                },
                getSpanStart(it),
                getSpanEnd(it),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            // remove old URLSpan
            removeSpan(it)
        }
    }
    // make sure movement method is set
    movementMethod = LinkMovementMethod.getInstance()
}
