package com.habitrpg.android.habitica.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun UserRow(
    username: String,
    modifier: Modifier = Modifier,
    extraContent: @Composable (() -> Unit)? = null,
    endContent: @Composable (() -> Unit)? = null,
    color: Color? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                "@$username",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = color ?: MaterialTheme.colors.primary,
            )
            if (extraContent != null) {
                extraContent()
            }
        }
        if (endContent != null) {
            endContent()
        }
    }
}