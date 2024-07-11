package com.habitrpg.android.habitica.rpgClassSelectScreen

import android.graphics.Bitmap
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class RpgClass(
    @StringRes val rpgName: Int,
    @ColorRes val rpgColor: Int,
    val icon: Bitmap,
    @StringRes val textDescription : Int,
    val serverName : String,
    @DrawableRes val pic : Int
)
