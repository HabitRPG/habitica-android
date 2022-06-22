package com.habitrpg.wearos.habitica.models.user

import android.graphics.drawable.Drawable

data class MenuItem(
    val identifier: String,
    val title: String,
    val icon: Drawable?,
    val color: Int,
    val textColor: Int,
    val isProminent: Boolean = false,
    val onClick: () -> Unit
)