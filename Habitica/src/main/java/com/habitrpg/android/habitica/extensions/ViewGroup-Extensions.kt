@file:JvmName("ViewGroupExt")
package com.habitrpg.android.habitica.extensions

import android.view.View
import android.view.ViewGroup
import com.habitrpg.common.habitica.extensions.layoutInflater

fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View =
    context.layoutInflater.inflate(layoutId, this, attachToRoot)
