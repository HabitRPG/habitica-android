package com.habitrpg.android.habitica.ui.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
fun LoginScreenField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    prefix: @Composable () -> Unit = {},
    state: LoginFieldState = LoginFieldState.DEFAULT,
    hideInput: Boolean = false,
) {
    val containerColor = colorResource(R.color.brand_100)
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, fontSize = 18.sp, fontWeight = FontWeight.Normal) },
        isError = state == LoginFieldState.ERROR,
        suffix = {
            AnimatedContent(state) {
                if (it == LoginFieldState.ERROR) {
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
        singleLine = true,
        trailingIcon = icon,
        prefix = prefix,
        textStyle = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
        ),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = containerColor,
            focusedContainerColor = containerColor,
            errorContainerColor = containerColor,
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White,
            errorTextColor = colorResource(R.color.red_100),
            unfocusedPlaceholderColor = colorResource(R.color.brand_600),
            focusedPlaceholderColor = colorResource(R.color.brand_600).copy(alpha = 0.5f),
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            unfocusedTrailingIconColor = colorResource(R.color.brand_100),
            cursorColor = Color.White,
            selectionColors = TextSelectionColors(
                handleColor = colorResource(R.color.brand_600),
                backgroundColor = colorResource(R.color.brand_600).copy(alpha = 0.3f)
            ),
        ),
        shape = HabiticaTheme.shapes.large,
        modifier = modifier.fillMaxWidth().heightIn(min = 60.dp),
        visualTransformation = if (hideInput) PasswordVisualTransformation() else VisualTransformation.None,
    )
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
            value = "abcd",
            hideInput = true,
            icon = {
                Image(painterResource(R.drawable.login_password), contentDescription = null)
            }, onValueChange = {}
        )
    }
}
