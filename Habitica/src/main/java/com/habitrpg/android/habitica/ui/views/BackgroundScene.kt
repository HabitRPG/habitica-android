package com.habitrpg.android.habitica.ui.views

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.habitrpg.android.habitica.R
import java.util.Calendar

@Composable
private fun getBackgroundPainter(): ImageBitmap {
    val calendar = Calendar.getInstance()
    val month = calendar.get(Calendar.MONTH)
    return ImageBitmap.imageResource(
        when (month) {
            Calendar.JANUARY -> R.drawable.stable_tile_janurary
            Calendar.FEBRUARY -> R.drawable.stable_tile_february
            Calendar.MARCH -> R.drawable.stable_tile_march
            Calendar.APRIL -> R.drawable.stable_tile_april
            Calendar.MAY -> R.drawable.stable_tile_may
            Calendar.JUNE -> R.drawable.stable_tile_june
            Calendar.JULY -> R.drawable.stable_tile_july
            Calendar.AUGUST -> R.drawable.stable_tile_august
            Calendar.SEPTEMBER -> R.drawable.stable_tile_september
            Calendar.OCTOBER -> R.drawable.stable_tile_october
            Calendar.NOVEMBER -> R.drawable.stable_tile_november
            Calendar.DECEMBER -> R.drawable.stable_tile_december
            else -> R.drawable.stable_tile_may
        },
    )
}

@Composable
fun BackgroundScene(modifier: Modifier = Modifier) {
    val image = getBackgroundPainter()
    Canvas(
        modifier =
            modifier
                .height(124.dp)
                .fillMaxWidth()
                .zIndex(1f),
        onDraw = {
            val bitmap =
                Bitmap.createScaledBitmap(
                    image.asAndroidBitmap(),
                    image.width.dp.roundToPx(),
                    124.dp.roundToPx(),
                    false,
                )
            val paint =
                Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    shader =
                        ImageShader(
                            bitmap.asImageBitmap(),
                            TileMode.Repeated,
                            TileMode.Repeated,
                        )
                }
            drawIntoCanvas {
                it.nativeCanvas.drawPaint(paint)
            }
            paint.reset()
        },
    )
}
