package com.habitrpg.android.habitica.widget.glance.widgets

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

internal val TASK_ROW_MIN_HEIGHT = 48.dp
internal val TASK_ROW_MAX_HEIGHT = 144.dp
internal val TASK_ROW_CHROME = 6.4.dp
internal val TASK_ROW_CORNER_RADIUS = 17.5.dp
internal val TASK_ROW_SPACING = 6.dp
internal val VIEW_MORE_HEIGHT = 28.dp

internal val TASK_TILE_WIDTH = 44.dp
internal val TASK_TEXT_START_GAP = 10.dp
internal val TASK_TEXT_END_GAP = 12.dp
internal val CHECKLIST_CHIP_GAP = 8.dp
internal val CHECKLIST_CHIP_PADDING = 8.dp

internal const val TASK_TEXT_SP = 16f
internal const val CHECKLIST_TEXT_SP = 12f
internal const val TASK_BASE_MAX_LINES = 2
internal const val MAX_TASK_ROW_SCALE = 3f

private const val LINE_FIT_EPSILON = 0.02f
private const val TEXT_WIDTH_TOLERANCE = 1.04f
private const val FULL_WIDTH_CODE_POINT = 0x2E80

private const val NARROW_CHARS = "ilI!|.,:;'`"
private const val SEMI_NARROW_CHARS = "ftjr()[]{}-"
private const val WIDE_CHARS = "mwMW@"

internal data class TaskRowMetrics(
    val height: Dp,
    val maxLines: Int,
)

internal fun taskRowMetrics(
    text: String,
    rowWidth: Dp,
    checklistDone: Int,
    checklistTotal: Int,
    fontScale: Float,
): TaskRowMetrics {
    val scale = maxOf(1f, fontScale)
    val lineHeight = (TASK_TEXT_SP * LINE_HEIGHT_FACTOR * scale).dp
    val lineCap =
        (TASK_BASE_MAX_LINES * scale)
            .roundToInt()
            .coerceIn(1, linesFitting(TASK_ROW_MAX_HEIGHT, lineHeight))
    val needed =
        estimateLineCount(
            text = text,
            maxWidth = taskTextWidth(rowWidth, checklistDone, checklistTotal, scale),
            em = TASK_TEXT_SP * scale,
            cap = lineCap,
        )
    val lines = maxOf(needed, linesFitting(TASK_ROW_MIN_HEIGHT, lineHeight)).coerceIn(1, lineCap)
    return TaskRowMetrics(
        height = maxOf(TASK_ROW_MIN_HEIGHT, lineHeight * lines + TASK_ROW_CHROME),
        maxLines = lines,
    )
}

internal fun viewMoreHeight(fontScale: Float): Dp = VIEW_MORE_HEIGHT * maxOf(1f, fontScale).coerceAtMost(MAX_TASK_ROW_SCALE)

private fun linesFitting(
    height: Dp,
    lineHeight: Dp,
): Int = ((height - TASK_ROW_CHROME) / lineHeight + LINE_FIT_EPSILON).toInt().coerceAtLeast(1)

private fun taskTextWidth(
    rowWidth: Dp,
    checklistDone: Int,
    checklistTotal: Int,
    scale: Float,
): Dp {
    val chip =
        if (checklistTotal > 0) {
            CHECKLIST_CHIP_GAP + CHECKLIST_CHIP_PADDING +
                textWidth("$checklistDone/$checklistTotal", CHECKLIST_TEXT_SP * scale).dp
        } else {
            0.dp
        }
    return rowWidth - TASK_TILE_WIDTH - TASK_TEXT_START_GAP - TASK_TEXT_END_GAP - chip
}

private fun estimateLineCount(
    text: String,
    maxWidth: Dp,
    em: Float,
    cap: Int,
): Int {
    val limit = maxWidth.value
    if (cap <= 1) return 1
    if (limit <= 0f) return cap
    val spaceWidth = charWidthEm(' ') * em
    var lines = 1
    var used = 0f
    for ((index, paragraph) in text.split('\n').withIndex()) {
        if (index > 0) {
            lines++
            if (lines >= cap) return cap
            used = 0f
        }
        for (word in paragraph.split(' ')) {
            if (word.isEmpty()) continue
            var width = textWidth(word, em)
            val advance = if (used > 0f) width + spaceWidth else width
            if (used + advance <= limit) {
                used += advance
                continue
            }
            if (used > 0f) {
                lines++
                if (lines >= cap) return cap
            }
            while (width > limit) {
                width -= limit
                lines++
                if (lines >= cap) return cap
            }
            used = width
        }
    }
    return lines
}

private fun textWidth(
    text: String,
    em: Float,
): Float {
    var total = 0f
    for (char in text) {
        total += charWidthEm(char)
    }
    return total * em * TEXT_WIDTH_TOLERANCE
}

private fun charWidthEm(char: Char): Float =
    when {
        char == ' ' -> 0.26f
        char in NARROW_CHARS -> 0.27f
        char in SEMI_NARROW_CHARS -> 0.36f
        char in WIDE_CHARS -> 0.86f
        char.isLowSurrogate() -> 0f
        char.isHighSurrogate() -> 1.2f
        char.code >= FULL_WIDTH_CODE_POINT -> 1f
        char.isDigit() || char.isUpperCase() -> 0.63f
        else -> 0.54f
    }
