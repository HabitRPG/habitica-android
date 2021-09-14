package com.habitrpg.android.habitica.extensions

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

fun Bitmap.asDrawable(resources: Resources): Drawable {
    return BitmapDrawable(resources, this)
}
