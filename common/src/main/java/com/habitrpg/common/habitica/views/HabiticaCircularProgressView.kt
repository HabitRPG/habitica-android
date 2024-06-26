package com.habitrpg.common.habitica.views

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habitrpg.common.habitica.R

@Composable
fun HabiticaCircularProgressView(
    modifier: Modifier = Modifier,
    partialDisplay: Float = 1f,
    animate: Boolean = true,
    indicatorSize: Dp = 100.dp,
    animationDuration: Int = 4000,
    strokeWidth: Dp = 8.dp
) {
    val rotateAnimation: State<Float>
    if (animate) {
        val infiniteTransition = rememberInfiniteTransition()

        rotateAnimation =
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1080f,
                animationSpec =
                infiniteRepeatable(
                    animation =
                    tween(
                        durationMillis = animationDuration,
                        easing = CubicBezierEasing(0.3f, 0.0f, 0.2f, 1.0f)
                    )
                )
            )
    } else {
        rotateAnimation = remember { mutableFloatStateOf(0f) }
    }
    val backgroundColor = MaterialTheme.colorScheme.surface
    val brush =
        Brush.sweepGradient(
            listOf(
                colorResource(R.color.background_brand),
                colorResource(R.color.background_red),
                colorResource(R.color.background_orange),
                colorResource(R.color.background_yellow),
                colorResource(R.color.background_green),
                colorResource(R.color.background_blue),
                colorResource(R.color.background_brand)
            )
        )
    Canvas(
        modifier =
        modifier
            .rotate(-90f)
            .size(size = indicatorSize - (strokeWidth * 2))
            .padding(strokeWidth / 2)
    ) {
        rotate(rotateAnimation.value) {
            drawCircle(
                brush = brush,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        if (partialDisplay < 1f) {
            drawArc(
                color = backgroundColor,
                startAngle = (360f * partialDisplay),
                sweepAngle = 360f - (360f * partialDisplay),
                useCenter = true,
                style = Stroke(width = strokeWidth.toPx() * 1.4f, cap = StrokeCap.Square, join = StrokeJoin.Miter, miter = 2f)
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        HabiticaCircularProgressView()
        HabiticaCircularProgressView(indicatorSize = 40.dp, strokeWidth = 5.dp)
        HabiticaCircularProgressView(partialDisplay = 0.3f, indicatorSize = 32.dp, strokeWidth = 4.dp)
        HabiticaCircularProgressView(partialDisplay = 0.91f, indicatorSize = 32.dp, strokeWidth = 4.dp)
    }
}
