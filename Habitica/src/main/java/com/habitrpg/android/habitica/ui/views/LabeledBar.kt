package com.habitrpg.android.habitica.ui.views

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.common.habitica.helpers.NumberAbbreviator

@Composable
fun LabeledBar(
    modifier: Modifier = Modifier,
    icon: Bitmap? = null,
    label: String? = null,
    color: Color = colorResource(R.color.brand),
    barColor: Color = HabiticaTheme.colors.windowBackground,
    value: Double,
    maxValue: Double,
    displayCompact: Boolean,
    disabled: Boolean = false
) {
    val cleanedMaxVlaue = java.lang.Double.max(1.0, maxValue)

    val animatedValue = animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    ).value
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.alpha(if (disabled) 0.5f else 1.0f)
    ) {
        icon?.let {
            AnimatedVisibility(visible = !displayCompact,
                enter = slideInHorizontally { -18 },
                exit = slideOutHorizontally { -18 }) {
                Image(
                    it.asImageBitmap(), null, modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            LinearProgressIndicator(
                progress = (animatedValue / cleanedMaxVlaue).toFloat(),
                Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .height(8.dp),
                backgroundColor = barColor,
                color = color
            )
            AnimatedVisibility(visible = !displayCompact) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (!disabled) {
                        Text(
                            "${NumberAbbreviator.abbreviate(LocalContext.current, animatedValue)} / ${NumberAbbreviator.abbreviate(LocalContext.current, cleanedMaxVlaue)}",
                            fontSize = 12.sp,
                            color = colorResource(R.color.text_ternary)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (label != null) {
                        Text(label, fontSize = 12.sp, color = colorResource(R.color.text_ternary))
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.width(180.dp)) {
        LabeledBar(
            icon = HabiticaIconsHelper.imageOfHeartLightBg(),
            label = stringResource(id = R.string.health),
            color = colorResource(R.color.hpColor),
            value = 10.0,
            maxValue = 50.0,
            displayCompact = false
        )
        LabeledBar(
            icon = HabiticaIconsHelper.imageOfExperience(),
            label = stringResource(id = R.string.XP_default),
            color = colorResource(R.color.xpColor),
            value = 100123.0,
            maxValue = 50000000000000.0,
            displayCompact = false
        )
        LabeledBar(
            icon = HabiticaIconsHelper.imageOfMagic(),
            label = stringResource(id = R.string.unlock_level, 10),
            color = colorResource(R.color.mpColor),
            value = 10.0,
            maxValue = 5000.0,
            displayCompact = false,
            disabled = true
        )
    }
}