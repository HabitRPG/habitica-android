package com.habitrpg.android.habitica.widget.glance.components

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.glance.LocalContext

@Composable
fun stringRes(@StringRes id: Int): String =
    LocalContext.current.getString(id)

@Composable
fun stringRes(@StringRes id: Int, vararg formatArgs: Any): String =
    LocalContext.current.getString(id, *formatArgs)

@Composable
fun pluralRes(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String =
    LocalContext.current.resources.getQuantityString(id, quantity, *formatArgs)
