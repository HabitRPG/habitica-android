package com.habitrpg.android.habitica.widget.glance.widgets

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.datastore.preferences.core.Preferences
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.widget.glance.actions.openAppAction
import com.habitrpg.android.habitica.widget.glance.components.SignedOutContent
import com.habitrpg.android.habitica.widget.glance.components.SegmentedProgressBar
import com.habitrpg.android.habitica.widget.glance.components.pluralRes
import com.habitrpg.android.habitica.widget.glance.components.stringRes
import com.habitrpg.android.habitica.widget.glance.data.WidgetAuth
import com.habitrpg.android.habitica.widget.glance.data.DailyCountWidgetState
import com.habitrpg.android.habitica.widget.glance.data.computeNeedsCron
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.state.WidgetStateKeys
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme
import com.habitrpg.android.habitica.widget.glance.theme.WidgetBarColors
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class DailiesCountGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(218.dp, 110.dp),
            DpSize(290.dp, 110.dp),
            DpSize(360.dp, 110.dp),
            DpSize(430.dp, 110.dp),
            DpSize(500.dp, 110.dp),
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        if (!WidgetAuth.isLoggedIn(context)) {
            provideContent { HabiticaWidgetTheme { SignedOutContent() } }
            return
        }
        val raw = withContext(Dispatchers.Main) {
            val entry = widgetEntryPoint(context)
            val user = entry.userRepository().getUser().firstOrNull()
            val mirroredGroupIds = user?.preferences?.tasks?.mirrorGroupTasks
                ?.toTypedArray() ?: emptyArray()
            val tasks = entry.taskRepository().getTasks(
                taskType = TaskType.DAILY,
                userID = user?.id,
                includedGroupIDs = mirroredGroupIds,
            ).firstOrNull().orEmpty().filter { it.isDue == true }

            DailyCountRaw(
                dueIds = tasks.mapNotNull { it.id }.toSet(),
                completedIds = tasks.filter { it.completed }.mapNotNull { it.id }.toSet(),
                needsCron = computeNeedsCron(user),
            )
        }

        provideContent {
            val hiddenIds = currentState<Preferences>()[WidgetStateKeys.taskListHiddenIds] ?: emptySet()
            val effectiveCompleted = (raw.completedIds + hiddenIds.intersect(raw.dueIds)).size
            val state = DailyCountWidgetState(
                totalDue = raw.dueIds.size,
                completed = effectiveCompleted,
                needsCron = raw.needsCron,
            )
            HabiticaWidgetTheme {
                DailiesCountTile(state)
            }
        }
    }
}

private data class DailyCountRaw(
    val dueIds: Set<String>,
    val completedIds: Set<String>,
    val needsCron: Boolean,
)

private val MaterialYouEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

internal data class DailiesTilePalette(
    val tileBackground: ColorProvider,
    val primaryText: ColorProvider,
    val secondaryText: ColorProvider,
    val accentNumber: ColorProvider,
    val iconTint: ColorProvider?,
    val trackColor: ColorProvider,
)

@Composable
private fun rememberPalette(): DailiesTilePalette {
    return if (MaterialYouEnabled) {
        DailiesTilePalette(
            tileBackground = GlanceTheme.colors.widgetBackground,
            primaryText = GlanceTheme.colors.onSurface,
            secondaryText = GlanceTheme.colors.onSurfaceVariant,
            accentNumber = GlanceTheme.colors.primary,
            iconTint = GlanceTheme.colors.onSurface,
            trackColor = GlanceTheme.colors.outline,
        )
    } else {
        DailiesTilePalette(
            tileBackground = ColorProvider(R.color.widget_bg),
            primaryText = ColorProvider(R.color.widget_text),
            secondaryText = ColorProvider(R.color.widget_text_secondary),
            accentNumber = ColorProvider(R.color.widget_dailies_purple),
            iconTint = null,
            trackColor = ColorProvider(R.color.widget_progress_track),
        )
    }
}

private fun progressColor(progress: Float): Color = when {
    progress >= 1.0f -> WidgetBarColors.purple
    progress >= 0.67f -> WidgetBarColors.blue
    progress >= 0.34f -> WidgetBarColors.orange
    else -> WidgetBarColors.red
}

@Composable
private fun DailiesCountTile(state: DailyCountWidgetState) {
    val palette = rememberPalette()
    val size = LocalSize.current
    val tileInnerPadding = 14.dp
    val barAvailableWidth = (size.width - tileInnerPadding * 2).coerceAtLeast(40.dp)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(palette.tileBackground),
    ) {
        val isAllDone = state.totalDue > 0 && state.completed == state.totalDue
        when {
            state.needsCron -> StartDayContent(palette, tileInnerPadding)
            isAllDone -> AllDoneContent(state.totalDue, palette, barAvailableWidth, tileInnerPadding)
            else -> InProgressContent(state, palette, barAvailableWidth, tileInnerPadding)
        }
    }
}

@Composable
private fun StartDayContent(palette: DailiesTilePalette, innerPadding: Dp) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(innerPadding)
            .clickable(onClick = openAppAction("habitica://user/tasks/daily")),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(R.drawable.widget_start_day),
            contentDescription = stringRes(R.string.widget_start_day),
            modifier = GlanceModifier.size(36.dp),
            colorFilter = palette.iconTint?.let { ColorFilter.tint(it) },
        )
        Spacer(GlanceModifier.height(8.dp))
        Text(
            text = stringRes(R.string.widget_start_day),
            style = TextStyle(
                color = palette.primaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun InProgressContent(
    state: DailyCountWidgetState,
    palette: DailiesTilePalette,
    barAvailableWidth: Dp,
    innerPadding: Dp,
) {
    val progress = if (state.totalDue > 0) (state.completed.toFloat() / state.totalDue).coerceIn(0f, 1f) else 0f
    val remaining = state.totalDue - state.completed

    GaugeBody(
        topNumber = state.completed.toString(),
        topLabel = stringRes(R.string.widget_dailies_done),
        bottomCaption = pluralRes(R.plurals.widget_dailies_left_to_do, remaining, remaining),
        progress = progress,
        progressColor = progressColor(progress),
        showSparkles = false,
        palette = palette,
        barAvailableWidth = barAvailableWidth,
        innerPadding = innerPadding,
        onClick = openAppAction("habitica://user/tasks/daily"),
    )
}

@Composable
private fun AllDoneContent(
    totalCount: Int,
    palette: DailiesTilePalette,
    barAvailableWidth: Dp,
    innerPadding: Dp,
) {
    GaugeBody(
        topNumber = totalCount.toString(),
        topLabel = stringRes(R.string.widget_dailies_done),
        bottomCaption = stringRes(R.string.widget_empty_dailies),
        progress = 1f,
        progressColor = WidgetBarColors.purple,
        showSparkles = true,
        palette = palette,
        barAvailableWidth = barAvailableWidth,
        innerPadding = innerPadding,
        onClick = openAppAction("habitica://user/tasks/daily"),
    )
}

@Composable
private fun GaugeBody(
    topNumber: String,
    topLabel: String,
    bottomCaption: String,
    progress: Float,
    progressColor: Color,
    showSparkles: Boolean,
    palette: DailiesTilePalette,
    barAvailableWidth: Dp,
    innerPadding: Dp,
    onClick: androidx.glance.action.Action,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(innerPadding)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start,
    ) {
        Column(
            modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Row(
                modifier = GlanceModifier.padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = topNumber,
                    style = TextStyle(
                        color = palette.accentNumber,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                if (showSparkles) {
                    Spacer(GlanceModifier.width(6.dp))
                    Image(
                        provider = ImageProvider(R.drawable.widget_sparkles),
                        contentDescription = null,
                        modifier = GlanceModifier.size(32.dp),
                    )
                }
            }
            Text(
                text = topLabel,
                style = TextStyle(
                    color = palette.primaryText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = GlanceModifier.padding(start = 4.dp),
            )
        }
        SegmentedProgressBar(
            progress = progress,
            fillColor = ColorProvider(progressColor),
            trackColor = palette.trackColor,
            availableWidth = barAvailableWidth - 8.dp,
            modifier = GlanceModifier.padding(horizontal = 4.dp),
        )
        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = bottomCaption,
            style = TextStyle(
                color = palette.secondaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
            modifier = GlanceModifier.padding(start = 4.dp),
        )
    }
}
