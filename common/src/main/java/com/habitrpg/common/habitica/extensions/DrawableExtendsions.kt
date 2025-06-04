package com.habitrpg.common.habitica.extensions

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

fun Drawable.setTintWith(
    context: Context,
    colorResource: Int,
    tintMode: PorterDuff.Mode = PorterDuff.Mode.MULTIPLY
) {
    DrawableCompat.setTintMode(this, tintMode)
    DrawableCompat.setTint(this, ContextCompat.getColor(context, colorResource))
}

fun Drawable.setTintWith(
    color: Int,
    tintMode: PorterDuff.Mode = PorterDuff.Mode.MULTIPLY
) {
    DrawableCompat.setTint(this, color)
    DrawableCompat.setTintMode(this, tintMode)
}

fun Drawable?.asPainter(): Painter? {
    return if (this is BitmapDrawable) {
        BitmapPainter(this.bitmap.asImageBitmap())
    } else null
}