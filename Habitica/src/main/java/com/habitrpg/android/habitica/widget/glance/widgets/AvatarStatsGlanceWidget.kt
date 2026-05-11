package com.habitrpg.android.habitica.widget.glance.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
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
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.widget.glance.actions.openAppAction
import com.habitrpg.android.habitica.widget.glance.components.CurrencyChip
import com.habitrpg.android.habitica.widget.glance.components.LevelChip
import com.habitrpg.android.habitica.widget.glance.components.StatRow
import com.habitrpg.android.habitica.widget.glance.components.StatRowMode
import com.habitrpg.android.habitica.widget.glance.data.AvatarBitmapCache
import com.habitrpg.android.habitica.widget.glance.data.StatsWidgetState
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme
import com.habitrpg.android.habitica.widget.glance.theme.WidgetBarColors
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors
import kotlinx.coroutines.flow.firstOrNull

private val SIZE_2x1 = DpSize(100.dp, 40.dp)
private val SIZE_3x1 = DpSize(170.dp, 40.dp)
private val SIZE_4x1 = DpSize(240.dp, 40.dp)
private val SIZE_5x1 = DpSize(310.dp, 40.dp)
private val SIZE_2x2 = DpSize(100.dp, 80.dp)
private val SIZE_3x2 = DpSize(170.dp, 80.dp)
private val SIZE_4x2 = DpSize(240.dp, 80.dp)
private val SIZE_5x2 = DpSize(310.dp, 80.dp)

private const val OUTER_PADDING_DP = 12

class AvatarStatsGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(SIZE_2x1, SIZE_3x1, SIZE_4x1, SIZE_5x1, SIZE_2x2, SIZE_3x2, SIZE_4x2, SIZE_5x2),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = runCatching {
            val user = widgetEntryPoint(context).userRepository().getUser().firstOrNull()
            AvatarBitmapCache.refreshIfNeeded(context, user)
            StatsWidgetState.fromUser(
                context = context,
                user = user,
                avatarBitmapPath = AvatarBitmapCache.cachedFile(context).absolutePath,
            )
        }.getOrElse { StatsWidgetState.Empty }
        provideContent {
            HabiticaWidgetTheme {
                StatsContent(state)
            }
        }
    }
}

private data class StatsLayout(
    val cols: Int,
    val tall: Boolean,
    val rowMode: StatRowMode,
    val showAvatar: Boolean,
    val avatarOnTop: Boolean,
    val showFooter: Boolean,
)

private fun pickLayout(width: Dp, height: Dp): StatsLayout {
    val tall = height >= 80.dp
    val cols = when {
        width >= 310.dp -> 5
        width >= 240.dp -> 4
        width >= 170.dp -> 3
        else -> 2
    }
    val rowMode = when {
        tall && cols >= 4 -> StatRowMode.LabelStackedValue
        !tall && cols >= 5 -> StatRowMode.InlineValueMaxWithLabel
        !tall && cols == 4 -> StatRowMode.InlineValueWithLabel
        else -> StatRowMode.BarOnly
    }
    val showAvatar = tall && (cols == 2 || cols >= 5)
    val avatarOnTop = showAvatar && cols == 2
    val showFooter = tall && cols >= 4
    return StatsLayout(cols, tall, rowMode, showAvatar, avatarOnTop, showFooter)
}

@Composable
private fun StatsContent(state: StatsWidgetState) {
    val size = LocalSize.current
    val layout = pickLayout(size.width, size.height)
    val outerPadding = OUTER_PADDING_DP.dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(WidgetColors.background)
            .padding(outerPadding)
            .clickable(onClick = openAppAction()),
    ) {
        if (layout.avatarOnTop) {
            CompactAvatarLayout(state, layout, size.width, outerPadding)
        } else {
            HorizontalLayout(state, layout, size.width, outerPadding)
        }
    }
}

@Composable
private fun CompactAvatarLayout(
    state: StatsWidgetState,
    layout: StatsLayout,
    widgetWidth: Dp,
    outerPadding: Dp,
) {
    val barWidth = (widgetWidth - outerPadding * 2 - 16.dp - 8.dp).coerceAtLeast(40.dp)
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AvatarImage(state = state, width = 88.dp, height = 92.dp, cornerRadius = 14.dp)
        Spacer(GlanceModifier.defaultWeight())
        StatBars(state = state, layout = layout, barWidth = barWidth)
    }
}

@Composable
private fun HorizontalLayout(
    state: StatsWidgetState,
    layout: StatsLayout,
    widgetWidth: Dp,
    outerPadding: Dp,
) {
    val avatarBoxWidth = if (layout.showAvatar) 124.dp else 0.dp
    val avatarSpacing = if (layout.showAvatar) 12.dp else 0.dp
    val iconAndSpacing = 16.dp + 8.dp
    val barWidth = (widgetWidth - outerPadding * 2 - avatarBoxWidth - avatarSpacing - iconAndSpacing)
        .coerceAtLeast(40.dp)

    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (layout.showAvatar) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AvatarImage(state = state, width = 124.dp, height = 129.dp, cornerRadius = 16.dp)
                Spacer(GlanceModifier.height(6.dp))
                LevelChip(
                    level = state.level,
                    className = state.className,
                    showFullLabel = true,
                )
            }
            Spacer(GlanceModifier.width(12.dp))
        }
        Column(modifier = GlanceModifier.defaultWeight()) {
            StatBars(state = state, layout = layout, barWidth = barWidth)
            if (layout.showFooter) {
                Spacer(GlanceModifier.height(8.dp))
                StatsFooter(
                    state = state,
                    includeLevel = !layout.showAvatar,
                    showFullLevelLabel = layout.cols >= 5,
                )
            }
        }
    }
}

@Composable
private fun StatBars(state: StatsWidgetState, layout: StatsLayout, barWidth: Dp) {
    val gap = if (layout.rowMode == StatRowMode.LabelStackedValue) 10.dp else 6.dp
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        StatRow(
            label = "HP",
            value = state.hp,
            maxValue = state.maxHp,
            valueText = state.hpText,
            maxText = state.maxHpText,
            barColor = WidgetBarColors.red,
            iconResId = R.drawable.widget_icon_heart,
            mode = layout.rowMode,
            barAvailableWidth = barWidth,
        )
        Spacer(GlanceModifier.height(gap))
        StatRow(
            label = "EXP",
            value = state.exp,
            maxValue = state.toNextLevel,
            valueText = state.expText,
            maxText = state.toNextLevelText,
            barColor = WidgetBarColors.yellow,
            iconResId = R.drawable.widget_icon_experience,
            mode = layout.rowMode,
            barAvailableWidth = barWidth,
        )
        if (state.showMp) {
            Spacer(GlanceModifier.height(gap))
            StatRow(
                label = "MP",
                value = state.mp,
                maxValue = state.maxMp,
                valueText = state.mpText,
                maxText = state.maxMpText,
                barColor = WidgetBarColors.blue,
                iconResId = R.drawable.widget_icon_mana,
                mode = layout.rowMode,
                barAvailableWidth = barWidth,
            )
        }
    }
}

@Composable
private fun AvatarImage(
    state: StatsWidgetState,
    width: Dp,
    height: Dp = width,
    cornerRadius: Dp = 0.dp,
) {
    val bitmapFile = state.avatarBitmapPath?.let { java.io.File(it) }
    val bitmap = if (bitmapFile?.exists() == true) {
        runCatching { android.graphics.BitmapFactory.decodeFile(bitmapFile.absolutePath) }.getOrNull()
    } else {
        null
    }
    val baseModifier = GlanceModifier.width(width).height(height)
    val clippedModifier = if (cornerRadius > 0.dp) baseModifier.cornerRadius(cornerRadius) else baseModifier
    if (bitmap != null) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = "Avatar",
            modifier = clippedModifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(modifier = clippedModifier) {}
    }
}

@Composable
private fun StatsFooter(
    state: StatsWidgetState,
    includeLevel: Boolean,
    showFullLevelLabel: Boolean,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (includeLevel) {
            LevelChip(
                level = state.level,
                className = state.className,
                showFullLabel = showFullLevelLabel,
            )
            Spacer(GlanceModifier.defaultWeight())
        }
        if (state.hourglassCount > 0) {
            CurrencyChip(
                iconResId = R.drawable.ic_clock_24dp,
                text = state.hourglassesText,
            )
            Spacer(GlanceModifier.width(6.dp))
        }
        CurrencyChip(
            iconResId = R.drawable.widget_icon_gem,
            text = state.gemsText,
        )
        Spacer(GlanceModifier.width(6.dp))
        CurrencyChip(
            iconResId = R.drawable.widget_icon_gold,
            text = state.goldText,
        )
    }
}
