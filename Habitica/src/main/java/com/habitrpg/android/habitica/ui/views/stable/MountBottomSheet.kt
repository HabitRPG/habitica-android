package com.habitrpg.android.habitica.ui.views.stable

import com.habitrpg.android.habitica.models.inventory.Animal
import com.habitrpg.android.habitica.models.inventory.Mount
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.views.BackgroundScene
import com.habitrpg.android.habitica.ui.views.HabiticaButton
import java.util.Calendar
import kotlin.math.sin

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
fun MountBottomSheet(
    mount: Mount,
    isCurrentMount: Boolean,
    onEquip: ((String) -> Unit)?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 22.dp)
    ) {
        Text(
            mount.text ?: "",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = HabiticaTheme.colors.textTertiary
        )
        Box(
            modifier = Modifier
                .padding(top = 9.dp, bottom = 16.dp)
                .fillMaxWidth()
                .height(124.dp)
                .clip(HabiticaTheme.shapes.medium)
        ) {
            BackgroundScene()

            val regularPosition = 33f
            val highJump = 22f
            val lowJump = 30f
            val position by if (isAnimalFlying(mount)) {
                infiniteTransition.animateFloat(
                    initialValue = 24f,
                    targetValue = 16f,
                    animationSpec = infiniteRepeatable(
                        tween(
                            2500,
                            easing = CubicBezierEasing(0.3f, 0.0f, 0.2f, 1.0f)
                        ), RepeatMode.Reverse
                    ),
                    label = "animalPosition"
                )
            } else {
                infiniteTransition.animateFloat(
                    initialValue = regularPosition,
                    targetValue = highJump,
                    animationSpec = infiniteRepeatable(animation = keyframes {
                        durationMillis = 6000
                        regularPosition at 0 with LinearOutSlowInEasing
                        highJump at 150 with LinearOutSlowInEasing
                        regularPosition at 300 with FastOutSlowInEasing
                        regularPosition at 1800 with FastOutSlowInEasing
                        lowJump at 1850 with LinearOutSlowInEasing
                        regularPosition at 1900 with LinearOutSlowInEasing
                        regularPosition at 2100 with FastOutSlowInEasing
                        lowJump at 2200 with LinearOutSlowInEasing
                        regularPosition at 2350 with LinearOutSlowInEasing
                        regularPosition at 6000
                    }, RepeatMode.Restart, StartOffset(1500)), label = "animalPosition"
                )
            }
            MountView(mount, modifier = Modifier
                    .offset(0.dp, position.dp)
                    .size(68.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
            )
        }
        HabiticaButton(
            background = HabiticaTheme.colors.tintedUiSub,
            color = Color.White,
            contentPadding = PaddingValues(12.dp),
            onClick = {
                onEquip?.invoke(mount.key)
                onDismiss()
            }) {
            if (isCurrentMount) {
                Text(stringResource(id = R.string.unequip))
            } else {
                Text(stringResource(id = R.string.equip))
            }
        }
    }
}

fun isAnimalFlying(animal: Animal): Boolean {
    if (listOf(
            "FlyingPig",
            "Bee"
        ).contains(animal.animal)
    ) return true
    return listOf(
        "Ghost",
        "Cupid",
        "Fairy",
        "SolarSystem",
        "Vampire"
    ).contains(animal.color)
}
