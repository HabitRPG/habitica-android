package com.habitrpg.android.habitica.extensions

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat


public fun Drawable.setTintWith(context: Context, colorResource: Int, tintMode: PorterDuff.Mode) {
    this.setTintMode(tintMode)
    this.setTint(ContextCompat.getColor(context, colorResource))
}

public fun Drawable.setTintWith(color: Int, tintMode: PorterDuff.Mode) {
    this.setTintMode(tintMode)
    this.setTint(color)
}