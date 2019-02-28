package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import net.pherth.android.emoji_library.EmojiTextView

open class HabiticaEmojiTextView : EmojiTextView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    init {
        setEmojiconSize((textSize * 1.5).toInt())
    }
}