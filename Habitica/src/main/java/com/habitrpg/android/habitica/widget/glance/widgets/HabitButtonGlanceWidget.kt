package com.habitrpg.android.habitica.widget.glance.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.activities.HabitButtonWidgetActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.habitrpg.android.habitica.widget.glance.actions.ScoreHabitAction
import com.habitrpg.android.habitica.widget.glance.actions.openAppAction
import com.habitrpg.android.habitica.widget.glance.components.HabitButtonBar
import com.habitrpg.android.habitica.widget.glance.data.HabitButtonWidgetCache
import com.habitrpg.android.habitica.widget.glance.state.WidgetActionKeys
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors
import com.habitrpg.android.habitica.widget.glance.theme.colorForHabitValueLight
import com.habitrpg.android.habitica.widget.glance.theme.colorForHabitValueMedium
import androidx.glance.unit.ColorProvider
import com.habitrpg.shared.habitica.models.responses.TaskDirection

class HabitButtonGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(120.dp, 40.dp),
            DpSize(218.dp, 70.dp),
            DpSize(360.dp, 70.dp),
            DpSize(218.dp, 160.dp),
            DpSize(360.dp, 160.dp),
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val configureIntent = Intent(context, HabitButtonWidgetActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        val configureAction = actionStartActivity(configureIntent)

        provideContent {
            val cached = HabitButtonWidgetCache.fromPrefs(currentState<Preferences>())
            Log.d("HabitButtonWidget", "provideGlance widgetId=$widgetId taskId=${cached?.taskId}")
            HabiticaWidgetTheme {
                if (cached == null) {
                    UnconfiguredContent(onClick = configureAction)
                } else {
                    HabitButtonContent(
                        title = cached.text,
                        showUp = cached.up,
                        showDown = cached.down,
                        value = cached.value,
                        onUpClick = actionRunCallback<ScoreHabitAction>(
                            actionParametersOf(
                                WidgetActionKeys.taskId to cached.taskId,
                                WidgetActionKeys.direction to TaskDirection.UP.text,
                            ),
                        ),
                        onDownClick = actionRunCallback<ScoreHabitAction>(
                            actionParametersOf(
                                WidgetActionKeys.taskId to cached.taskId,
                                WidgetActionKeys.direction to TaskDirection.DOWN.text,
                            ),
                        ),
                    )
                }
            }
        }
    }
}

private val MaterialYouEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
private fun habitTileBackground(): ColorProvider =
    if (MaterialYouEnabled) GlanceTheme.colors.primaryContainer else WidgetColors.background

@Composable
private fun habitTitleText(): ColorProvider =
    if (MaterialYouEnabled) GlanceTheme.colors.onPrimaryContainer else WidgetColors.text

@Composable
private fun HabitButtonContent(
    title: String,
    showUp: Boolean,
    showDown: Boolean,
    value: Double,
    onUpClick: Action,
    onDownClick: Action,
) {
    val size = LocalSize.current
    val isTall = size.height >= 130.dp
    val isVeryCompact = size.height < 60.dp
    val titleFraction = if (isTall) 0.35f else 0.50f
    val titleHeight = size.height * titleFraction
    val barColor = colorForHabitValueLight(value)
    val circleColor = colorForHabitValueMedium(value)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(habitTileBackground()),
    ) {
        HabitButtonBar(
            showUp = showUp,
            showDown = showDown,
            barColor = barColor,
            circleColor = circleColor,
            isTall = isTall,
            onUpClick = onUpClick,
            onDownClick = onDownClick,
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
        )
        if (!isVeryCompact) {
            TitleStrip(title = title, height = titleHeight)
        }
    }
}

@Composable
private fun TitleStrip(title: String, height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 14.dp)
            .clickable(onClick = openAppAction("habitica://user/tasks/habit")),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = habitTitleText(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
            maxLines = 2,
        )
    }
}

@Composable
private fun UnconfiguredContent(onClick: Action) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(habitTileBackground())
            .padding(12.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Tap to configure habit",
            style = TextStyle(
                color = habitTitleText(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}
