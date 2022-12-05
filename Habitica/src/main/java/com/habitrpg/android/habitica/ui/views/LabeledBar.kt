package com.habitrpg.android.habitica.ui.views

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import java.math.RoundingMode
import java.text.NumberFormat

@Composable
fun LabeledBar(
    icon: Bitmap,
    label: String,
    color: Color,
    value: Double,
    maxValue: Double,
    displayCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getInstance()
    formatter.maximumFractionDigits = 1
    formatter.roundingMode = RoundingMode.UP
    formatter.isGroupingUsed = true

    val cleanedMaxVlaue = java.lang.Double.max(1.0, maxValue)

    val animatedValue = animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    ).value
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        AnimatedVisibility(
            visible = !displayCompact,
            enter = slideInHorizontally { -18 },
            exit = slideOutHorizontally { -18 }) {
            Image(icon.asImageBitmap(), null, modifier = Modifier.padding(end = 8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            LinearProgressIndicator(
                progress = (animatedValue / cleanedMaxVlaue).toFloat(),
                Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .height(8.dp),
                backgroundColor = colorResource(R.color.window_background),
                color = color
            )
            AnimatedVisibility(visible = !displayCompact) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        "${formatter.format(animatedValue)} / ${formatter.format(cleanedMaxVlaue)}",
                        fontSize = 12.sp,
                        color = colorResource(R.color.text_ternary)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(label, fontSize = 12.sp, color = colorResource(R.color.text_ternary))
                }
            }
        }
    }
}