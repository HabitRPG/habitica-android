package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors

@Composable
fun LevelChip(
    level: Int,
    className: String?,
    showFullLabel: Boolean,
    modifier: GlanceModifier = GlanceModifier,
    backgroundColor: ColorProvider = WidgetColors.levelChipBackground,
    textColor: ColorProvider = WidgetColors.levelChipText,
    horizontalPadding: Dp = 8.dp,
) {
    val classBitmap = when (className) {
        "warrior" -> runCatching { HabiticaIconsHelper.imageOfWarriorLightBg() }.getOrNull()
        "wizard" -> runCatching { HabiticaIconsHelper.imageOfMageLightBg() }.getOrNull()
        "healer" -> runCatching { HabiticaIconsHelper.imageOfHealerLightBg() }.getOrNull()
        "rogue" -> runCatching { HabiticaIconsHelper.imageOfRogueLightBg() }.getOrNull()
        else -> null
    }
    val labelText = stringRes(
        if (showFullLabel) R.string.user_level_long else R.string.widget_level_short,
        level,
    )

    Row(
        modifier = modifier
            .cornerRadius(15.dp)
            .background(backgroundColor)
            .padding(horizontal = horizontalPadding, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(GlanceModifier.defaultWeight().height(22.dp))
        if (classBitmap != null) {
            Image(
                provider = ImageProvider(classBitmap),
                contentDescription = null,
                modifier = GlanceModifier.size(22.dp),
            )
            Spacer(GlanceModifier.width(6.dp))
        }
        Text(
            text = labelText,
            maxLines = 1,
            style = TextStyle(
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(GlanceModifier.defaultWeight())
    }
}

@Composable
fun CurrencyChip(
    iconProvider: androidx.glance.ImageProvider,
    text: String,
    modifier: GlanceModifier = GlanceModifier,
    backgroundColor: ColorProvider = WidgetColors.currencyChipBackground,
    textColor: ColorProvider = WidgetColors.currencyChipText,
) {
    Row(
        modifier = modifier
            .cornerRadius(15.dp)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = iconProvider,
            contentDescription = null,
            modifier = GlanceModifier.size(18.dp),
        )
        Spacer(GlanceModifier.width(4.dp))
        Text(
            text = text,
            maxLines = 1,
            style = TextStyle(
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

data class CurrencyChipItem(
    val iconProvider: androidx.glance.ImageProvider,
    val text: String,
)

@Composable
fun MergedCurrencyChip(
    items: List<CurrencyChipItem>,
    modifier: GlanceModifier = GlanceModifier,
    backgroundColor: ColorProvider = WidgetColors.currencyChipBackground,
    textColor: ColorProvider = WidgetColors.currencyChipText,
) {
    Row(
        modifier = modifier
            .cornerRadius(15.dp)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = if (index > 0) GlanceModifier.padding(start = 10.dp) else GlanceModifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    provider = item.iconProvider,
                    contentDescription = null,
                    modifier = GlanceModifier.size(18.dp),
                )
                Spacer(GlanceModifier.width(4.dp))
                Text(
                    text = item.text,
                    maxLines = 1,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}
