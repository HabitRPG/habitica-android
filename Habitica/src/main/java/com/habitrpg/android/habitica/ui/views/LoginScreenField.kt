package com.habitrpg.android.habitica.ui.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.HabiticaCircularProgressView

enum class LoginFieldState {
    DEFAULT,
    VALID,
    ERROR,
    LOADING,
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LoginDecorationBox(
    value: String,
    label: String,
    state: LoginFieldState,
    focused: Boolean = false,
    prefix: @Composable () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors,
    interactionSource: MutableInteractionSource,
    innerTextField: @Composable () -> Unit) {
        TextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = innerTextField,
            placeholder = { Text(label, fontSize = 18.sp, fontWeight = FontWeight.Normal) },
            enabled = true,
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            shape = HabiticaTheme.shapes.large,
            prefix = prefix,
            leadingIcon = icon,
            suffix = {
                AnimatedContent(Pair(state, focused)) { (it, focused) ->
                    if (it == LoginFieldState.ERROR && !focused) {
                        Image(
                            painterResource(R.drawable.ic_close_white_18dp),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorResource(R.color.red_100))
                        )
                    } else if (it == LoginFieldState.VALID) {
                        Image(
                            painterResource(R.drawable.checkmark),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorResource(R.color.green_50))
                        )
                    } else if (it == LoginFieldState.LOADING) {
                        HabiticaCircularProgressView(indicatorSize = 20.dp, strokeWidth = 2.dp)
                    }
                }
            },
            colors = colors
        )
}

@Composable
fun LoginScreenField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    prefix: @Composable () -> Unit = {},
    state: LoginFieldState = LoginFieldState.DEFAULT,
    errorMessage: String? = null,
    hideInput: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    var focused by remember { mutableStateOf(false) }
    val textFieldState by remember { mutableStateOf(TextFieldState(value)) }

    LaunchedEffect(textFieldState.text) {
        onValueChange(textFieldState.text.toString())
    }

    val containerColor = colorResource(R.color.brand_100)
    val keyboardOptions = if (hideInput) {
        KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password, autoCorrectEnabled = false)
    } else {
        KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email, autoCorrectEnabled = false)
    }
    var stillShowInput by remember { mutableStateOf(false) }
    val showInput = !hideInput || stillShowInput

    val colors = TextFieldDefaults.colors(
        unfocusedContainerColor = containerColor,
        focusedContainerColor = containerColor,
        errorContainerColor = containerColor,
        unfocusedTextColor = Color.White,
        focusedTextColor = Color.White,
        errorTextColor = colorResource(R.color.red_500),
        unfocusedPlaceholderColor = colorResource(R.color.brand_600),
        focusedPlaceholderColor = colorResource(R.color.brand_600).copy(alpha = 0.5f),
        unfocusedIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        unfocusedTrailingIconColor = colorResource(R.color.brand_100),
        cursorColor = Color.White,
        selectionColors = TextSelectionColors(
            handleColor = colorResource(R.color.white),
            backgroundColor = colorResource(R.color.brand_600).copy(alpha = 0.3f)
        ),
    )

    val mergedTextStyle = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal
    ).merge(TextStyle(color = if (focused) colors.focusedTextColor else colors.unfocusedTextColor))

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (hideInput) {
            BasicSecureTextField(
                state = textFieldState,
                textObfuscationMode = if (showInput) {
                    TextObfuscationMode.Visible
                } else {
                    TextObfuscationMode.RevealLastTyped
                },
                cursorBrush = SolidColor(Color.White),
                textStyle = mergedTextStyle,
                keyboardOptions = keyboardOptions,
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)
                    .onFocusChanged {
                        focused = it.isFocused
                    },
                decorator = {
                    LoginDecorationBox(value, label, state, focused, prefix, {
                        Box(modifier = Modifier.clickable {
                            stillShowInput = !stillShowInput
                        }) {
                            icon?.invoke()
                        }
                    }, colors, interactionSource, it)
                }
            )
        } else {
            BasicTextField(
                state = textFieldState,
                textStyle = mergedTextStyle,
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)
                    .onFocusChanged {
                        focused = it.isFocused
                    },
                keyboardOptions = keyboardOptions,
                lineLimits = TextFieldLineLimits.SingleLine,
                decorator = { innerTextField ->
                    LoginDecorationBox(value, label, state, focused, prefix, icon, colors, interactionSource, innerTextField)
                }
            )
        }
        AnimatedVisibility(errorMessage != null && !focused) {
            Text(
                text = errorMessage ?: "",
                color = colorResource(R.color.red_500),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp)
            )
        }
    }
}

@Preview(wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE)
@Composable
fun LoginScreenFieldPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(8.dp)) {
        LoginScreenField(
            label = "Email",
            value = "",
            icon = {
                Image(painterResource(R.drawable.login_email), contentDescription = null)
            }, onValueChange = {}
        )
        LoginScreenField(
            label = "Email",
            value = "test@test.com",
            icon = {
                Image(painterResource(R.drawable.login_email), contentDescription = null)
            }, onValueChange = {}, state = LoginFieldState.VALID
        )
        LoginScreenField(
            label = "Email",
            value = "test@test.com",
            icon = {
                Image(painterResource(R.drawable.login_email), contentDescription = null)
            }, onValueChange = {}, state = LoginFieldState.ERROR
        )
        LoginScreenField(
            label = "Username",
            value = "Bla",
            icon = {
                Image(painterResource(R.drawable.login_username), contentDescription = null)
            }, onValueChange = {}, state = LoginFieldState.LOADING
        )
        LoginScreenField(
            label = "Password",
            value = "",
            hideInput = true,
            icon = {
                Image(painterResource(R.drawable.login_password), contentDescription = null)
            }, onValueChange = {}
        )
        LoginScreenField(
            label = "Password",
            value = "abcd",
            hideInput = true,
            icon = {
                Image(painterResource(R.drawable.login_password), contentDescription = null)
            }, onValueChange = {}
        )
    }
}
