package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors

@Composable
fun LevelChip(
    level: Int,
    className: String?,
    showFullLabel: Boolean,
    modifier: GlanceModifier = GlanceModifier,
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
            .height(24.dp)
            .background(ImageProvider(R.drawable.widget_chip_level))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (classBitmap != null) {
            Image(
                provider = ImageProvider(classBitmap),
                contentDescription = null,
                modifier = GlanceModifier.size(14.dp),
            )
            Spacer(GlanceModifier.width(6.dp))
        }
        Text(
            text = labelText,
            style = TextStyle(
                color = WidgetColors.levelChipText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
fun CurrencyChip(
    iconResId: Int,
    text: String,
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(
        modifier = modifier
            .height(24.dp)
            .background(ImageProvider(R.drawable.widget_chip_currency))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(iconResId),
            contentDescription = null,
            modifier = GlanceModifier.size(14.dp),
        )
        Spacer(GlanceModifier.width(4.dp))
        Text(
            text = text,
            style = TextStyle(
                color = WidgetColors.currencyChipText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}
