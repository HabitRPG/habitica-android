package com.habitrpg.android.habitica.widget.glance.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
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
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.widget.glance.actions.openAppAction
import com.habitrpg.android.habitica.widget.glance.actions.openProfileAction
import com.habitrpg.android.habitica.widget.glance.components.CurrencyChip
import com.habitrpg.android.habitica.widget.glance.components.CurrencyChipItem
import com.habitrpg.android.habitica.widget.glance.components.LevelChip
import com.habitrpg.android.habitica.widget.glance.components.MergedCurrencyChip
import com.habitrpg.android.habitica.widget.glance.components.SignedOutContent
import com.habitrpg.android.habitica.widget.glance.components.StatRow
import com.habitrpg.android.habitica.widget.glance.components.StatRowMode
import com.habitrpg.android.habitica.widget.glance.components.inlineValueColumnWidth
import com.habitrpg.android.habitica.widget.glance.components.inlineValueText
import com.habitrpg.android.habitica.widget.glance.components.stringRes
import com.habitrpg.android.habitica.widget.glance.components.WidgetLoadingContent
import com.habitrpg.android.habitica.widget.glance.data.WidgetAuth
import com.habitrpg.android.habitica.widget.glance.data.StatsWidgetState
import com.habitrpg.android.habitica.widget.glance.data.hydrateSnapshot
import com.habitrpg.android.habitica.widget.glance.data.loadStatsStateOrNull
import android.os.Build
import androidx.glance.GlanceTheme
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme
import com.habitrpg.android.habitica.widget.glance.theme.WidgetBarColors
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors
import androidx.datastore.preferences.core.Preferences
import androidx.glance.currentState
import com.habitrpg.android.habitica.widget.glance.data.WidgetSnapshotStore
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

private val HORIZONTAL_START_PADDING = 14.dp
private val HORIZONTAL_END_PADDING = 20.dp

private val DEFAULT_BAR_HEIGHT = 9.dp
private val COMPACT_BAR_HEIGHT = 6.dp

class AvatarStatsGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val initial = if (WidgetAuth.isLoggedIn(context)) {
            hydrateSnapshot(context, id, WidgetSnapshotStore.statsKey) {
                loadStatsStateOrNull(context)?.let { WidgetSnapshotStore.encodeStats(it) }
            }?.let { WidgetSnapshotStore.decodeStats(it) }
        } else {
            null
        }
        provideContent {
            val loggedIn = WidgetAuth.isLoggedIn(context)
            val state = if (loggedIn) {
                WidgetSnapshotStore.statsFrom(currentState()) ?: initial
            } else {
                null
            }
            HabiticaWidgetTheme {
                when {
                    !loggedIn -> SignedOutContent()
                    state == null -> WidgetLoadingContent()
                    else -> StatsContent(state)
                }
            }
        }
    }
}

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
        val context = LocalContext.current
        StatsInnerPalette(
            labelText = GlanceTheme.colors.onSurface,
            chipBackground = ColorProvider(
                GlanceTheme.colors.primary.getColor(context).copy(alpha = 0.3f),
            ),
            chipText = GlanceTheme.colors.onSurfaceVariant,
            levelChipBackground = GlanceTheme.colors.primary,
            levelChipText = ColorProvider(
                GlanceTheme.colors.onPrimary.getColor(context).copy(alpha = 0.85f),
            ),
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

@Composable
private fun StatsContent(state: StatsWidgetState) {
    val size = LocalSize.current
    val fontScale = WidgetSnapshotStore.fontScale(LocalContext.current)
    val rows = if (state.showMp) 3 else 2
    val layout = pickLayout(size.width, size.height, rows, fontScale)
    val basePadding = layout.outerPadding
    val noAvatarHorizontal = !layout.avatarOnTop && !layout.showAvatar
    val startPadding = if (noAvatarHorizontal) HORIZONTAL_START_PADDING else basePadding
    val endPadding = if (noAvatarHorizontal) HORIZONTAL_END_PADDING else basePadding
    val horizontalPadding = startPadding + endPadding
    val palette = rememberInnerPalette()

    val tileBackground: ColorProvider = if (MaterialYouEnabled) {
        GlanceTheme.colors.widgetBackground
    } else {
        WidgetColors.background
    }
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(tileBackground)
            .padding(start = startPadding, end = endPadding, top = basePadding, bottom = basePadding)
            .clickable(onClick = openAppAction()),
    ) {
        when {
            layout.avatarOnTop ->
                CompactAvatarLayout(state, layout, size.width, horizontalPadding, palette)
            layout.showAvatar ->
                FullStatsLayout(state, layout, size.width, horizontalPadding, palette)
            else ->
                HorizontalLayout(state, layout, size.width, horizontalPadding, palette)
        }
    }
}

@Composable
private fun CompactAvatarLayout(
    state: StatsWidgetState,
    layout: StatsLayout,
    widgetWidth: Dp,
    horizontalPadding: Dp,
    palette: StatsInnerPalette,
) {
    val columnWidth = (widgetWidth - horizontalPadding).coerceAtLeast(40.dp)
    val avatarHeight = layout.avatarHeight
    val avatarWidth = avatarHeight * COMPACT_AVATAR_ASPECT
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = GlanceModifier.defaultWeight(),
            contentAlignment = Alignment.Center,
        ) {
            AvatarImage(
                state = state,
                width = avatarWidth,
                height = avatarHeight,
                cornerRadius = 14.dp,
                modifier = GlanceModifier.clickable(onClick = openProfileAction(state.userId)),
            )
        }
        StatBars(state = state, layout = layout, columnWidth = columnWidth, palette = palette)
    }
}

@Composable
private fun HorizontalLayout(
    state: StatsWidgetState,
    layout: StatsLayout,
    widgetWidth: Dp,
    horizontalPadding: Dp,
    palette: StatsInnerPalette,
) {
    val columnWidth = (widgetWidth - horizontalPadding).coerceAtLeast(40.dp)

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Box(
            modifier = GlanceModifier.defaultWeight(),
            contentAlignment = Alignment.CenterStart,
        ) {
            StatBars(state = state, layout = layout, columnWidth = columnWidth, palette = palette)
        }
        if (layout.showFooter) {
            Spacer(GlanceModifier.height(FOOTER_SPACING))
            StatsFooter(
                state = state,
                includeLevel = true,
                showFullLevelLabel = layout.cols >= 5,
                palette = palette,
                mergeCurrencyChips = true,
            )
        }
    }
}

@Composable
private fun FullStatsLayout(
    state: StatsWidgetState,
    layout: StatsLayout,
    widgetWidth: Dp,
    horizontalPadding: Dp,
    palette: StatsInnerPalette,
) {
    val avatarHeight = layout.avatarHeight
    val avatarWidth = avatarHeight * AVATAR_ASPECT
    val columnWidth = (widgetWidth - horizontalPadding - avatarWidth - AVATAR_SPACING)
        .coerceAtLeast(40.dp)

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarImage(
                state = state,
                width = avatarWidth,
                height = avatarHeight,
                cornerRadius = 16.dp,
                modifier = GlanceModifier.clickable(onClick = openProfileAction(state.userId)),
            )
            Spacer(GlanceModifier.width(AVATAR_SPACING))
            Column(modifier = GlanceModifier.defaultWeight()) {
                StatBars(state = state, layout = layout, columnWidth = columnWidth, palette = palette)
            }
        }
        StatsFooter(
            state = state,
            includeLevel = true,
            showFullLevelLabel = true,
            palette = palette,
            levelChipWidth = avatarWidth,
        )
    }
}

@Composable
private fun StatBars(
    state: StatsWidgetState,
    layout: StatsLayout,
    columnWidth: Dp,
    palette: StatsInnerPalette,
) {
    val fill = layout.barsFillHeight
    val iconSize = layout.iconSize
    val useThinBar = layout.avatarOnTop || !layout.tall
    val barHeight = if (useThinBar) COMPACT_BAR_HEIGHT else DEFAULT_BAR_HEIGHT
    val gap = layout.rowGap
    val columnModifier = if (fill) {
        GlanceModifier.fillMaxWidth().fillMaxHeight()
    } else {
        GlanceModifier.fillMaxWidth()
    }
    val hpLabel = stringRes(R.string.widget_stat_hp)
    val expLabel = stringRes(R.string.widget_stat_exp)
    val mpLabel = stringRes(R.string.widget_stat_mp)
    val isInline = layout.rowMode == StatRowMode.InlineValueWithLabel ||
        layout.rowMode == StatRowMode.InlineValueMaxWithLabel
    val valueColumnWidth = if (isInline) {
        val texts = buildList {
            add(inlineValueText(layout.rowMode, state.hpText, state.maxHpText, hpLabel))
            add(inlineValueText(layout.rowMode, state.expText, state.toNextLevelText, expLabel))
            if (state.showMp) add(inlineValueText(layout.rowMode, state.mpText, state.maxMpText, mpLabel))
        }
        inlineValueColumnWidth(texts)
    } else {
        0.dp
    }
    val barAvailableWidth = (
        columnWidth -
            (if (isInline) 8.dp else 0.dp) -
            iconSize -
            8.dp -
            (if (isInline) 4.dp + valueColumnWidth else 0.dp)
    ).coerceAtLeast(16.dp)
    Column(modifier = columnModifier) {
        if (fill) Spacer(GlanceModifier.defaultWeight())
        StatRow(
            label = hpLabel,
            value = state.hp,
            maxValue = state.maxHp,
            valueText = state.hpText,
            maxText = state.maxHpText,
            barColor = WidgetBarColors.red,
            iconResId = R.drawable.widget_icon_heart,
            mode = layout.rowMode,
            barAvailableWidth = barAvailableWidth,
            labelTextColor = palette.labelText,
            iconSize = iconSize,
            valueColumnWidth = valueColumnWidth,
            barHeight = barHeight,
        )
        if (fill) Spacer(GlanceModifier.defaultWeight()) else Spacer(GlanceModifier.height(gap))
        StatRow(
            label = expLabel,
            value = state.exp,
            maxValue = state.toNextLevel,
            valueText = state.expText,
            maxText = state.toNextLevelText,
            barColor = WidgetBarColors.yellow,
            iconResId = R.drawable.widget_icon_experience,
            mode = layout.rowMode,
            barAvailableWidth = barAvailableWidth,
            labelTextColor = palette.labelText,
            iconSize = iconSize,
            valueColumnWidth = valueColumnWidth,
            barHeight = barHeight,
        )
        if (state.showMp) {
            if (fill) Spacer(GlanceModifier.defaultWeight()) else Spacer(GlanceModifier.height(gap))
            StatRow(
                label = mpLabel,
                value = state.mp,
                maxValue = state.maxMp,
                valueText = state.mpText,
                maxText = state.maxMpText,
                barColor = WidgetBarColors.blue,
                iconResId = R.drawable.widget_icon_mana,
                mode = layout.rowMode,
                barAvailableWidth = barAvailableWidth,
                labelTextColor = palette.labelText,
                iconSize = iconSize,
                valueColumnWidth = valueColumnWidth,
                barHeight = barHeight,
            )
        }
        if (fill) Spacer(GlanceModifier.defaultWeight())
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
    levelChipWidth: Dp? = null,
    mergeCurrencyChips: Boolean = false,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val levelChip: @Composable () -> Unit = {
            if (includeLevel) {
                val levelModifier = (if (levelChipWidth != null) {
                    GlanceModifier.width(levelChipWidth)
                } else {
                    GlanceModifier
                }).clickable(onClick = openProfileAction(state.userId))
                LevelChip(
                    level = state.level,
                    className = state.className,
                    showFullLabel = showFullLevelLabel,
                    modifier = levelModifier,
                    backgroundColor = palette.levelChipBackground,
                    textColor = palette.levelChipText,
                    horizontalPadding = if (mergeCurrencyChips) 12.dp else 8.dp,
                )
            }
        }
        if (mergeCurrencyChips) {
            Box(
                modifier = GlanceModifier.defaultWeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                levelChip()
            }
            val items = buildList {
                if (state.hourglassCount > 0) {
                    add(
                        CurrencyChipItem(
                            iconProvider = ImageProvider(HabiticaIconsHelper.imageOfHourglass()),
                            text = state.hourglassesText,
                        ),
                    )
                }
                add(
                    CurrencyChipItem(
                        iconProvider = ImageProvider(R.drawable.widget_icon_gem),
                        text = state.gemsText,
                    ),
                )
                add(
                    CurrencyChipItem(
                        iconProvider = ImageProvider(R.drawable.widget_icon_gold),
                        text = state.goldText,
                    ),
                )
            }
            MergedCurrencyChip(
                items = items,
                backgroundColor = palette.chipBackground,
                textColor = palette.chipText,
            )
        } else {
            levelChip()
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
}
