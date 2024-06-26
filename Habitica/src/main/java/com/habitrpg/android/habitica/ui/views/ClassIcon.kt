package com.habitrpg.android.habitica.ui.views

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun ClassIcon(
    className: String?,
    hasClass: Boolean,
    modifier: Modifier = Modifier
) {
    if (hasClass) {
        val icon =
            when (className) {
                "warrior" -> HabiticaIconsHelper.imageOfWarriorLightBg().asImageBitmap()
                "wizard" -> HabiticaIconsHelper.imageOfMageLightBg().asImageBitmap()
                "healer" -> HabiticaIconsHelper.imageOfHealerLightBg().asImageBitmap()
                "rogue" -> HabiticaIconsHelper.imageOfRogueLightBg().asImageBitmap()
                else -> null
            }
        if (icon != null) {
            Image(bitmap = icon, "", modifier = modifier)
        }
    }
}
