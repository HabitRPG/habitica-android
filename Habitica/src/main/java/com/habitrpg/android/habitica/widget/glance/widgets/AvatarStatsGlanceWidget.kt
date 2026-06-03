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
import com.habitrpg.android.habitica.widget.glance.actions.openProfileAction
import com.habitrpg.android.habitica.widget.glance.components.CurrencyChip
import com.habitrpg.android.habitica.widget.glance.components.LevelChip
import com.habitrpg.android.habitica.widget.glance.components.SignedOutContent
import com.habitrpg.android.habitica.widget.glance.components.StatRow
import com.habitrpg.android.habitica.widget.glance.components.StatRowMode
import com.habitrpg.android.habitica.widget.glance.components.stringRes
import com.habitrpg.android.habitica.widget.glance.data.WidgetAuth
import com.habitrpg.android.habitica.widget.glance.data.AvatarBitmapCache
import com.habitrpg.android.habitica.widget.glance.data.StatsWidgetState
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import android.os.Build
import androidx.glance.GlanceTheme
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme
import com.habitrpg.android.habitica.widget.glance.theme.WidgetBarColors
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors
import androidx.datastore.preferences.core.Preferences
import androidx.glance.currentState
import com.habitrpg.android.habitica.widget.glance.state.WidgetStateKeys
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.helpers.NumberAbbreviator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

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
        if (!WidgetAuth.isLoggedIn(context)) {
            provideContent { HabiticaWidgetTheme { SignedOutContent() } }
            return
        }
        val rawState = withContext(Dispatchers.Main) {
            runCatching {
                val user = widgetEntryPoint(context).userRepository().getUser().firstOrNull()
                AvatarBitmapCache.refreshIfNeeded(context, user)
                StatsWidgetState.fromUser(
                    context = context,
                    user = user,
                    avatarBitmapPath = AvatarBitmapCache.cachedFile(context).absolutePath,
                )
            }.getOrElse { StatsWidgetState.Empty }
        }
        provideContent {
            val prefs = currentState<Preferences>()
            val state = if (prefs[WidgetStateKeys.statOverrideValid] == true) {
                applyStatOverride(rawState, prefs, context)
            } else {
                rawState
            }
            HabiticaWidgetTheme {
                StatsContent(state)
            }
        }
    }
}

private fun applyStatOverride(
    state: StatsWidgetState,
    prefs: Preferences,
    context: Context,
): StatsWidgetState {
    val hp = (prefs[WidgetStateKeys.statOverrideHp] ?: state.hp).coerceIn(0f, state.maxHp)
    val exp = (prefs[WidgetStateKeys.statOverrideExp] ?: state.exp).coerceAtLeast(0f)
    val mp = (prefs[WidgetStateKeys.statOverrideMp] ?: state.mp).coerceIn(0f, state.maxMp)
    val gold = (prefs[WidgetStateKeys.statOverrideGold] ?: state.goldText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val goldText = NumberAbbreviator.abbreviate(context, gold, numberOfDecimals = 0, minForAbbrevation = 1000)
    return state.copy(
        hp = hp,
        exp = exp,
        mp = mp,
        goldText = goldText,
    )
}

private data class StatsLayout(
    val cols: Int,
    val tall: Boolean,
    val rowMode: StatRowMode,
    val showAvatar: Boolean,
    val avatarOnTop: Boolean,
    val showFooter: Boolean,
)

private data class StatsInnerPalette(
    val labelText: ColorProvider,
    val chipBackground: ColorProvider,
    val chipText: ColorProvider,
    val levelChipBackground: ColorProvider,
    val levelChipText: ColorProvider,
)

private val MaterialYouEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
private fun rememberInnerPalette(): StatsInnerPalette {
    return if (MaterialYouEnabled) {
        StatsInnerPalette(
            labelText = GlanceTheme.colors.onPrimary,
            chipBackground = GlanceTheme.colors.secondaryContainer,
            chipText = GlanceTheme.colors.onSecondaryContainer,
            levelChipBackground = GlanceTheme.colors.tertiaryContainer,
            levelChipText = GlanceTheme.colors.onTertiaryContainer,
        )
    } else {
        StatsInnerPalette(
            labelText = WidgetColors.text,
            chipBackground = WidgetColors.currencyChipBackground,
            chipText = WidgetColors.currencyChipText,
            levelChipBackground = WidgetColors.levelChipBackground,
            levelChipText = WidgetColors.levelChipText,
        )
    }
}

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
    val palette = rememberInnerPalette()

    val tileBackground: ColorProvider = if (MaterialYouEnabled) {
        GlanceTheme.colors.primary
    } else {
        WidgetColors.background
    }
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(tileBackground)
            .padding(outerPadding)
            .clickable(onClick = openAppAction()),
    ) {
        if (layout.avatarOnTop) {
            CompactAvatarLayout(state, layout, size.width, outerPadding, palette)
        } else {
            HorizontalLayout(state, layout, size.width, outerPadding, palette)
        }
    }
}

@Composable
private fun CompactAvatarLayout(
    state: StatsWidgetState,
    layout: StatsLayout,
    widgetWidth: Dp,
    outerPadding: Dp,
    palette: StatsInnerPalette,
) {
    val barWidth = (widgetWidth - outerPadding * 2 - 24.dp - 8.dp).coerceAtLeast(40.dp)
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AvatarImage(
            state = state,
            width = 88.dp,
            height = 92.dp,
            cornerRadius = 14.dp,
            modifier = GlanceModifier.clickable(onClick = openProfileAction(state.userId)),
        )
        Spacer(GlanceModifier.defaultWeight())
        StatBars(state = state, layout = layout, barWidth = barWidth, palette = palette)
    }
}

@Composable
private fun HorizontalLayout(
    state: StatsWidgetState,
    layout: StatsLayout,
    widgetWidth: Dp,
    outerPadding: Dp,
    palette: StatsInnerPalette,
) {
    val avatarBoxWidth = if (layout.showAvatar) 124.dp else 0.dp
    val avatarSpacing = if (layout.showAvatar) 12.dp else 0.dp
    val iconAndSpacing = 24.dp + 8.dp
    val barWidth = (widgetWidth - outerPadding * 2 - avatarBoxWidth - avatarSpacing - iconAndSpacing)
        .coerceAtLeast(40.dp)

    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (layout.showAvatar) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AvatarImage(
                    state = state,
                    width = 124.dp,
                    height = 129.dp,
                    cornerRadius = 16.dp,
                    modifier = GlanceModifier.clickable(onClick = openProfileAction(state.userId)),
                )
                Spacer(GlanceModifier.height(6.dp))
                LevelChip(
                    level = state.level,
                    className = state.className,
                    showFullLabel = true,
                    modifier = GlanceModifier
                        .width(124.dp)
                        .clickable(onClick = openProfileAction(state.userId)),
                    backgroundColor = palette.levelChipBackground,
                    textColor = palette.levelChipText,
                )
            }
            Spacer(GlanceModifier.width(12.dp))
        }
        Column(modifier = GlanceModifier.defaultWeight()) {
            StatBars(state = state, layout = layout, barWidth = barWidth, palette = palette)
            if (layout.showFooter) {
                Spacer(GlanceModifier.height(8.dp))
                StatsFooter(
                    state = state,
                    includeLevel = !layout.showAvatar,
                    showFullLevelLabel = layout.cols >= 5,
                    palette = palette,
                )
            }
        }
    }
}

@Composable
private fun StatBars(
    state: StatsWidgetState,
    layout: StatsLayout,
    barWidth: Dp,
    palette: StatsInnerPalette,
) {
    val gap = if (layout.rowMode == StatRowMode.LabelStackedValue) 10.dp else 6.dp
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        StatRow(
            label = stringRes(R.string.widget_stat_hp),
            value = state.hp,
            maxValue = state.maxHp,
            valueText = state.hpText,
            maxText = state.maxHpText,
            barColor = WidgetBarColors.red,
            iconResId = R.drawable.widget_icon_heart,
            mode = layout.rowMode,
            barAvailableWidth = barWidth,
            labelTextColor = palette.labelText,
        )
        Spacer(GlanceModifier.height(gap))
        StatRow(
            label = stringRes(R.string.widget_stat_exp),
            value = state.exp,
            maxValue = state.toNextLevel,
            valueText = state.expText,
            maxText = state.toNextLevelText,
            barColor = WidgetBarColors.yellow,
            iconResId = R.drawable.widget_icon_experience,
            mode = layout.rowMode,
            barAvailableWidth = barWidth,
            labelTextColor = palette.labelText,
        )
        if (state.showMp) {
            Spacer(GlanceModifier.height(gap))
            StatRow(
                label = stringRes(R.string.widget_stat_mp),
                value = state.mp,
                maxValue = state.maxMp,
                valueText = state.mpText,
                maxText = state.maxMpText,
                barColor = WidgetBarColors.blue,
                iconResId = R.drawable.widget_icon_mana,
                mode = layout.rowMode,
                barAvailableWidth = barWidth,
                labelTextColor = palette.labelText,
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
    modifier: GlanceModifier = GlanceModifier,
) {
    val bitmapFile = state.avatarBitmapPath?.let { java.io.File(it) }
    val bitmap = if (bitmapFile?.exists() == true) {
        runCatching { android.graphics.BitmapFactory.decodeFile(bitmapFile.absolutePath) }.getOrNull()
    } else {
        null
    }
    val baseModifier = modifier.width(width).height(height)
    val clippedModifier = if (cornerRadius > 0.dp) baseModifier.cornerRadius(cornerRadius) else baseModifier
    if (bitmap != null) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = stringRes(R.string.avatar),
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
    palette: StatsInnerPalette,
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
                modifier = GlanceModifier.clickable(onClick = openProfileAction(state.userId)),
                backgroundColor = palette.levelChipBackground,
                textColor = palette.levelChipText,
            )
        }
        Spacer(GlanceModifier.defaultWeight())
        if (state.hourglassCount > 0) {
            CurrencyChip(
                iconProvider = ImageProvider(HabiticaIconsHelper.imageOfHourglass()),
                text = state.hourglassesText,
                backgroundColor = palette.chipBackground,
                textColor = palette.chipText,
            )
            Spacer(GlanceModifier.width(4.dp))
        }
        CurrencyChip(
            iconProvider = ImageProvider(R.drawable.widget_icon_gem),
            text = state.gemsText,
            backgroundColor = palette.chipBackground,
            textColor = palette.chipText,
        )
        Spacer(GlanceModifier.width(4.dp))
        CurrencyChip(
            iconProvider = ImageProvider(R.drawable.widget_icon_gold),
            text = state.goldText,
            backgroundColor = palette.chipBackground,
            textColor = palette.chipText,
        )
    }
}
