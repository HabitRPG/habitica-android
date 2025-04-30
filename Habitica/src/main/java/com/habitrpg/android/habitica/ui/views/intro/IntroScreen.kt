package com.habitrpg.android.habitica.ui.views.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.helpers.launchCatching

@Composable
fun IntroPage(
    page: Int,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    description: @Composable () -> Unit,
    image: @Composable () -> Unit,
    background: Brush,
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ProvideTextStyle(
                TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            ) {
                subtitle()
            }
            Spacer(modifier = Modifier.height(12.dp))
            ProvideTextStyle(
                TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            ) {
                title()
            }
            Spacer(modifier = Modifier.height(50.dp))
            image()
            Spacer(modifier = Modifier.height(50.dp))
            ProvideTextStyle(
                TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    lineHeight = 20.sp
                )
            ) {
                description()
            }
        }
    }
}

fun Color.blend(topColor: Color, ratio: Float = 0.5f): Color {
    if (ratio == 0f) return this
    if (ratio == 1f) return topColor
    val intColor = ColorUtils.blendARGB(toArgb(), topColor.toArgb(), ratio)
    return Color(intColor)
}

@Composable
fun IntroScreen(onNextOnboardingStep: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val pageOffset by remember { derivedStateOf { pagerState.currentPageOffsetFraction } }
    var topColor: Color
    var bottomColor: Color
    print(pageOffset)
    if (pagerState.currentPage < 1) {
        if (pageOffset > 0) {
            topColor = colorResource(R.color.brand_400).blend(colorResource(R.color.blue_100), pageOffset)
            bottomColor = colorResource(R.color.brand_100).blend(colorResource(R.color.blue_10), pageOffset)
        } else {
            topColor = colorResource(R.color.brand_400)
            bottomColor = colorResource(R.color.brand_200)
        }
    } else if (pagerState.currentPage < 2) {
        if (pageOffset > 0) {
            topColor = colorResource(R.color.blue_100).blend(colorResource(R.color.red_100), pageOffset)
            bottomColor = colorResource(R.color.blue_10).blend(colorResource(R.color.red_10), pageOffset)
        } else {
            topColor = colorResource(R.color.blue_100).blend(colorResource(R.color.brand_400), -pageOffset)
            bottomColor = colorResource(R.color.blue_10).blend(colorResource(R.color.brand_200), -pageOffset)
        }
    } else {
        if (pageOffset > 0) {
            topColor = colorResource(R.color.blue_100).blend(colorResource(R.color.red_100), pageOffset)
            bottomColor = colorResource(R.color.blue_10).blend(colorResource(R.color.red_10), pageOffset)
        } else {
            topColor = colorResource(R.color.red_100).blend(colorResource(R.color.blue_100), -pageOffset)
            bottomColor = colorResource(R.color.red_10).blend(colorResource(R.color.blue_10), -pageOffset)
        }
    }
    Box(Modifier.fillMaxSize().background(
        Brush.verticalGradient(
            listOf(topColor, bottomColor)
        )
    )) {
        HorizontalPager(pagerState) { page ->
            when (page) {
                0 -> IntroPage(
                    page = page,
                    title = { Image(painterResource(R.drawable.intro_1_title), contentDescription = null) },
                    subtitle = { Text(stringResource(R.string.intro_1_subtitle)) },
                    description = { Text(stringResource(R.string.intro_1_description)) },
                    image = { Image(painterResource(R.drawable.intro_1), contentDescription = null) },
                    background = Brush.verticalGradient(listOf(colorResource(R.color.brand_400), colorResource(R.color.brand_200)))
                )

                1 -> IntroPage(
                    page = page,
                    title = { Text(stringResource(R.string.intro_2_title)) },
                    subtitle = { Text(stringResource(R.string.intro_2_subtitle)) },
                    description = { Text(stringResource(R.string.intro_2_description)) },
                    image = { Image(painterResource(R.drawable.intro_2), contentDescription = null) },
                    background = Brush.verticalGradient(listOf(colorResource(R.color.blue_100), colorResource(R.color.blue_50)))
                )

                2 -> IntroPage(
                    page = page,
                    title = { Text(stringResource(R.string.intro_3_title)) },
                    subtitle = { Text(stringResource(R.string.intro_3_subtitle)) },
                    description = { Text(stringResource(R.string.intro_3_description)) },
                    image = { Image(painterResource(R.drawable.intro_3), contentDescription = null) },
                    background = Brush.verticalGradient(listOf(colorResource(R.color.red_100), colorResource(R.color.red_50)))
                )
            }
        }
            Button(
                onClick = {
                    onNextOnboardingStep()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(WindowInsets.systemBars.asPaddingValues()),
                colors = ButtonDefaults.textButtonColors(),
            ) {
                Text(stringResource(R.string.skip_button), color = Color.White, fontSize = 18.sp)
            }

        Column(Modifier
            .align(Alignment.BottomCenter)
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = 20.dp)) {
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = (if (pagerState.currentPage == iteration) Color.Black else Color.White).copy(alpha = 0.6f)
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .rotate(45f)
                            .clip(RectangleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
            Button(
                {
                    if (pagerState.currentPage < pagerState.pageCount - 1) {
                        coroutineScope.launchCatching {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onNextOnboardingStep()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.4f),
                    contentColor = Color.White
                ),

                modifier = Modifier.fillMaxWidth()
            ) {
                if (pagerState.currentPage < pagerState.pageCount - 1) {
                    Text(stringResource(R.string.next_button), fontSize = 18.sp, fontWeight = FontWeight.Normal)
                } else {
                    Text(stringResource(R.string.lets_go), fontSize = 18.sp, fontWeight = FontWeight.Normal)
                }
            }
        }
    }
}
