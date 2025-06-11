package com.habitrpg.android.habitica.ui.views

import android.content.res.Configuration
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val fieldColor = colors.contentBackground
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .padding(bottom = 12.dp)
            ) {
                Icon(
                    painterResource(id = R.drawable.arrow_back),
                    contentDescription = stringResource(R.string.action_back),
                    tint = textColor
                )
            }

            Text(
                text = stringResource(R.string.change_password),
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = textColor,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = stringResource(R.string.password_change_info),
                color = labelColor,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 22.dp)
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
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            PasswordField(
                label = stringResource(R.string.new_password),
                value = newPassword,
                onValueChange = { newPassword = it },
                fieldColor = fieldColor,
                labelColor = labelColor,
                textColor = textColor,
                isError = attemptedSave && !passwordValid,
                modifier = Modifier.fillMaxWidth()
            )
            if (attemptedSave && !passwordValid) {
                Text(
                    text = stringResource(R.string.password_too_short),
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            PasswordField(
                label = stringResource(R.string.confirm_new_password),
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                fieldColor = fieldColor,
                labelColor = labelColor,
                textColor = textColor,
                isError = attemptedSave && !passwordsMatch,
                modifier = Modifier.fillMaxWidth()
            )
            if (attemptedSave && !passwordsMatch) {
                Text(
                    text = stringResource(R.string.password_not_matching),
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.padding(top = 24.dp))

            Button(
                onClick = {
                    attemptedSave = true
                    if (canSave) onSave(oldPassword, newPassword)
                },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.purple400_purple500),
                    disabledContainerColor = colorResource(id = R.color.purple400_purple500).copy(alpha = 0.3f)
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

@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    fieldColor: Color,
    labelColor: Color,
    textColor: Color,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {

    val dividerColor = if (value.isNotBlank()) colorResource(id = R.color.purple400_purple500) else colorResource(id = R.color.gray_400)

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(label, color = labelColor, fontSize = 17.sp)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            isError = isError,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = fieldColor,
                focusedContainerColor = fieldColor,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                cursorColor = Color(0xFF9C8DF6),
                unfocusedTextColor = textColor,
                focusedTextColor = textColor
            )
        )
        HorizontalDivider(
            color = dividerColor,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        )
    }
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
