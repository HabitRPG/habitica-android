package com.habitrpg.android.habitica.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.theme.HabiticaTheme


@Composable
fun ChangePasswordScreen(
    onCancel: () -> Unit,
    onSave: (oldPassword: String, newPassword: String) -> Unit
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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(id = R.string.cancel),
                    color = buttonColor,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onCancel() }
                )
                Text(
                    stringResource(id = R.string.change_password),
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
                Text(
                    stringResource(id = R.string.save),
                    color = if (canSave) buttonColor else labelColor,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable(enabled = canSave) {
                            attemptedSave = true
                            if (canSave) onSave(oldPassword, newPassword)
                        }
                )
            }

            PasswordField(
                label = stringResource(id = R.string.old_password),
                value = oldPassword,
                onValueChange = { oldPassword = it },
                fieldColor = fieldColor,
                labelColor = labelColor,
                textColor = textColor
            )
            Spacer(modifier = Modifier.height(20.dp))
            Column {
                PasswordField(
                    label = stringResource(id = R.string.new_password),
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    fieldColor = fieldColor,
                    labelColor = labelColor,
                    textColor = textColor
                )
                if (attemptedSave && !passwordValid) {
                    Text(
                        stringResource(id = R.string.password_too_short),
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column {
                PasswordField(
                    label = stringResource(id = R.string.confirm_new_password),
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    fieldColor = fieldColor,
                    labelColor = labelColor,
                    textColor = textColor
                )
                if (attemptedSave && !passwordsMatch) {
                    Text(
                        stringResource(id = R.string.password_not_matching),
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(id = R.string.password_change_info),
                color = labelColor,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.weight(1f))
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label, color = labelColor, fontSize = 15.sp)
        },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        isError = isError,
        visualTransformation = PasswordVisualTransformation(),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = fieldColor,
            focusedContainerColor = fieldColor,
            unfocusedBorderColor = if (isError) Color.Red else Color.Transparent,
            focusedBorderColor = if (isError) Color.Red else Color.Transparent,
            cursorColor = Color(0xFF9C8DF6),
            unfocusedTextColor = textColor,
            focusedTextColor = textColor
        )
    )
}



@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ChangePasswordScreenPreview() {
    HabiticaTheme {
        ChangePasswordScreen(
            onCancel = {},
            onSave = { _, _ -> }
        )
    }
}



