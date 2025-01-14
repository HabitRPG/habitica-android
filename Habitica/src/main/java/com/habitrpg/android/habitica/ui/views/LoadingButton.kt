package com.habitrpg.android.habitica.ui.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.theme.HabiticaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class LoadingButtonState {
    CONTENT,
    DISABLED,
    LOADING,
    FAILED,
    SUCCESS
}

enum class LoadingButtonType {
    NORMAL,
    DESTRUCTIVE
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoadingButton(
    state: LoadingButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: LoadingButtonType = LoadingButtonType.NORMAL,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    shape: Shape = MaterialTheme.shapes.medium,
    border: BorderStroke? = null,
    colors: ButtonColors? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    successContent: (@Composable RowScope.() -> Unit)? = null,
    failedContent: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val colorStyle =
        if (type == LoadingButtonType.DESTRUCTIVE) {
            ButtonDefaults.buttonColors(
                containerColor = HabiticaTheme.colors.errorBackground,
                contentColor = Color.White,
                disabledContainerColor = HabiticaTheme.colors.offsetBackground,
                disabledContentColor = HabiticaTheme.colors.textQuad
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = HabiticaTheme.colors.tintedUiSub,
                contentColor = Color.White,
                disabledContainerColor = HabiticaTheme.colors.offsetBackground,
                disabledContentColor = HabiticaTheme.colors.textQuad
            )
        }
    val colorSpec = tween<Color>(350)
    val backgroundColor =
        animateColorAsState(
            targetValue =
            when (state) {
                LoadingButtonState.FAILED -> HabiticaTheme.colors.errorBackground
                LoadingButtonState.SUCCESS -> Color.Transparent
                else -> if (state != LoadingButtonState.DISABLED) colorStyle.containerColor else colorStyle.disabledContainerColor
            },
            animationSpec = colorSpec
        )
    val contentColor =
        animateColorAsState(
            targetValue =
            when (state) {
                LoadingButtonState.FAILED -> Color.White
                LoadingButtonState.SUCCESS -> if (type == LoadingButtonType.DESTRUCTIVE) HabiticaTheme.colors.errorColor else HabiticaTheme.colors.successColor
                else -> if (state != LoadingButtonState.DISABLED) colorStyle.contentColor else colorStyle.disabledContentColor
            },
            animationSpec = colorSpec
        )
    val borderWidth =
        animateDpAsState(
            targetValue =
            if (state == LoadingButtonState.SUCCESS) {
                3.dp
            } else {
                border?.width ?: 0.dp
            }
        )

    val buttonColors =
        ButtonDefaults.buttonColors(
            containerColor = backgroundColor.value,
            contentColor = contentColor.value,
            disabledContainerColor = backgroundColor.value,
            disabledContentColor = contentColor.value
        )
    Button(
        {
            if (state == LoadingButtonState.CONTENT || state == LoadingButtonState.FAILED) {
                onClick()
            }
        },
        modifier
            .requiredHeight(40.dp)
            .animateContentSize(tween(350)),
        state != LoadingButtonState.DISABLED,
        elevation = elevation,
        shape = shape,
        border =
        if (state == LoadingButtonState.SUCCESS) {
            BorderStroke(
                borderWidth.value,
                if (type == LoadingButtonType.DESTRUCTIVE) HabiticaTheme.colors.errorColor else HabiticaTheme.colors.successColor
            )
        } else {
            border
        },
        colors = buttonColors,
        contentPadding = PaddingValues(0.dp)
    ) {
        ProvideTextStyle(value = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)) {
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    val isInitialShowingContent =
                        initialState == LoadingButtonState.CONTENT || initialState == LoadingButtonState.DISABLED || (initialState == LoadingButtonState.SUCCESS && successContent == null)
                    val isTargetShowingContent =
                        targetState == LoadingButtonState.CONTENT || targetState == LoadingButtonState.DISABLED || (targetState == LoadingButtonState.SUCCESS && successContent == null)
                    if (targetState == LoadingButtonState.FAILED) {
                        (
                            fadeIn(
                                animationSpec = tween(220, delayMillis = 90)
                            ) +
                                slideInHorizontally(
                                    animationSpec =
                                    spring(
                                        dampingRatio = 0.2f,
                                        stiffness = StiffnessMediumLow
                                    )
                                )
                            ).togetherWith(fadeOut(animationSpec = tween(90)))
                    } else if (isInitialShowingContent && isTargetShowingContent) {
                        fadeIn() togetherWith fadeOut()
                    } else {
                        (
                            fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                scaleIn(
                                    initialScale = 0.92f,
                                    animationSpec =
                                    tween(
                                        220,
                                        delayMillis = 90,
                                        FastOutSlowInEasing
                                    )
                                )
                            ).togetherWith(fadeOut(animationSpec = tween(90)))
                    }
                },
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(contentPadding)
            ) { state ->
                when (state) {
                    LoadingButtonState.LOADING ->
                        CircularProgressIndicator(
                            color = contentColor.value,
                            modifier = Modifier.size(16.dp)
                        )

                    LoadingButtonState.SUCCESS -> successContent?.let { it() } ?: content()
                    LoadingButtonState.FAILED ->
                        failedContent?.let { it() } ?: Image(
                            painterResource(R.drawable.failed_loading),
                            stringResource(R.string.failed),
                            Modifier.padding(horizontal = 8.dp)
                        )

                    else -> content()
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    var state: LoadingButtonState by remember { mutableStateOf(LoadingButtonState.CONTENT) }
    val scope = rememberCoroutineScope()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
        Modifier
            .width(200.dp)
            .padding(8.dp)
    ) {
        LoadingButton(state, {
            scope.launch {
                state = LoadingButtonState.LOADING
                delay(2000)
                state = LoadingButtonState.FAILED
                delay(2000)
                state = LoadingButtonState.LOADING
                delay(2000)
                state = LoadingButtonState.SUCCESS
                delay(2000)
                state = LoadingButtonState.DISABLED
                delay(2000)
                state = LoadingButtonState.CONTENT
            }
        }, successContent = {
                Text("I did it!")
            }, content = {
                Text("Do something")
            }, modifier = Modifier.fillMaxWidth())
        LoadingButton(
            LoadingButtonState.LOADING,
            {},
            colors =
            ButtonDefaults.buttonColors(
                containerColor = HabiticaTheme.colors.successBackground,
                contentColor = Color.White
            ),
            content = {
                Text("Do something")
            }
        )
        LoadingButton(LoadingButtonState.LOADING, {}, content = {
            Text("Do something")
        }, modifier = Modifier.fillMaxWidth())
        LoadingButton(LoadingButtonState.FAILED, {}, content = {
            Text("Do something")
        })
        LoadingButton(LoadingButtonState.FAILED, {}, failedContent = {
            Text("Didn't work :(")
        }, content = {
                Text("Do something")
            })
        LoadingButton(LoadingButtonState.SUCCESS, {}, content = {
            Text("Do something")
        })
        LoadingButton(LoadingButtonState.SUCCESS, {}, successContent = {
            Text("Success!")
        }, content = {
                Text("Do something")
            })
        LoadingButton(LoadingButtonState.CONTENT, {}, content = {
            Text("Do something")
        })
        LoadingButton(LoadingButtonState.DISABLED, {}, content = {
            Text("Disabled Button")
        })
    }
}
