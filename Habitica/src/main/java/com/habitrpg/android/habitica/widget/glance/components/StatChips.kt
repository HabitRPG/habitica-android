package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
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
) {
    val classBitmap = when (className) {
        "warrior" -> runCatching { HabiticaIconsHelper.imageOfWarriorLightBg() }.getOrNull()
        "wizard" -> runCatching { HabiticaIconsHelper.imageOfMageLightBg() }.getOrNull()
        "healer" -> runCatching { HabiticaIconsHelper.imageOfHealerLightBg() }.getOrNull()
        "rogue" -> runCatching { HabiticaIconsHelper.imageOfRogueLightBg() }.getOrNull()
        else -> null
    }
    val labelText = if (showFullLabel) "Level $level" else "Lv. $level"

    Row(
        modifier = modifier
            .height(30.dp)
            .cornerRadius(15.dp)
            .background(backgroundColor)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(GlanceModifier.defaultWeight())
        if (classBitmap != null) {
            Image(
                provider = ImageProvider(classBitmap),
                contentDescription = null,
                modifier = GlanceModifier.size(18.dp),
            )
            Spacer(GlanceModifier.width(6.dp))
        }
        Text(
            text = labelText,
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
    iconResId: Int,
    text: String,
    modifier: GlanceModifier = GlanceModifier,
    backgroundColor: ColorProvider = WidgetColors.currencyChipBackground,
    textColor: ColorProvider = WidgetColors.currencyChipText,
) {
    Row(
        modifier = modifier
            .height(30.dp)
            .cornerRadius(15.dp)
            .background(backgroundColor)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(iconResId),
            contentDescription = null,
            modifier = GlanceModifier.size(18.dp),
        )
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = text,
            style = TextStyle(
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}
