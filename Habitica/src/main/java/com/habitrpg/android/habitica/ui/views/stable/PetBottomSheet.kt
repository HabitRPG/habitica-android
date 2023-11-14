package com.habitrpg.android.habitica.ui.views.stable

import android.graphics.Bitmap
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.interactors.ShareMountUseCase
import com.habitrpg.android.habitica.interactors.SharePetUseCase
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.views.BackgroundScene
import com.habitrpg.android.habitica.ui.views.HabiticaButton
import com.habitrpg.android.habitica.ui.views.PixelArtView
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlin.math.sin

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
    trained: Int,
    isCurrentPet: Boolean,
    canRaiseToMount: Boolean,
    ownsSaddles: Boolean,
    onEquip: ((String) -> Unit)?,
    onFeed: (suspend (Pet, Food?) -> FeedResponse?)?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val coroutineScope = rememberCoroutineScope()

    var oldFeedValue: Int by remember { mutableIntStateOf(0) }
    var feedValue: Int by remember { mutableIntStateOf(0) }
    var feedMessage: String by remember { mutableStateOf("") }
    var showFeedResponse: Boolean by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = pet, block = {
        feedValue = trained
    })

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
        Box(
            modifier = Modifier
                .padding(top = 9.dp, bottom = 16.dp)
                .fillMaxWidth()
                .height(124.dp)
                .clip(HabiticaTheme.shapes.medium)
        ) {
            BackgroundScene()

            this@Column.AnimatedVisibility(
                visible = showFeedResponse, modifier = Modifier
                    .offset(y = 90.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(4f), enter = fadeIn(), exit = fadeOut()
            ) {
                Text(
                    feedMessage,
                    color = HabiticaTheme.colors.textPrimary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .background(
                            HabiticaTheme.colors.windowBackground,
                            HabiticaTheme.shapes.medium
                        )
                        .padding(8.dp, 3.dp)
                        .alpha(0.65f)
                )
            }

            this@Column.AnimatedVisibility(
                visible = showFeedResponse,
                modifier = Modifier
                    .offset(y = 6.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(4f), enter = fadeIn() + scaleIn(), exit = fadeOut()
            ) {
                val progressAnimation = animateFloatAsState(
                    targetValue = feedValue / 50f,
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                )
                val scale = (sin((progressAnimation.value - oldFeedValue) * 120) * 0.02f)
                LinearProgressIndicator(
                    progress = progressAnimation.value,
                    color = HabiticaTheme.colors.successColor,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .width(200.dp)
                        .scale(1.0f + scale)
                        .background(
                            HabiticaTheme.colors.windowBackground,
                            HabiticaTheme.shapes.medium
                        )
                        .padding(3.dp)
                )
            }

            val regularPosition = 44f
            val highJump = 32f
            val midJump = 37f
            val lowJump = 40f
            val position by if (showFeedResponse) {
                    infiniteTransition.animateFloat(
                        initialValue = regularPosition,
                        targetValue = highJump,
                        animationSpec = infiniteRepeatable(animation = keyframes {
                            durationMillis = 800
                            regularPosition at 0 with FastOutSlowInEasing
                            lowJump at 50 with LinearOutSlowInEasing
                            regularPosition at 100 with LinearOutSlowInEasing
                            regularPosition at 300 with FastOutSlowInEasing
                            midJump at 400 with LinearOutSlowInEasing
                            regularPosition at 550 with LinearOutSlowInEasing
                            regularPosition at 800
                        }, RepeatMode.Restart, StartOffset(1500)), label = "animalPosition"
                    )
                } else if (isAnimalFlying(pet)) {
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
                    Color(LocalContext.current.getThemeColor(R.attr.colorTintedBackgroundOffset)),
                    HabiticaTheme.colors.textPrimary,
                    onClick = {
                        if (ownsSaddles) {
                            val saddle = Food()
                            saddle.key = "Saddle"
                            coroutineScope.launchCatching {
                                onFeed?.invoke(pet, saddle)
                            }
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
                    Color(LocalContext.current.getThemeColor(R.attr.colorTintedBackgroundOffset)),
                    HabiticaTheme.colors.textPrimary,
                    onClick = {
                        coroutineScope.launchCatching {
                            val response = onFeed?.invoke(pet, null)
                            feedMessage = response?.message ?: ""
                            showFeedResponse = true
                            delay(700)
                            oldFeedValue = feedValue
                            feedValue = if (response?.value == -1) 50 else (response?.value ?: feedValue)

                            delay(1800)
                            showFeedResponse = false
                            if (response?.value == -1) {
                                onDismiss()
                            }
                        }
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
        val context = LocalContext.current
        HabiticaButton(
            background = HabiticaTheme.colors.tintedUiSub,
            color = Color.White,
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier.padding(bottom = 16.dp),
            onClick = {
                MainScope().launchCatching {
                    SharePetUseCase().callInteractor(
                        SharePetUseCase.RequestValues(
                            pet.key,
                            "",
                            context
                        ))
                }
                onDismiss()
            }) {
            Text(stringResource(id = R.string.share))
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

fun isAnimalFlying(pet: Pet): Boolean {
    if (listOf(
            "FlyingPig",
            "Bee"
        ).contains(pet.animal)
    ) return true
    return listOf(
        "Ghost",
        "Cupid",
        "Fairy",
        "SolarSystem",
        "Vampire"
    ).contains(pet.color)
}
