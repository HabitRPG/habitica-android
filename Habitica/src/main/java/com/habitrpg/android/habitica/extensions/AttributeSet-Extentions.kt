package com.habitrpg.android.habitica.extensions

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet

fun AttributeSet.styledAttributes(context: Context?, style: IntArray?): TypedArray? =
        context?.theme?.obtainStyledAttributes(this, style ?: IntArray(0), 0, 0)