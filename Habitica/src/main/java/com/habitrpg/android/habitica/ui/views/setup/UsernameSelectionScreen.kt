package com.habitrpg.android.habitica.ui.views.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.viewmodels.AuthenticationViewModel
import com.habitrpg.android.habitica.ui.views.LoginFieldState
import com.habitrpg.android.habitica.ui.views.LoginScreenField
import com.habitrpg.common.habitica.theme.HabiticaTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun <T> T.useDebounce(
    delayMillis: Long = 300L,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onChange: (T) -> Unit
): T{
    val state by rememberUpdatedState(this)

    DisposableEffect(state){
        val job = coroutineScope.launch {
            delay(delayMillis)
            onChange(state)
        }
        onDispose {
            job.cancel()
        }
    }
    return state
}

@Composable
fun UsernameSelectionScreen(
    authenticationViewModel: AuthenticationViewModel,
    onPreviousOnboardingStep: () -> Unit,
    onNextOnboardingStep: () -> Unit
) {
    var username by authenticationViewModel.username
    val isUsernameValid by authenticationViewModel.isUsernameValid.collectAsState(null)
    val usernameIssues by authenticationViewModel.usernameIssues.collectAsState(null)

    username.useDebounce {
        if (it.length > 2) {
            authenticationViewModel.checkUsername(it)
        }
    }

    var acceptedTerms by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.brand_300))
            .padding(WindowInsets.ime.union(WindowInsets.systemBars).asPaddingValues())
    ) {
        Button(
            {
                onPreviousOnboardingStep()
            },
            colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Image(painterResource(R.drawable.arrow_back), contentDescription = null)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp)
                .padding(horizontal = 20.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.header_verify_username),
                contentDescription = null,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )
            Text(
                text = stringResource(R.string.what_should_call_you),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier
            )
            LoginScreenField(
                stringResource(R.string.username),
                value = username,
                prefix = {
                    Text("@", fontSize = 16.sp, color = colorResource(R.color.brand_600), modifier = Modifier.padding(end = 8.dp))
                },
                state = when (isUsernameValid) {
                    true -> LoginFieldState.VALID
                    false -> LoginFieldState.ERROR
                    else -> LoginFieldState.DEFAULT
                },
                onValueChange = {
                                username = it
                                authenticationViewModel.invalidateUsernameState()
                                },
                modifier = Modifier
            )
            AnimatedVisibility(usernameIssues?.isNotBlank() == true) {
                Text(
                    text = usernameIssues ?: "",
                    fontSize = 16.sp,
                    color = colorResource(R.color.red_500),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                )
            }
            Text(
                text = stringResource(R.string.username_description),
                fontSize = 16.sp,
                color = colorResource(R.color.brand_600),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            )
            Spacer(Modifier.weight(1f))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            acceptedTerms = !acceptedTerms
                        }
                        .background(colorResource(R.color.brand_100), shape = HabiticaTheme.shapes.small)) {
                    if (acceptedTerms) {
                        Image(
                            painter = painterResource(R.drawable.checkmark),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }
                }
                Text(
                    AnnotatedString.fromHtml(
                        stringResource(R.string.register_tos_confirm),
                        linkStyles = TextLinkStyles(style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.white)
                        ))
                    ),
                    fontSize = 16.sp,
                    color = colorResource(R.color.brand_600),
                    fontWeight = FontWeight.Normal,
                )
            }
            Button(
                onClick = onNextOnboardingStep,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = colorResource(R.color.gray_50),
                    disabledContainerColor = Color.White.copy(alpha = 0.5f),
                    disabledContentColor = colorResource(R.color.gray_50)
                ),
                enabled = isUsernameValid == true && acceptedTerms,
                shape = HabiticaTheme.shapes.large,
                contentPadding = PaddingValues(15.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(stringResource(R.string.get_started), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
