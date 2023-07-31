package com.habitrpg.android.habitica.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme

@Composable
fun HabiticaButton(
    background: Color,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    fontSize: TextUnit = 16.sp,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(background, HabiticaTheme.shapes.medium)
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(contentPadding)
    ) {
        ProvideTextStyle(
            value = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        ) {
            content()
        }
    }
}
