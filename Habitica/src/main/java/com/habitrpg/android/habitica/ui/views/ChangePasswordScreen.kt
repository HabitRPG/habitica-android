package com.habitrpg.android.habitica.ui.views

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.theme.HabiticaTheme

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    onSave: (oldPassword: String, newPassword: String) -> Unit,
    onForgotPassword: () -> Unit
) {
    val colors = HabiticaTheme.colors
    val backgroundColor = colors.windowBackground
    val fieldColor = colorResource(id = R.color.gray600_gray10)
    val labelColor = colors.textSecondary
    val buttonColor = colors.tintedUiMain
    val textColor = colors.textPrimary

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var attemptedSave by remember { mutableStateOf(false) }

    val passwordValid = newPassword.length >= 8
    val passwordsMatch = newPassword == confirmPassword && newPassword.isNotEmpty()
    val canSave = passwordValid && passwordsMatch && oldPassword.isNotBlank()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopStart)
                    .padding(start = 22.dp, top = 16.dp)
            ) {
                Icon(
                    painterResource(id = R.drawable.arrow_back),
                    contentDescription = stringResource(R.string.action_back),
                    tint = textColor
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp + 40.dp + 8.dp
                    )
            ) {
                Text(
                    text = stringResource(R.string.change_password),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = textColor,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 6.dp, bottom = 12.dp)
                )

                Text(
                    text = stringResource(R.string.password_change_info),
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(start = 6.dp,bottom = 22.dp)
                )

                PasswordField(
                    label = stringResource(R.string.old_password),
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    fieldColor = fieldColor,
                    labelColor = labelColor,
                    textColor = textColor,
                    modifier = Modifier.fillMaxWidth()
                )

                PasswordField(
                    label = stringResource(R.string.new_password),
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    fieldColor = fieldColor,
                    labelColor = labelColor,
                    textColor = textColor,
                    isError = attemptedSave && !passwordValid,
                    errorMessage = if (attemptedSave && !passwordValid) stringResource(R.string.password_too_short, 8) else null,
                    modifier = Modifier.fillMaxWidth()
                )
                PasswordField(
                    label = stringResource(R.string.confirm_new_password),
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    fieldColor = fieldColor,
                    labelColor = labelColor,
                    textColor = textColor,
                    isError = attemptedSave && !passwordsMatch,
                    errorMessage = if (attemptedSave && !passwordsMatch) stringResource(R.string.password_not_matching) else null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(top = 24.dp))
                Button(
                    onClick = {
                        attemptedSave = true
                        if (canSave) onSave(oldPassword, newPassword)
                    },
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.brand_400),
                        disabledContainerColor = colorResource(id = R.color.brand_400),
                        contentColor           = Color.White,
                        disabledContentColor   = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = stringResource(R.string.change_password),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                TextButton(
                    onClick = onForgotPassword,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(R.string.forgot_pw_btn),
                        color = buttonColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}


@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    fieldColor: Color,
    labelColor: Color,
    textColor: Color,
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val onTextChangedColor = if (value.isNotBlank())
        colorResource(id = R.color.purple400_purple500)
    else
        colorResource(id = R.color.gray_400)

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        factory = { ctx ->
            LayoutInflater.from(ctx)
                .inflate(R.layout.component_text_input, null, false)
        },
        update = { view ->
            val til = view.findViewById<TextInputLayout>(R.id.text_input_layout)
            val edit = view.findViewById<AppCompatEditText>(R.id.text_edit_text)
            til.hint = label

            edit.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            edit.transformationMethod = PasswordTransformationMethod.getInstance()

            fun syncColors(focused: Boolean) {
                val active = focused || edit.text?.isNotBlank() == true
                val stroke = if (active) onTextChangedColor else labelColor
                til.defaultHintTextColor = ColorStateList.valueOf(stroke.toArgb())
                til.setBoxStrokeColorStateList(ColorStateList.valueOf(stroke.toArgb()))
            }

            syncColors(edit.isFocused)

            edit.setOnFocusChangeListener { _, focused ->
                syncColors(focused)
            }

            edit.doAfterTextChanged {
                syncColors(edit.isFocused)
                onValueChange(it.toString())
            }

            if (edit.text.toString() != value) {
                edit.setText(value)
                edit.setSelection(value.length)
            }

            til.isErrorEnabled = isError
            til.error = errorMessage

            til.setBoxStrokeWidth(2)
            edit.setTextColor(textColor.toArgb())
        }
    )
}

@Preview(showBackground = true, widthDp = 327, heightDp = 704, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChangePasswordScreenPreview() {
    HabiticaTheme {
        ChangePasswordScreen(
            onBack = {},
            onSave = { _, _ -> },
            onForgotPassword = {}
        )
    }
}
