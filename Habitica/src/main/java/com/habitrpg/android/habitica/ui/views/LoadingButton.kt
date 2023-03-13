package com.habitrpg.android.habitica.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme


enum class LoadingButtonState {
    CONTENT,
    DISABLED,
    LOADING,
    FAILED,
    SUCCESS
}

@Composable
fun LoadingButton(
    state : LoadingButtonState,
    onClick : () -> Unit,
    modifier : Modifier = Modifier,
    elevation : ButtonElevation? = ButtonDefaults.elevation(0.dp),
    shape : Shape = MaterialTheme.shapes.medium,
    border : BorderStroke? = null,
    colors : ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = HabiticaTheme.colors.tintedUiSub,
        contentColor = Color.White
    ),
    contentPadding : PaddingValues = ButtonDefaults.ContentPadding,
    successContent : @Composable RowScope.() -> Unit,
    content : @Composable RowScope.() -> Unit
) {
    val buttonColors = if (state == LoadingButtonState.FAILED) {
        ButtonDefaults.buttonColors(backgroundColor = HabiticaTheme.colors.errorBackground)
    } else if (state == LoadingButtonState.SUCCESS) {
        ButtonDefaults.outlinedButtonColors(
            backgroundColor = HabiticaTheme.colors.successColor,
            contentColor = HabiticaTheme.colors.successColor
        )
    } else colors
    Button(
        {
            if (state == LoadingButtonState.CONTENT || state == LoadingButtonState.FAILED) {
                onClick()
            }
        },
        modifier.requiredHeight(40.dp),
        state != LoadingButtonState.DISABLED,
        elevation = elevation,
        shape = shape,
        border = border,
        colors = buttonColors,
        contentPadding = contentPadding
    ) {
        when (state) {
            LoadingButtonState.LOADING -> CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(16.dp)
            )
            LoadingButtonState.SUCCESS -> successContent()
            LoadingButtonState.FAILED -> Image(
                painterResource(R.drawable.failed_loading),
                stringResource(R.string.failed)
            )
            else -> content()
        }
    }
}
