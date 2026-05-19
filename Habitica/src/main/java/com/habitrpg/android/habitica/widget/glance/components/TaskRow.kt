package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors

@Composable
fun TaskRow(
    text: String,
    valueColor: Color,
    valueBorderColor: Color,
    primaryTextColor: ColorProvider = WidgetColors.taskListTaskText,
    checklistDoneCount: Int = 0,
    checklistTotalCount: Int = 0,
    showChecklistCount: Boolean = true,
    onClick: Action,
    modifier: GlanceModifier = GlanceModifier,
    tileWidth: Dp = 44.dp,
    innerSquareSize: Dp = 26.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .width(tileWidth)
                .fillMaxHeight()
                .background(ColorProvider(valueColor)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = GlanceModifier
                    .size(innerSquareSize)
                    .cornerRadius(8.dp)
                    .background(ColorProvider(valueBorderColor)),
            ) {}
        }
        Spacer(GlanceModifier.width(10.dp))
        Text(
            text = text,
            style = TextStyle(
                color = primaryTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 2,
            modifier = GlanceModifier.defaultWeight(),
        )
        if (showChecklistCount && checklistTotalCount > 0) {
            Spacer(GlanceModifier.width(8.dp))
            val isAllDone = checklistDoneCount == checklistTotalCount
            Box(
                modifier = GlanceModifier
                    .cornerRadius(4.dp)
                    .background(
                        if (isAllDone) WidgetColors.checklistBackgroundDone
                        else WidgetColors.checklistBackground,
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$checklistDoneCount/$checklistTotalCount",
                    style = TextStyle(
                        color = if (isAllDone) WidgetColors.textSecondary else ColorProvider(Color.White),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }
        }
        Spacer(GlanceModifier.width(12.dp))
    }
}

