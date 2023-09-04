package com.habitrpg.android.habitica.ui.views.stable

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.views.HabiticaButton
import com.habitrpg.android.habitica.ui.views.PixelArtView
import com.habitrpg.common.habitica.helpers.MainNavigationController
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
        }
    )
}

@Composable
private fun getFoodPainter(petColor: String): ImageBitmap {
    return ImageBitmap.imageResource(
        when (petColor) {
            "Base" -> R.drawable.feed_base
            "CottonCandyBlue" -> R.drawable.feed_blue
            "Desert" -> R.drawable.feed_desert
            "Golden" -> R.drawable.feed_golden
            "CottonCandyPink" -> R.drawable.feed_pink
            "Red" -> R.drawable.feed_red
            "Shade" -> R.drawable.feed_shade
            "Skeleton" -> R.drawable.feed_skeleton
            "White" -> R.drawable.feed_white
            "Zombie" -> R.drawable.feed_zombie
            else -> R.drawable.feed_base
        }
    )
}

@Composable
fun PetBottomSheet(
    pet: Pet,
    isCurrentPet: Boolean,
    canRaiseToMount: Boolean,
    ownsSaddles: Boolean,
    onEquip: ((String) -> Unit)?,
    onFeed: ((Pet, Food?) -> Unit)?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 22.dp)
    ) {
        Text(
            pet.text ?: "",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = HabiticaTheme.colors.textTertiary
        )
        val image = getBackgroundPainter()
        Box(
            modifier = Modifier
                .padding(top = 9.dp, bottom = 16.dp)
                .fillMaxWidth()
                .height(124.dp)
                .clip(HabiticaTheme.shapes.medium)
        ) {
            Canvas(
                modifier = Modifier
                    .height(124.dp)
                    .fillMaxWidth()
                    .zIndex(1f), onDraw = {
                    val bitmap = Bitmap.createScaledBitmap(
                        image.asAndroidBitmap(),
                        image.width.dp.roundToPx(),
                        124.dp.roundToPx(),
                        false
                    )
                    val paint = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        shader = ImageShader(
                            bitmap.asImageBitmap(),
                            TileMode.Repeated,
                            TileMode.Repeated
                        )
                    }
                    drawIntoCanvas {
                        it.nativeCanvas.drawPaint(paint)
                    }
                    paint.reset()
                })
            val regularPosition = 33f
            val highJump = 22f
            val lowJump = 30f
            val position by infiniteTransition.animateFloat(
                initialValue = regularPosition,
                targetValue = highJump,
                animationSpec = infiniteRepeatable(animation = keyframes {
                    durationMillis = 6000
                    regularPosition at 0 with LinearOutSlowInEasing
                    highJump at 200 with LinearOutSlowInEasing
                    regularPosition at 400 with FastOutSlowInEasing
                    regularPosition at 1800 with FastOutSlowInEasing
                    lowJump at 1850 with LinearOutSlowInEasing
                    regularPosition at 1900 with LinearOutSlowInEasing
                    regularPosition at 2100 with FastOutSlowInEasing
                    lowJump at 2200 with LinearOutSlowInEasing
                    regularPosition at 2350 with LinearOutSlowInEasing
                    regularPosition at 6000
                }, RepeatMode.Restart, StartOffset(1500))
            )
            PixelArtView(
                imageName = "stable_Pet-${pet.animal}-${pet.color}", modifier = Modifier
                    .offset(0.dp, position.dp)
                    .size(68.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
            )
        }
        if (canRaiseToMount) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                HabiticaButton(
                    HabiticaTheme.colors.windowBackground,
                    HabiticaTheme.colors.textPrimary,
                    onClick = {
                        if (ownsSaddles) {
                            val saddle = Food()
                            saddle.key = "Saddle"
                            onFeed?.invoke(pet, saddle)
                        } else {
                            MainNavigationController.navigate(R.id.marketFragment)
                        }
                        onDismiss()
                    }, modifier = Modifier
                        .weight(1.0f)
                        .heightIn(min = 101.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PixelArtView(
                            ImageBitmap.imageResource(R.drawable.feed_saddle),
                            modifier = Modifier.size(64.dp, 50.dp)
                        )
                        Text(stringResource(id = R.string.use_saddle))
                    }
                }
                HabiticaButton(
                    HabiticaTheme.colors.windowBackground,
                    HabiticaTheme.colors.textPrimary,
                    onClick = {
                        onFeed?.invoke(pet, null)
                        onDismiss()
                    }, modifier = Modifier
                        .weight(1.0f)
                        .heightIn(min = 101.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PixelArtView(
                            getFoodPainter(pet.color),
                            modifier = Modifier.size(64.dp, 50.dp)
                        )
                        Text(stringResource(id = R.string.feed))
                    }
                }
            }
        }
        HabiticaButton(
            background = HabiticaTheme.colors.tintedUiSub,
            color = Color.White,
            contentPadding = PaddingValues(12.dp),
            onClick = {
                onEquip?.invoke(pet.key)
                onDismiss()
            }) {
            if (isCurrentPet) {
                Text(stringResource(id = R.string.unequip))
            } else {
                Text(stringResource(id = R.string.equip))
            }
        }
    }
}
