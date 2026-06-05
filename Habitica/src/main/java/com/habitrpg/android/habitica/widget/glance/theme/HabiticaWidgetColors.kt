package com.habitrpg.android.habitica.widget.glance.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.glance.material3.ColorProviders
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.R

private val LightColors = lightColorScheme(
    primary = Color(0xFF925CF3),
    onPrimary = Color.White,
    secondary = Color(0xFF6133B4),
    onSecondary = Color.White,
    background = Color(0xFFF9F9F9),
    onBackground = Color(0xFF686274),
    surface = Color(0xFFF9F9F9),
    onSurface = Color(0xFF686274),
    surfaceVariant = Color(0xFFC3C0C7),
    onSurfaceVariant = Color(0xFF878190),
    error = Color(0xFFF74E52),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBDA8FF),
    onPrimary = Color(0xFF36205D),
    secondary = Color(0xFF925CF3),
    onSecondary = Color.White,
    background = Color(0xFF2B2020),
    onBackground = Color(0xFFD5C8FF),
    surface = Color(0xFF2B2020),
    onSurface = Color(0xFFD5C8FF),
    surfaceVariant = Color(0xFF434074),
    onSurfaceVariant = Color(0xFFBDA8FF),
    error = Color(0xFFFF6165),
    onError = Color.White,
)

object HabiticaWidgetColorScheme {
    val colors = ColorProviders(light = LightColors, dark = DarkColors)
}

object WidgetColors {
    val background = ColorProvider(R.color.widget_bg)
    val backgroundSecondary = ColorProvider(R.color.widget_bg_secondary)
    val cardBackground = ColorProvider(R.color.widget_card_bg)
    val text = ColorProvider(R.color.widget_text)
    val textSecondary = ColorProvider(R.color.widget_text_secondary)
    val dailiesPurple = ColorProvider(R.color.widget_dailies_purple)
    val progressTrack = ColorProvider(R.color.widget_progress_track)
    val taskListPrimaryText = ColorProvider(R.color.widget_task_list_primary_text)
    val taskListSecondaryText = ColorProvider(R.color.widget_task_list_secondary_text)
    val taskListTaskText = ColorProvider(R.color.widget_task_list_task_text)
    val separator = ColorProvider(R.color.widget_separator)
    val checklistBackground = ColorProvider(R.color.widget_checklist_bg)
    val checklistBackgroundDone = ColorProvider(R.color.widget_checklist_bg_done)
    val addLabelText = ColorProvider(R.color.widget_add_label_text)
    val levelChipBackground = ColorProvider(R.color.widget_level_chip_bg)
    val levelChipText = ColorProvider(R.color.widget_level_chip_text)
    val currencyChipBackground = ColorProvider(R.color.widget_currency_chip_bg)
    val currencyChipText = ColorProvider(R.color.widget_currency_chip_text)
}

object WidgetBarColors {
    val red = Color(0xFFFF6165)
    val orange = Color(0xFFFF944C)
    val yellow = Color(0xFFFFBE5D)
    val green = Color(0xFF24CC8F)
    val teal = Color(0xFF3BCAD7)
    val blue = Color(0xFF50B5E9)
    val purple = Color(0xFF925CF3)
    val gray = Color(0xFF34313A)
}

object TaskValueBucketColors {
    val maroon = Color(0xFFC92B2B)
    val red = Color(0xFFF74E52)
    val orange = Color(0xFFFA8537)
    val yellow = Color(0xFFFFB445)
    val green = Color(0xFF20B780)
    val teal = Color(0xFF34B5C1)
    val blue = Color(0xFF46A7D9)
}

object HabitButtonBarColors {
    val maroonLight = Color(0xFFDE3F3F)
    val maroonMedium = Color(0xFFC92B2B)
    val redLight = Color(0xFFFF6165)
    val redMedium = Color(0xFFF74E52)
    val orangeLight = Color(0xFFFF944C)
    val orangeMedium = Color(0xFFFA8537)
    val yellowLight = Color(0xFFFFBE5D)
    val yellowMedium = Color(0xFFFFA624)
    val greenLight = Color(0xFF24CC8F)
    val greenMedium = Color(0xFF20B780)
    val tealLight = Color(0xFF3BCAD7)
    val tealMedium = Color(0xFF34B5C1)
    val blueLight = Color(0xFF50B5E9)
    val blueMedium = Color(0xFF46A7D9)
}

fun colorForTaskValue(value: Double): Color =
    when {
        value < -20.0 -> TaskValueBucketColors.maroon
        value < -10.0 -> TaskValueBucketColors.red
        value < -1.0 -> TaskValueBucketColors.orange
        value < 1.0 -> TaskValueBucketColors.yellow
        value < 5.0 -> TaskValueBucketColors.green
        value < 10.0 -> TaskValueBucketColors.teal
        else -> TaskValueBucketColors.blue
    }

fun colorForTaskValueLight(value: Double): Color = when {
    value < -20.0 -> HabitButtonBarColors.maroonLight
    value < -10.0 -> HabitButtonBarColors.redLight
    value < -1.0 -> HabitButtonBarColors.orangeLight
    value < 1.0 -> HabitButtonBarColors.yellowLight
    value < 5.0 -> HabitButtonBarColors.greenLight
    value < 10.0 -> HabitButtonBarColors.tealLight
    else -> HabitButtonBarColors.blueLight
}

fun colorForTaskValueMedium(value: Double): Color = when {
    value < -20.0 -> HabitButtonBarColors.maroonMedium
    value < -10.0 -> HabitButtonBarColors.redMedium
    value < -1.0 -> HabitButtonBarColors.orangeMedium
    value < 1.0 -> HabitButtonBarColors.yellowMedium
    value < 5.0 -> HabitButtonBarColors.greenMedium
    value < 10.0 -> HabitButtonBarColors.tealMedium
    else -> HabitButtonBarColors.blueMedium
}

fun colorForHabitValueLight(value: Double): Color = when {
    value < -20.0 -> HabitButtonBarColors.maroonLight
    value < -10.0 -> HabitButtonBarColors.redLight
    value < -1.0 -> HabitButtonBarColors.orangeLight
    value < 1.0 -> HabitButtonBarColors.yellowLight
    value < 5.0 -> HabitButtonBarColors.greenLight
    value < 10.0 -> HabitButtonBarColors.tealLight
    else -> HabitButtonBarColors.blueLight
}

fun colorForHabitValueMedium(value: Double): Color = when {
    value < -20.0 -> HabitButtonBarColors.maroonMedium
    value < -10.0 -> HabitButtonBarColors.redMedium
    value < -1.0 -> HabitButtonBarColors.orangeMedium
    value < 1.0 -> HabitButtonBarColors.yellowMedium
    value < 5.0 -> HabitButtonBarColors.greenMedium
    value < 10.0 -> HabitButtonBarColors.tealMedium
    else -> HabitButtonBarColors.blueMedium
}
