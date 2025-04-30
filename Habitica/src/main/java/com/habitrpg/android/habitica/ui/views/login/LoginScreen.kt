package com.habitrpg.android.habitica.ui.views.login

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.viewmodels.AuthenticationViewModel
import com.habitrpg.android.habitica.ui.views.LoginFieldState
import com.habitrpg.common.habitica.extensions.layoutInflater

enum class LoginScreenState {
    INITIAL,
    LOGIN,
    REGISTER,
}

@Composable
fun LoginScreen(onNextOnboardingStep: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val viewModel: AuthenticationViewModel = viewModel()
    val scrollState = rememberScrollState()
    var loginScreenState by remember { mutableStateOf(LoginScreenState.INITIAL) }
    var password by remember { mutableStateOf("") }
    var passwordFieldState by remember { mutableStateOf(LoginFieldState.DEFAULT) }
    var email by remember { mutableStateOf("") }
    var emailFieldState by remember { mutableStateOf(LoginFieldState.DEFAULT) }
    Box(modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                context.layoutInflater.inflate(R.layout.login_background, null)
            },
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier.fillMaxSize().align(Alignment.BottomCenter)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painterResource(R.drawable.login_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(800.dp)
            )
            AnimatedVisibility(
                loginScreenState == LoginScreenState.INITIAL,
                enter = expandVertically(tween(300, 400), expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp).background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFA995EA),
                                    colorResource(R.color.brand_400)
                                )
                            )
                        )
                )
            }
        }
        AnimatedVisibility(
            loginScreenState != LoginScreenState.INITIAL,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.padding(WindowInsets.systemBars.asPaddingValues())
        ) {
            Button(
                {
                    loginScreenState = LoginScreenState.INITIAL
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Image(painterResource(R.drawable.arrow_back), contentDescription = null)
            }
        }
        val logoPadding by animateDpAsState(
            if (loginScreenState == LoginScreenState.INITIAL) {
                120.dp
            } else {
                50.dp
            },
            animationSpec = tween(
                delayMillis = if (loginScreenState == LoginScreenState.INITIAL) 400 else 0,
                easing = EaseInOut
            ),
            label = "padding"
        )
        ProvideTextStyle(TextStyle(fontSize = 18.sp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(WindowInsets.systemBars.asPaddingValues()).padding(horizontal = 20.dp)
            ) {
                Image(
                    painterResource(R.drawable.login_logo),
                    contentDescription = null,
                    modifier = Modifier.padding(top = logoPadding)
                )
                AnimatedVisibility(
                    loginScreenState == LoginScreenState.INITIAL,
                    enter = fadeIn(tween(300, 500)) + expandVertically(tween(300, 500)),
                    exit = fadeOut()
                ) {
                    Text(
                        stringResource(R.string.enjoy_getting_things_done),
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(R.color.brand_600),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 30.dp)
                            .padding(horizontal = 30.dp)
                    )
                }
                AnimatedVisibility(
                    loginScreenState != LoginScreenState.INITIAL,
                    enter = fadeIn(tween(300, 400)),
                    exit = fadeOut()
                ) {
                    LoginForm(
                        onToggleFormType = {
                            loginScreenState =
                                if (loginScreenState == LoginScreenState.LOGIN) LoginScreenState.REGISTER else LoginScreenState.LOGIN
                        },
                        email = email,
                        emailFieldState = emailFieldState,
                        onEmailChange = {
                            email = it
                            if (loginScreenState == LoginScreenState.REGISTER) {
                                emailFieldState = if (it.isEmpty()) {
                                    LoginFieldState.DEFAULT
                                } else if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                                    LoginFieldState.VALID
                                } else {
                                    LoginFieldState.ERROR
                                }
                            } else {
                                emailFieldState = LoginFieldState.DEFAULT
                            }
                        },
                        password = password,
                        passwordFieldState = passwordFieldState,
                        onPasswordChange = {
                            password = it
                            if (loginScreenState == LoginScreenState.REGISTER) {
                                passwordFieldState = if (it.isEmpty()) {
                                    LoginFieldState.DEFAULT
                                } else if (it.length >= 8) {
                                    LoginFieldState.VALID
                                } else {
                                    LoginFieldState.ERROR
                                }
                            } else {
                                passwordFieldState = LoginFieldState.DEFAULT
                            }
                        },
                        isRegistering = loginScreenState == LoginScreenState.REGISTER,
                        onSubmit = {
                            if (loginScreenState == LoginScreenState.REGISTER) {
                                // TODO: Implement registration
                            } else {
                                viewModel.login(email, password)
                            }
                        },
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                AnimatedVisibility(
                    loginScreenState == LoginScreenState.INITIAL,
                    enter = fadeIn(tween(300, 500)) + expandVertically(tween(300, 400)),
                    exit = fadeOut() + shrinkVertically(tween(300, 200))
                ) {
                    LoginInitialButtons({
                        loginScreenState = LoginScreenState.LOGIN
                    }, {
                        loginScreenState = LoginScreenState.REGISTER
                    })
                }
            }
        }
    }
}
