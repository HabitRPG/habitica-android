package com.habitrpg.android.habitica.widget.glance.components

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.widget.glance.actions.openAppAction
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors

private val MaterialYouEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun SignedOutContent(modifier: GlanceModifier = GlanceModifier) {
    val background: ColorProvider
    val titleColor: ColorProvider
    val subtitleColor: ColorProvider
    if (MaterialYouEnabled) {
        background = GlanceTheme.colors.primaryContainer
        titleColor = GlanceTheme.colors.onPrimaryContainer
        subtitleColor = GlanceTheme.colors.onSurfaceVariant
    } else {
        background = WidgetColors.background
        titleColor = WidgetColors.text
        subtitleColor = WidgetColors.textSecondary
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(background)
            .padding(16.dp)
            .clickable(onClick = openAppAction()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringRes(R.string.widget_signed_out_title),
                style = TextStyle(
                    color = titleColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = stringRes(R.string.widget_signed_out_subtitle),
                style = TextStyle(
                    color = subtitleColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}
