package com.habitrpg.android.habitica.ui.views.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.views.LoginFieldState
import com.habitrpg.android.habitica.ui.views.LoginScreenField
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.HabiticaCircularProgressView

@Composable
fun LoginForm(
    onToggleFormType: () -> Unit,
    email: String,
    emailFieldState: LoginFieldState,
    onEmailChange: (String) -> Unit,
    password: String,
    passwordFieldState: LoginFieldState,
    onPasswordChange: (String) -> Unit,
    onGoogleLoginClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit,
    isRegistering: Boolean,
    onSubmit: () -> Unit,
    showLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    var confirmPassword by remember { mutableStateOf("") }
    Column(
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        modifier = modifier.widthIn(max = 480.dp)
    ) {
        Button(
            {
                onToggleFormType()
            },
            colors = ButtonDefaults.textButtonColors(),
            contentPadding = PaddingValues(15.dp),
            modifier = Modifier.Companion.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            ProvideTextStyle(TextStyle(fontSize=18.sp)) {
                AnimatedContent(isRegistering) {
                    if (it) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                stringResource(R.string.already_have_an_account),
                                color = colorResource(R.color.brand_600)
                            )
                            Text(
                                stringResource(R.string.login_btn),
                                color = colorResource(R.color.white)
                            )
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                stringResource(R.string.need_an_account),
                                color = colorResource(R.color.brand_600)
                            )
                            Text(
                                stringResource(R.string.register_btn),
                                color = colorResource(R.color.white)
                            )
                        }
                    }
                }
            }
        }
        LoginScreenField(
            label = if (isRegistering) stringResource(R.string.email) else stringResource(R.string.username_or_email),
            value = email,
            state = emailFieldState,
            onValueChange = onEmailChange,
            icon = {
                AnimatedContent(isRegistering) {
                    if (it) {
                        Image(
                            painterResource(R.drawable.login_email),
                            contentDescription = stringResource(R.string.email)
                        )
                    } else {
                        Image(
                            painterResource(R.drawable.login_username),
                            contentDescription = stringResource(R.string.email)
                        )
                    }
                }

            },
            modifier = Modifier.Companion.fillMaxWidth().padding(bottom = 10.dp),
        )
        LoginScreenField(
            label = stringResource(R.string.password),
            value = password,
            state = passwordFieldState,
            onValueChange = onPasswordChange,
            hideInput = true,
            icon = {
                Image(
                    painterResource(R.drawable.login_password),
                    contentDescription = stringResource(R.string.password)
                )
            },
            errorMessage = if (passwordFieldState == LoginFieldState.ERROR) stringResource(R.string.password_too_short, 8) else null,
            modifier = Modifier.Companion.fillMaxWidth(),
        )
        AnimatedVisibility(isRegistering) {
            LoginScreenField(
                label = stringResource(R.string.confirmpassword),
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                hideInput = true,
                state = when {
                    confirmPassword.isEmpty() -> LoginFieldState.DEFAULT
                    confirmPassword == password && passwordFieldState == LoginFieldState.VALID -> LoginFieldState.VALID
                    else -> LoginFieldState.ERROR
                },
                errorMessage = if (confirmPassword.isNotBlank() && confirmPassword != password &&
                        passwordFieldState == LoginFieldState.VALID) stringResource(R.string.password_not_matching) else null,
                icon = {
                    Image(
                        painterResource(R.drawable.login_password),
                        contentDescription = stringResource(R.string.confirmpassword)
                    )
                },
                modifier = Modifier.Companion.fillMaxWidth().padding(top = 10.dp),
            )
        }
        AnimatedContent(showLoading) { isLoading ->
            if (isLoading) {
                HabiticaCircularProgressView(indicatorSize = 64.dp, modifier = Modifier.padding(top = 30.dp))
            } else {
                Button(
                    onSubmit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Companion.White,
                        contentColor = colorResource(R.color.gray_50),
                        disabledContainerColor = Color.White.copy(alpha = 0.5f),
                        disabledContentColor = colorResource(R.color.gray_50)
                    ),
                    shape = HabiticaTheme.shapes.large,
                    contentPadding = PaddingValues(15.dp),
                    enabled = if (isRegistering) {
                        (emailFieldState == LoginFieldState.VALID && passwordFieldState == LoginFieldState.VALID && password == confirmPassword)
                    } else {
                        (email.isNotBlank() && password.isNotBlank())
                    },
                    modifier = Modifier.Companion.fillMaxWidth().padding(top = 30.dp)
                ) {
                    if (isRegistering) {
                        Text(
                            stringResource(R.string.action_continue),
                            fontWeight = FontWeight.Companion.Bold,
                            fontSize = 18.sp
                        )
                    } else {
                        Text(
                            stringResource(R.string.login_btn), fontWeight = FontWeight.Companion.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
        AnimatedVisibility(!isRegistering) {
            Button(
                onGoogleLoginClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Companion.White,
                    contentColor = colorResource(R.color.gray_50)
                ),
                shape = HabiticaTheme.shapes.large,
                contentPadding = PaddingValues(15.dp),
                modifier = Modifier.Companion.widthIn(max = 480.dp).fillMaxWidth().padding(top = 10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Image(
                        painterResource(R.drawable.googleg_standard_color_18),
                        contentDescription = null
                    )
                    Text(
                        stringResource(R.string.continue_with_google),
                        fontWeight = FontWeight.Companion.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
        AnimatedVisibility(!isRegistering) {
            Button(
                onForgotPasswordClicked,
                colors = ButtonDefaults.textButtonColors(),
                contentPadding = PaddingValues(15.dp),
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.forgot_pw_btn),
                    color = colorResource(R.color.white),
                    fontSize = 18.sp
                )
            }
        }
    }
}
