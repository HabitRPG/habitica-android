package com.habitrpg.android.habitica.widget.glance.widgets

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habitrpg.android.habitica.widget.glance.components.StatRowMode

internal val TALL_THRESHOLD = 120.dp

internal val OUTER_PADDING = 12.dp
internal val COMPACT_OUTER_PADDING = 8.dp
internal val MICRO_OUTER_PADDING = 4.dp

internal val AVATAR_MAX_HEIGHT = 129.dp
internal val AVATAR_MIN_HEIGHT = 96.dp
internal const val AVATAR_ASPECT = 124f / 129f
internal val AVATAR_SPACING = 12.dp

internal val COMPACT_AVATAR_MAX_HEIGHT = 92.dp
internal val COMPACT_AVATAR_MIN_HEIGHT = 64.dp
internal const val COMPACT_AVATAR_ASPECT = 88f / 92f

internal val CHIP_HEIGHT = 30.dp
internal val STACKED_ROW_GAP = 6.dp
internal val BAR_ROW_GAP = 6.dp
internal val FOOTER_SPACING = 6.dp

internal val FILL_ICON_SIZE = 30.dp
internal val TALL_ICON_SIZE = 24.dp
internal val COMPACT_ICON_SIZE = 18.dp
internal val TINY_ICON_SIZE = 14.dp
internal val MICRO_ICON_SIZE = 10.dp
internal val COMPACT_ROW_GAP = 4.dp
internal val TINY_ROW_GAP = 3.dp

internal val STACKED_ROW_CHROME = 18.5.dp
internal const val LABEL_TEXT_SP = 14f
internal const val LINE_HEIGHT_FACTOR = 1.3f

internal data class StatsLayout(
    val cols: Int,
    val tall: Boolean,
    val rowMode: StatRowMode,
    val showAvatar: Boolean,
    val avatarOnTop: Boolean,
    val showFooter: Boolean,
    val barsFillHeight: Boolean,
    val avatarHeight: Dp,
    val iconSize: Dp,
    val rowGap: Dp,
    val outerPadding: Dp,
)

private data class CompactTier(
    val rowMode: StatRowMode,
    val padding: Dp,
    val iconSize: Dp,
    val gap: Dp,
)

internal fun chipHeight(fontScale: Float): Dp = CHIP_HEIGHT * maxOf(1f, fontScale)

internal fun labelLineHeight(fontScale: Float): Dp =
    (LABEL_TEXT_SP * LINE_HEIGHT_FACTOR * fontScale).dp

internal fun rowHeight(mode: StatRowMode, iconSize: Dp, fontScale: Float): Dp = when (mode) {
    StatRowMode.LabelStackedValue ->
        maxOf(iconSize, STACKED_ROW_CHROME + labelLineHeight(fontScale))
    StatRowMode.BarOnly -> iconSize
    else -> maxOf(iconSize, labelLineHeight(fontScale))
}

internal fun stackHeight(
    mode: StatRowMode,
    rows: Int,
    iconSize: Dp,
    gap: Dp,
    fontScale: Float,
): Dp = rowHeight(mode, iconSize, fontScale) * rows + gap * (rows - 1)

internal fun requiredHeight(layout: StatsLayout, rows: Int, fontScale: Float): Dp {
    val padding = layout.outerPadding * 2
    val stack = stackHeight(layout.rowMode, rows, layout.iconSize, layout.rowGap, fontScale)
    if (layout.avatarOnTop) {
        return padding + layout.avatarHeight + stack
    }
    val body = if (layout.showAvatar) maxOf(layout.avatarHeight, stack) else stack
    val footer = if (layout.showFooter) {
        (if (layout.showAvatar) 0.dp else FOOTER_SPACING) + chipHeight(fontScale)
    } else {
        0.dp
    }
    return padding + body + footer
}

private fun compactTiers(cols: Int): List<CompactTier> {
    val inlineMode = when {
        cols >= 5 -> StatRowMode.InlineValueMaxWithLabel
        cols == 4 -> StatRowMode.InlineValueWithLabel
        else -> null
    }
    return buildList {
        if (inlineMode != null) {
            add(CompactTier(inlineMode, COMPACT_OUTER_PADDING, COMPACT_ICON_SIZE, COMPACT_ROW_GAP))
            add(CompactTier(inlineMode, MICRO_OUTER_PADDING, TINY_ICON_SIZE, TINY_ROW_GAP))
        }
        add(CompactTier(StatRowMode.BarOnly, COMPACT_OUTER_PADDING, COMPACT_ICON_SIZE, COMPACT_ROW_GAP))
        add(CompactTier(StatRowMode.BarOnly, COMPACT_OUTER_PADDING, TINY_ICON_SIZE, TINY_ROW_GAP))
        add(CompactTier(StatRowMode.BarOnly, MICRO_OUTER_PADDING, TINY_ICON_SIZE, TINY_ROW_GAP))
        add(CompactTier(StatRowMode.BarOnly, MICRO_OUTER_PADDING, MICRO_ICON_SIZE, TINY_ROW_GAP))
    }
}

internal fun pickLayout(width: Dp, height: Dp, rows: Int, fontScale: Float): StatsLayout {
    val cols = when {
        width >= 310.dp -> 5
        width >= 240.dp -> 4
        width >= 170.dp -> 3
        else -> 2
    }

    if (height < TALL_THRESHOLD) {
        val tiers = compactTiers(cols)
        val tier = tiers.firstOrNull {
            height - it.padding * 2 >=
                stackHeight(it.rowMode, rows, it.iconSize, it.gap, fontScale)
        } ?: tiers.last()
        return StatsLayout(
            cols = cols,
            tall = false,
            rowMode = tier.rowMode,
            showAvatar = false,
            avatarOnTop = false,
            showFooter = false,
            barsFillHeight = false,
            avatarHeight = 0.dp,
            iconSize = tier.iconSize,
            rowGap = tier.gap,
            outerPadding = tier.padding,
        )
    }

    val budget = height - OUTER_PADDING * 2
    val chip = chipHeight(fontScale)
    val stackedStack = stackHeight(
        StatRowMode.LabelStackedValue, rows, TALL_ICON_SIZE, STACKED_ROW_GAP, fontScale,
    )
    val barStack = stackHeight(StatRowMode.BarOnly, rows, TALL_ICON_SIZE, BAR_ROW_GAP, fontScale)

    val avatarBudget = budget - chip
    val compactAvatarBudget = budget - barStack
    val showAvatar = when {
        cols >= 5 -> avatarBudget >= maxOf(AVATAR_MIN_HEIGHT, barStack)
        cols == 2 -> compactAvatarBudget >= COMPACT_AVATAR_MIN_HEIGHT
        else -> false
    }
    val avatarOnTop = showAvatar && cols == 2
    val avatarHeight = when {
        avatarOnTop ->
            compactAvatarBudget.coerceIn(COMPACT_AVATAR_MIN_HEIGHT, COMPACT_AVATAR_MAX_HEIGHT)
        showAvatar -> avatarBudget.coerceIn(AVATAR_MIN_HEIGHT, AVATAR_MAX_HEIGHT)
        else -> 0.dp
    }

    val footerBudget = budget - chip - FOOTER_SPACING
    val richRows = cols >= 4 &&
        if (showAvatar) avatarBudget >= stackedStack else footerBudget >= stackedStack
    val rowMode = if (richRows) StatRowMode.LabelStackedValue else StatRowMode.BarOnly
    val showFooter = when {
        showAvatar -> !avatarOnTop
        cols >= 4 -> footerBudget >= barStack
        else -> false
    }
    val barsFillHeight = rowMode == StatRowMode.BarOnly && !showAvatar && !showFooter
    val iconSize = if (
        barsFillHeight &&
        budget >= stackHeight(rowMode, rows, FILL_ICON_SIZE, BAR_ROW_GAP, fontScale)
    ) {
        FILL_ICON_SIZE
    } else {
        TALL_ICON_SIZE
    }
    val rowGap = if (richRows) STACKED_ROW_GAP else BAR_ROW_GAP

    return StatsLayout(
        cols = cols,
        tall = true,
        rowMode = rowMode,
        showAvatar = showAvatar,
        avatarOnTop = avatarOnTop,
        showFooter = showFooter,
        barsFillHeight = barsFillHeight,
        avatarHeight = avatarHeight,
        iconSize = iconSize,
        rowGap = rowGap,
        outerPadding = OUTER_PADDING,
    )
}
