package com.habitrpg.android.habitica.ui.views

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.common.habitica.helpers.NumberAbbreviator
import java.text.NumberFormat

@Composable
fun LabeledBar(
    modifier: Modifier = Modifier,
    icon: Bitmap? = null,
    label: String? = null,
    color: Color = colorResource(R.color.brand),
    barColor: Color = HabiticaTheme.colors.windowBackground,
    value: Double,
    maxValue: Double,
    displayCompact: Boolean = false,
    barHeight: Dp = 8.dp,
    disabled: Boolean = false,
    abbreviateValue: Boolean = true,
    abbreviateMax: Boolean = true,
    animated: Boolean = true
) {
    val cleanedMaxValue = java.lang.Double.max(1.0, maxValue)

    val animatedValue = if (animated) {
        animateFloatAsState(
            targetValue = value.toFloat(),
            animationSpec = spring()
        ).value
    } else {
        value.toFloat()
    }
    val formatter = NumberFormat.getNumberInstance()
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 2

    val animatedPadding = animateDpAsState(
        targetValue = if (displayCompact) {
            0.dp
        } else {
            24.dp
        }
    )

    Box(
        modifier = modifier.alpha(if (disabled) 0.5f else 1.0f)
    ) {
        icon?.let {
            AnimatedVisibility(
                visible = !displayCompact,
                enter = fadeIn() + slideInHorizontally { -18 },
                exit = fadeOut() + slideOutHorizontally { -18 },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Image(
                    it.asImageBitmap(),
                    null,
                    modifier = Modifier
                )
            }
        }
        Column(modifier = Modifier.padding(start = animatedPadding.value)) {
            LinearProgressIndicator(
                progress = (animatedValue / cleanedMaxValue).toFloat(),
                Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .height(barHeight),
                backgroundColor = barColor,
                color = color
            )
            AnimatedVisibility(visible = !displayCompact) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (!disabled) {
                        val currentValueText = if (abbreviateValue) {
                            NumberAbbreviator.abbreviate(
                                LocalContext.current,
                                animatedValue,
                                0
                            )
                        } else {
                            formatter.format(animatedValue)
                        }
                        val maxValueText = if (abbreviateMax) {
                            NumberAbbreviator.abbreviate(
                                LocalContext.current,
                                cleanedMaxValue,
                                0
                            )
                        } else {
                            formatter.format(cleanedMaxValue)
                        }
                        Text(
                            "$currentValueText / $maxValueText",
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
    var compact: Boolean by remember { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .width(240.dp)
            .padding(8.dp)
            .clickable {
                compact = !compact
            }
    ) {
        LabeledBar(
            icon = HabiticaIconsHelper.imageOfHeartLightBg(),
            label = stringResource(id = R.string.health),
            color = colorResource(R.color.hpColor),
            value = 10.0,
            maxValue = 50.0,
            displayCompact = compact
        )
        LabeledBar(
            icon = HabiticaIconsHelper.imageOfExperience(),
            label = stringResource(id = R.string.XP_default),
            color = colorResource(R.color.xpColor),
            value = 100123.0,
            maxValue = 50000000000000.0,
            displayCompact = compact,
            abbreviateValue = false
        )
        LabeledBar(
            icon = HabiticaIconsHelper.imageOfExperience(),
            label = stringResource(id = R.string.XP_default),
            color = colorResource(R.color.xpColor),
            value = 100123.0,
            maxValue = 500000000000.0,
            displayCompact = compact,
            abbreviateValue = false,
            abbreviateMax = false
        )
        LabeledBar(
            icon = HabiticaIconsHelper.imageOfMagic(),
            label = stringResource(id = R.string.unlock_level, 10),
            color = colorResource(R.color.mpColor),
            value = 10.0,
            maxValue = 5000.0,
            displayCompact = compact,
            disabled = false
        )
    }
}
