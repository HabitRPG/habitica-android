package com.habitrpg.android.habitica.extensions

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

public fun Drawable.setTintWith(
    context: Context,
    colorResource: Int,
    tintMode: PorterDuff.Mode = PorterDuff.Mode.MULTIPLY
) {
    DrawableCompat.setTintMode(this, tintMode)
    DrawableCompat.setTint(this, ContextCompat.getColor(context, colorResource))
}

public fun Drawable.setTintWith(color: Int, tintMode: PorterDuff.Mode = PorterDuff.Mode.MULTIPLY) {
    DrawableCompat.setTint(this, color)
    DrawableCompat.setTintMode(this, tintMode)
}
