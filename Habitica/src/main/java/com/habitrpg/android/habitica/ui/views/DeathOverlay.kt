package com.habitrpg.android.habitica.ui.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import android.content.SharedPreferences
import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.getShortRemainingString
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.extensions.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import kotlin.math.sqrt

@Composable
fun DeathOverlay(
    isVisible: Boolean,
    user: User?,
    appConfigManager: AppConfigManager,
    sharedPreferences: SharedPreferences,
    onSubscribeClick: () -> Unit = {},
    onUseSecondChanceClick: () -> Unit = {},
    onRefillHealthClick: () -> Unit = {},
    onAnimationComplete: () -> Unit = {},
    onDismissComplete: () -> Unit = {}
) {
    if (!isVisible) return

    val userLevel = user?.stats?.lvl?.toInt()
    val userGold = user?.stats?.gp?.toInt()
    val isSubscribed = appConfigManager.enableFaintSubs() && user?.isSubscribed == true

    var hasUsedSecondChance by remember(user) {
        mutableStateOf(false)
    }
    var timeUntilRecharge by remember(user) {
        mutableStateOf<String?>(null)
    }

    var isDismissing by remember { mutableStateOf(false) }
    var isRefilling by remember { mutableStateOf(false) }
    var isUsingSecondChance by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    BackHandler(enabled = isVisible && !isDismissing) {
        (context as? android.app.Activity)?.moveTaskToBack(true)
    }
    
    val useAltText = appConfigManager.showAltDeathText()
    val lossDescriptionStringId = if (useAltText) {
        R.string.faint_loss_description_alt
    } else {
        R.string.faint_loss_description
    }

    LaunchedEffect(user, isSubscribed) {
        if (isSubscribed) {
            val lastRevive = Date(sharedPreferences.getLong("last_sub_revive", 0L))
            val usedToday = DateUtils.isSameDay(Date(), lastRevive)
            hasUsedSecondChance = usedToday

            if (usedToday) {
                val midnight = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    add(Calendar.DAY_OF_MONTH, 1)
                }.time

                while (hasUsedSecondChance) {
                    timeUntilRecharge = midnight.getShortRemainingString()
                    delay(1000L)
                }
            } else {
                timeUntilRecharge = null
            }
        } else {
            hasUsedSecondChance = false
            timeUntilRecharge = null
        }
    }

    val circleProgress = remember { Animatable(0f) }
    val waveProgress = remember { Animatable(0f) }
    val ghostHeartProgress = remember { Animatable(0f) }
    val ghostHeartScale = remember { Animatable(0f) }
    val coinsProgress = remember { Animatable(0f) }
    val goldCoinRightProgress = remember { Animatable(0f) }
    val goldCoinLeftProgress = remember { Animatable(0f) }
    val bobbingProgress = remember { Animatable(0f) }
    val coinBobbingProgress = remember { Animatable(0f) }
    val coinBobbingProgressRight = remember { Animatable(0f) }
    val coinBobbingProgressLeftSmall = remember { Animatable(0f) }
    val headerTextProgress = remember { Animatable(0f) }
    val uiElementsProgress = remember { Animatable(0f) }
    val red50Color = colorResource(id = R.color.red_50)
    val orange100Color = colorResource(id = R.color.orange_100)

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            circleProgress.snapTo(0f)
            launch {
                circleProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 770)
                )
            }
            launch {
                delay(150)
                waveProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 800)
                )
            }
            launch {
                delay(150)
                launch {
                    ghostHeartProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 700)
                    )
                }
                launch {
                    ghostHeartScale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 700)
                    )
                }
            }
            launch {
                delay(150)
                coinsProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 700)
                )
            }
            launch {
                delay(850)
                goldCoinRightProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = keyframes {
                        durationMillis = 1000
                        0f at 0 with FastOutSlowInEasing
                        0.50f at 350 with LinearOutSlowInEasing
                        1.65f at 750 with FastOutLinearInEasing
                        1f at 1000 with FastOutSlowInEasing
                    }
                )
            }
            launch {
                delay(1150)
                goldCoinLeftProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = keyframes {
                        durationMillis = 1000
                        0f at 0 with FastOutSlowInEasing
                        0.50f at 350 with LinearOutSlowInEasing
                        1.65f at 750 with FastOutLinearInEasing
                        1f at 1000 with FastOutSlowInEasing
                    }
                )
                coinBobbingProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            launch {
                delay(1050)
                coinBobbingProgressLeftSmall.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            launch {
                delay(850 + 1000)
                coinBobbingProgressRight.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            launch {
                delay(300)
                headerTextProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                )
            }
            launch {
                delay(500)
                uiElementsProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                )
            }
            delay(1270)
            launch {
                bobbingProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2000),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            onAnimationComplete()
        }
    }

    fun startDismissAnimation() {
        if (isDismissing) return
        isDismissing = true
        coroutineScope.launch {
            launch {
                uiElementsProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200)
                )
            }
            launch {
                headerTextProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200)
                )
            }
            launch {
                waveProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200)
                )
            }
            launch {
                ghostHeartProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200)
                )
            }
            launch {
                goldCoinLeftProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200)
                )
            }
            launch {
                goldCoinRightProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200)
                )
            }
            delay(200)
            circleProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1000)
            )
            onDismissComplete()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {}
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

            val maxRadius = sqrt(
                (canvasWidth / 2f) * (canvasWidth / 2f) +
                (canvasHeight / 2f) * (canvasHeight / 2f)
            )

            val currentRadius = maxRadius * (1f - circleProgress.value)

            val circlePath = Path().apply {
                addOval(
                    Rect(
                        center.x - currentRadius,
                        center.y - currentRadius,
                        center.x + currentRadius,
                        center.y + currentRadius
                    )
                )
            }

            clipPath(circlePath, clipOp = ClipOp.Difference) {
                drawRect(
                    color = red50Color,
                    size = size
                )
            }
        }

        if (goldCoinLeftProgress.value > 0f) {
            val waveHeight = (configuration.screenHeightDp * 0.65f).dp
            val waveTopPosition = configuration.screenHeightDp.dp - waveHeight

            val coinSize = 40.dp

            val finalX = 48.dp + (coinSize / 2f) + 8.dp
            val finalY = waveTopPosition + 30.dp

            val startX = finalX
            val startY = waveTopPosition + 100.dp

            val controlX = finalX
            val controlY = (startY + finalY) / 2f

            val progress = goldCoinLeftProgress.value
            val inverseProgress = 1f - progress
            val currentX = inverseProgress * inverseProgress * startX.value +
                          2f * inverseProgress * progress * controlX.value +
                          progress * progress * finalX.value
            val currentY = inverseProgress * inverseProgress * startY.value +
                          2f * inverseProgress * progress * controlY.value +
                          progress * progress * finalY.value

            val rotation = -150f * goldCoinLeftProgress.value
            val coinBobbingOffset = ((coinBobbingProgressLeftSmall.value * 0.4f - 0.5f) * 40f).dp

            Image(
                painter = painterResource(id = R.drawable.gold_coin),
                contentDescription = null,
                modifier = Modifier
                    .size(coinSize)
                    .absoluteOffset(
                        x = currentX.dp - (coinSize / 2f),
                        y = currentY.dp - (coinSize / 2f) + coinBobbingOffset
                    )
                    .graphicsLayer {
                        rotationZ = rotation
                    }
            )
        }

        if (goldCoinLeftProgress.value > 0f) {
            val waveHeight = (configuration.screenHeightDp * 0.65f).dp
            val waveTopPosition = configuration.screenHeightDp.dp - waveHeight

            val finalX = 48.dp
            val finalY = waveTopPosition + 25.dp

            val startX = finalX
            val startY = waveTopPosition + 100.dp

            val controlX = finalX
            val controlY = (startY + finalY) / 2f

            val progress = goldCoinLeftProgress.value
            val inverseProgress = 1f - progress
            val currentX = inverseProgress * inverseProgress * startX.value +
                          2f * inverseProgress * progress * controlX.value +
                          progress * progress * finalX.value
            val currentY = inverseProgress * inverseProgress * startY.value +
                          2f * inverseProgress * progress * controlY.value +
                          progress * progress * finalY.value

            val rotation = 158f * goldCoinLeftProgress.value
            val coinBobbingOffset = ((coinBobbingProgress.value * 0.4f - 0.5f) * 40f).dp

            val coinSize = 60.dp

            Image(
                painter = painterResource(id = R.drawable.gold_coin),
                contentDescription = null,
                modifier = Modifier
                    .size(coinSize)
                    .absoluteOffset(
                        x = currentX.dp - (coinSize / 2f),
                        y = currentY.dp - (coinSize / 2f) + coinBobbingOffset
                    )
                    .graphicsLayer {
                        rotationZ = rotation
                    }
            )
        }

        if (goldCoinRightProgress.value > 0f) {
            val waveHeight = (configuration.screenHeightDp * 0.65f).dp
            val waveTopPosition = configuration.screenHeightDp.dp - waveHeight

            val finalX = configuration.screenWidthDp.dp - 60.dp
            val finalY = waveTopPosition + 28.dp

            val startX = finalX
            val startY = waveTopPosition + 100.dp

            val controlX = finalX
            val controlY = (startY + finalY) / 2f

            val progress = goldCoinRightProgress.value
            val inverseProgress = 1f - progress
            val currentX = inverseProgress * inverseProgress * startX.value +
                          2f * inverseProgress * progress * controlX.value +
                          progress * progress * finalX.value
            val currentY = inverseProgress * inverseProgress * startY.value +
                          2f * inverseProgress * progress * controlY.value +
                          progress * progress * finalY.value

            val rotation = -158f * goldCoinRightProgress.value
            val coinBobbingOffset = ((coinBobbingProgressRight.value * 0.4f - 0.5f) * 40f).dp

            val coinSize = 60.dp

            Image(
                painter = painterResource(id = R.drawable.gold_coin),
                contentDescription = null,
                modifier = Modifier
                    .size(coinSize)
                    .absoluteOffset(
                        x = currentX.dp - (coinSize / 2f),
                        y = currentY.dp - (coinSize / 2f) + coinBobbingOffset
                    )
                    .graphicsLayer {
                        rotationZ = rotation
                    }
            )
        }

        if (ghostHeartProgress.value >= 0.5f) {
            AndroidView(
                factory = { context ->
                    android.widget.FrameLayout(context).apply {
                        id = android.view.View.generateViewId()
                        post {
                            val screenWidth = configuration.screenWidthDp * density.density
                            val screenHeight = configuration.screenHeightDp * density.density

                            val waveHeight = configuration.screenHeightDp * 0.65f * density.density
                            val waveTopPosition = screenHeight - waveHeight
                            val ghostHeartVerticalPosition = waveTopPosition / 2f
                            val heartCenterY = ghostHeartVerticalPosition + (55 * density.density)

                            com.plattysoft.leonids.ParticleSystem(
                                this,
                                14,
                                android.graphics.drawable.BitmapDrawable(
                                    context.resources,
                                    HabiticaIconsHelper.imageOfGold()
                                ),
                                5000
                            )
                                .setInitialRotationRange(0, 200)
                                .setScaleRange(0.7f, 1.1f)
                                .setSpeedRange(0.05f, 0.09f)
                                .setFadeOut(4000, android.view.animation.AccelerateInterpolator())
                                .setSpeedModuleAndAngleRange(0.05f, 0.09f, 305, 305 + 90)
                                .emit(
                                    (screenWidth / 2).toInt(),
                                    heartCenterY.toInt(),
                                    3,
                                    12000
                                )

                            com.plattysoft.leonids.ParticleSystem(
                                this,
                                14,
                                android.graphics.drawable.BitmapDrawable(
                                    context.resources,
                                    HabiticaIconsHelper.imageOfGold()
                                ),
                                5000
                            )
                                .setInitialRotationRange(0, 200)
                                .setScaleRange(0.7f, 1.1f)
                                .setSpeedRange(0.05f, 0.09f)
                                .setFadeOut(4000, android.view.animation.AccelerateInterpolator())
                                .setSpeedModuleAndAngleRange(0.05f, 0.09f, 160, 160 + 90)
                                .emit(
                                    (screenWidth / 2).toInt(),
                                    heartCenterY.toInt(),
                                    3,
                                    12000
                                )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (waveProgress.value > 0f) {
            val waveHeight = (configuration.screenHeightDp * 0.65f).dp

            val yellowOffset = with(density) {
                (screenHeight * (1f - waveProgress.value)).toDp()
            }

            val orangeOffset = with(density) {
                (screenHeight * (1f - waveProgress.value) * 0.7f).toDp()
            }

            Image(
                painter = painterResource(id = R.drawable.yellow_100_wave_front),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(
                        width = configuration.screenWidthDp.dp,
                        height = waveHeight
                    )
                    .graphicsLayer(
                        scaleX = -1.05f,
                        scaleY = 1.05f,
                        translationX = -10f
                    )
                    .offset(y = orangeOffset),
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(orange100Color)
            )

            Image(
                painter = painterResource(id = R.drawable.yellow_100_wave_front),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(
                        width = configuration.screenWidthDp.dp,
                        height = waveHeight
                    )
                    .offset(y = yellowOffset),
                contentScale = ContentScale.FillBounds
            )
        }

        if (ghostHeartProgress.value > 0f) {
            val waveHeight = (configuration.screenHeightDp * 0.65f).dp

            val waveTopPosition = configuration.screenHeightDp.dp - waveHeight

            val baseScale = 1.75f * 0.85f

            val currentScale = 0.15f + (0.85f * ghostHeartScale.value)
            val finalScale = baseScale * currentScale

            val ghostHeartVerticalPosition = waveTopPosition / 2f - ((157 * baseScale).dp / 2f)

            val startY = -300.dp
            val ghostHeartOffset = startY + ((ghostHeartVerticalPosition - startY) * ghostHeartProgress.value)

            val bobbingOffset = (bobbingProgress.value * 10f).dp

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(configuration.screenWidthDp.dp)
                    .height((157 * baseScale).dp)
                    .offset(y = ghostHeartOffset + bobbingOffset)
                    .graphicsLayer {
                        scaleX = currentScale
                        scaleY = currentScale
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.death_ghost),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(width = (90 * baseScale).dp, height = (132 * baseScale).dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_broken_heart),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size((110 * baseScale).dp)
                )
            }
        }

        val red1Color = colorResource(id = R.color.red_1)

        val waveHeight = (configuration.screenHeightDp * 0.65f).dp
        val waveTopPosition = configuration.screenHeightDp.dp - waveHeight

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = headerTextProgress.value
                    translationY = (1f - headerTextProgress.value) * 50f
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.you_ran_out_of_health),
                color = red1Color,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.23.sp
                )
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .offset(y = 64.dp)
                .graphicsLayer {
                    alpha = uiElementsProgress.value
                    translationY = (1f - uiElementsProgress.value) * 50f
                },
            contentAlignment = Alignment.Center
        ) {
            val level = (userLevel ?: 2) - 1
            val gold = userGold ?: 0
            val lossText = stringResource(id = lossDescriptionStringId, level, gold)

            val htmlSpanned = Html.fromHtml(lossText, Html.FROM_HTML_MODE_LEGACY)

            val annotatedString = buildAnnotatedString {
                val spannedString = htmlSpanned.toString()
                var currentIndex = 0
                val styleSpans = htmlSpanned.getSpans(0, htmlSpanned.length, Any::class.java)

                val boldRanges = styleSpans.mapNotNull { span ->
                    if (span is android.text.style.StyleSpan && span.style == android.graphics.Typeface.BOLD) {
                        htmlSpanned.getSpanStart(span) to htmlSpanned.getSpanEnd(span)
                    } else null
                }

                if (boldRanges.isEmpty()) {
                    append(spannedString)
                } else {
                    boldRanges.sortedBy { it.first }.forEach { (start, end) ->
                        if (currentIndex < start) {
                            append(spannedString.substring(currentIndex, start))
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(spannedString.substring(start, end))
                        }
                        currentIndex = end
                    }
                    if (currentIndex < spannedString.length) {
                        append(spannedString.substring(currentIndex))
                    }
                }
            }

            Text(
                text = annotatedString,
                color = red1Color,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    letterSpacing = (-0.48).sp
                )
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = uiElementsProgress.value
                    translationY = (1f - uiElementsProgress.value) * 50f
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!useAltText) {
                Text(
                    text = stringResource(id = R.string.faint_broken_gear_info),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
                    color = red1Color,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp,
                        letterSpacing = 0.4.sp
                    )
                )
            }

            val maroon100Color = colorResource(id = R.color.maroon_100)
            Button(
                onClick = {
                    if (!isRefilling) {
                        isRefilling = true
                        startDismissAnimation()
                        onRefillHealthClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isDismissing && !isRefilling && !isUsingSecondChance
            ) {
                if (isRefilling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = maroon100Color
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.faint_button),
                        color = maroon100Color,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            val teal10Color = colorResource(id = R.color.teal_10)
            val teal1Color = colorResource(id = R.color.teal_1)
            val subPerkBgColor = Color(0xFF005737)
            val subPerkTextColor = Color(0xFF77F4C7)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val path = Path().apply {
                        val cornerRadius = 24.dp.toPx()
                        moveTo(0f, cornerRadius)
                        quadraticTo(0f, 0f, cornerRadius, 0f)
                        lineTo(size.width - cornerRadius, 0f)
                        quadraticTo(size.width, 0f, size.width, cornerRadius)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF72CFFF),
                                Color(0xFF77F4C7)
                            )
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when {
                            isSubscribed && hasUsedSecondChance -> {
                                Button(
                                    onClick = {},
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .graphicsLayer { alpha = 0.6f },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = false
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_clock_24dp),
                                        contentDescription = null,
                                        tint = teal10Color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            isSubscribed && !hasUsedSecondChance -> {
                                Button(
                                    onClick = {
                                        if (!isUsingSecondChance) {
                                            isUsingSecondChance = true
                                            startDismissAnimation()
                                            onUseSecondChanceClick()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !isDismissing && !isRefilling && !isUsingSecondChance
                                ) {
                                    if (isUsingSecondChance) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = teal10Color
                                        )
                                    } else {
                                        Text(
                                            text = stringResource(id = R.string.subscriber_button_faint_use),
                                            color = teal10Color,
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        )
                                    }
                                }
                                if (isSubscribed && !hasUsedSecondChance) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 8.dp, y = (-8).dp)
                                            .background(
                                                color = subPerkBgColor,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.sub_perk),
                                            color = subPerkTextColor,
                                            style = TextStyle(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                            else -> {
                                Button(
                                    onClick = onSubscribeClick,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.subscribe_incentive_button_faint),
                                        color = teal10Color,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = when {
                            isSubscribed && hasUsedSecondChance -> {
                                stringResource(id = R.string.subscriber_benefit_used_faint, timeUntilRecharge ?: "...")
                            }
                            isSubscribed && !hasUsedSecondChance -> {
                                stringResource(id = R.string.subscriber_benefit_available_faint)
                            }
                            else -> {
                                stringResource(id = R.string.subscribe_incentive_text_faint)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 3.dp, bottom = 32.dp),
                        color = Color(0xFF005158),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            letterSpacing = 0.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}
