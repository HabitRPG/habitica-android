package com.habitrpg.android.habitica.ui.views.login

import android.util.Patterns
import android.widget.Toast
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
fun LoginScreen(authenticationViewModel: AuthenticationViewModel, onNextOnboardingStep: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val showLoading by authenticationViewModel.showAuthProgress.collectAsState(false)
    val authenticationError by authenticationViewModel.authenticationError.collectAsState(null)

    LaunchedEffect(authenticationViewModel) {
        authenticationViewModel.authenticationSuccess.collect { isRegistering ->
            onNextOnboardingStep(isRegistering)
        }
    }

    var loginScreenState by remember { mutableStateOf(LoginScreenState.INITIAL) }
    var password by authenticationViewModel.password
    var passwordFieldState by remember { mutableStateOf(LoginFieldState.DEFAULT) }
    var email by authenticationViewModel.email
    var emailFieldState by remember { mutableStateOf(LoginFieldState.DEFAULT) }
    var showServerDialog by remember { mutableStateOf(false) }
    var customServerUrl by remember { mutableStateOf(authenticationViewModel.currentServerSelection()) }
    var serverError by remember { mutableStateOf<String?>(null) }
    val invalidServerMessage = stringResource(R.string.custom_server_invalid)
    val context = LocalContext.current
    var devTapCount by remember { mutableStateOf(0) }
    var devOptionsUnlocked by remember { mutableStateOf(authenticationViewModel.isDevOptionsUnlocked()) }
    val requiredTapCount = 7

    LaunchedEffect(showServerDialog) {
        if (showServerDialog) {
            customServerUrl = authenticationViewModel.currentServerSelection()
            serverError = null
            devTapCount = 0
        }
    }
    Box(modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                val layout = context.layoutInflater.inflate(R.layout.login_background, null)
                (layout as? LockableScrollView)?.isScrollable = false
                layout
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
        Button(
            {
                if (showServerDialog) {
                    return@Button
                }
                if (devOptionsUnlocked) {
                    showServerDialog = true
                    return@Button
                }
                val nextCount = devTapCount + 1
                if (nextCount >= requiredTapCount) {
                    devTapCount = 0
                    devOptionsUnlocked = true
                    authenticationViewModel.setDevOptionsUnlocked(true)
                    Toast.makeText(context, context.getString(R.string.dev_options_unlocked), Toast.LENGTH_SHORT).show()
                    showServerDialog = true
                } else {
                    devTapCount = nextCount
                    val remaining = requiredTapCount - nextCount
                    Toast.makeText(
                        context,
                        context.resources.getQuantityString(R.plurals.dev_options_taps_remaining, remaining, remaining),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
            modifier = Modifier.align(Alignment.TopEnd).padding(WindowInsets.systemBars.asPaddingValues())
        ) {
            Image(
                painterResource(R.drawable.menu_settings),
                contentDescription = stringResource(R.string.custom_server_content_description)
            )
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
                                authenticationViewModel.register("", email, password, password)
                            } else {
                                authenticationViewModel.login(email, password)
                            }
                        },
                        showLoading = showLoading
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
    if (showServerDialog) {
        val dialogContainer = Color(0xFF3B3B3B)
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = { Text(stringResource(R.string.custom_server_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.dev_options_warning),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    val containerColor = Color(0xFF3B3B3B)
                    OutlinedTextField(
                        value = customServerUrl,
                        onValueChange = { customServerUrl = it },
                        label = { Text(stringResource(R.string.custom_server_label), color = Color.White) },
                        placeholder = { Text(stringResource(R.string.custom_server_placeholder), color = Color.White.copy(alpha = 0.6f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = containerColor,
                            unfocusedContainerColor = containerColor,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    if (serverError != null) {
                        Text(
                            serverError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            },
            containerColor = dialogContainer,
            tonalElevation = 8.dp,
            confirmButton = {
                TextButton(
                    onClick = {
                        val applied = authenticationViewModel.applyServerOverride(customServerUrl)
                        if (applied) {
                            serverError = null
                            showServerDialog = false
                        } else {
                            serverError = invalidServerMessage
                        }
                    }
                ) {
                    Text(stringResource(R.string.custom_server_apply))
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            authenticationViewModel.resetServerOverride()
                            customServerUrl = authenticationViewModel.currentServerSelection()
                            serverError = null
                            showServerDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.custom_server_reset))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showServerDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            }
        )
    }
}
