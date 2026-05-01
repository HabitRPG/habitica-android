package com.habitrpg.android.habitica.widget.glance.widgets

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.PreviewSizeMode
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.widget.glance.actions.openAppAction
import com.habitrpg.android.habitica.widget.glance.components.AddTaskTile
import com.habitrpg.android.habitica.widget.glance.theme.AddTaskTileColors
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme

class AddTaskMultiGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(PREVIEW_BUCKETS)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            HabiticaWidgetTheme {
                AddTaskMultiContent()
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            HabiticaWidgetTheme {
                AddTaskMultiContent()
            }
        }
    }

    companion object {
        val PREVIEW_BUCKETS = setOf(
            DpSize(250.dp, 60.dp),
            DpSize(250.dp, 140.dp),
        )
        internal val TALL_THRESHOLD = 130.dp
        internal val OUTER_PADDING = 8.dp
        internal val TILE_GAP = 8.dp
    }
}

private data class AddTile(
    val label: String,
    val iconResId: Int,
    val backgroundColor: androidx.compose.ui.graphics.Color,
    val deepLink: String,
)

private val ADD_TILES = listOf(
    AddTile("Habit", R.drawable.widget_add_habit_glyph, AddTaskTileColors.habit, "habitica://user/tasks/habit/add"),
    AddTile("Daily", R.drawable.widget_add_daily_glyph, AddTaskTileColors.daily, "habitica://user/tasks/daily/add"),
    AddTile("To Do", R.drawable.widget_add_todo_glyph, AddTaskTileColors.todo, "habitica://user/tasks/todo/add"),
    AddTile("Reward", R.drawable.widget_add_reward_glyph, AddTaskTileColors.reward, "habitica://user/tasks/reward/add"),
)

private data class TilePalette(
    val widgetBackground: ColorProvider,
    val tileBackground: ColorProvider,
    val iconTint: ColorProvider?,
)

private val MaterialYouEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
private fun rememberPalette(brandColor: androidx.compose.ui.graphics.Color): TilePalette {
    return if (MaterialYouEnabled) {
        TilePalette(
            widgetBackground = GlanceTheme.colors.background,
            tileBackground = GlanceTheme.colors.primaryContainer,
            iconTint = GlanceTheme.colors.onPrimaryContainer,
        )
    } else {
        TilePalette(
            widgetBackground = ColorProvider(R.color.widget_bg),
            tileBackground = ColorProvider(brandColor),
            iconTint = null,
        )
    }
}

@Composable
private fun AddTaskMultiContent() {
    val size = LocalSize.current
    if (size.height >= AddTaskMultiGlanceWidget.TALL_THRESHOLD) {
        GridLayout(size.width, size.height)
    } else {
        RowLayout(size.width)
    }
}

@Composable
private fun RowLayout(widthAvailable: androidx.compose.ui.unit.Dp) {
    val outer = AddTaskMultiGlanceWidget.OUTER_PADDING
    val gap = AddTaskMultiGlanceWidget.TILE_GAP
    val tileWidth = ((widthAvailable - outer * 2 - gap * (ADD_TILES.size - 1)) / ADD_TILES.size)
        .coerceAtLeast(32.dp)
    val widgetBackground = rememberPalette(ADD_TILES.first().backgroundColor).widgetBackground

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(widgetBackground)
            .padding(outer),
    ) {
        ADD_TILES.forEachIndexed { index, tile ->
            if (index > 0) Spacer(GlanceModifier.width(gap).fillMaxHeight())
            val palette = rememberPalette(tile.backgroundColor)
            AddTaskTile(
                label = tile.label,
                iconResId = tile.iconResId,
                backgroundColor = palette.tileBackground,
                iconTint = palette.iconTint,
                onClick = openAppAction(tile.deepLink),
                modifier = GlanceModifier.width(tileWidth).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun GridLayout(
    widthAvailable: androidx.compose.ui.unit.Dp,
    heightAvailable: androidx.compose.ui.unit.Dp,
) {
    val outer = AddTaskMultiGlanceWidget.OUTER_PADDING
    val gap = AddTaskMultiGlanceWidget.TILE_GAP
    val columnWidth = ((widthAvailable - outer * 2 - gap) / 2).coerceAtLeast(48.dp)
    val rowHeight = ((heightAvailable - outer * 2 - gap) / 2).coerceAtLeast(48.dp)
    val widgetBackground = rememberPalette(ADD_TILES.first().backgroundColor).widgetBackground

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(widgetBackground)
            .padding(outer),
    ) {
        GridColumn(
            top = ADD_TILES[0],
            bottom = ADD_TILES[2],
            columnWidth = columnWidth,
            rowHeight = rowHeight,
            gap = gap,
        )
        Spacer(GlanceModifier.width(gap).fillMaxHeight())
        GridColumn(
            top = ADD_TILES[1],
            bottom = ADD_TILES[3],
            columnWidth = columnWidth,
            rowHeight = rowHeight,
            gap = gap,
        )
    }
}

@Composable
private fun GridColumn(
    top: AddTile,
    bottom: AddTile,
    columnWidth: androidx.compose.ui.unit.Dp,
    rowHeight: androidx.compose.ui.unit.Dp,
    gap: androidx.compose.ui.unit.Dp,
) {
    val topPalette = rememberPalette(top.backgroundColor)
    val bottomPalette = rememberPalette(bottom.backgroundColor)
    Column(modifier = GlanceModifier.width(columnWidth).fillMaxHeight()) {
        AddTaskTile(
            label = top.label,
            iconResId = top.iconResId,
            backgroundColor = topPalette.tileBackground,
            iconTint = topPalette.iconTint,
            onClick = openAppAction(top.deepLink),
            modifier = GlanceModifier.fillMaxWidth().height(rowHeight),
        )
        Spacer(GlanceModifier.height(gap).fillMaxWidth())
        AddTaskTile(
            label = bottom.label,
            iconResId = bottom.iconResId,
            backgroundColor = bottomPalette.tileBackground,
            iconTint = bottomPalette.iconTint,
            onClick = openAppAction(bottom.deepLink),
            modifier = GlanceModifier.fillMaxWidth().height(rowHeight),
        )
    }
}
