package com.habitrpg.android.habitica.extensions

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toDrawable

fun Bitmap.asDrawable(resources: Resources): Drawable {
    return this.toDrawable(resources)
}
