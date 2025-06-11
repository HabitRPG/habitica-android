package com.habitrpg.android.habitica.ui.views.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.theme.HabiticaTheme

@Composable
fun LoginInitialButtons(onLoginClicked: () -> Unit,
                        onRegisterClicked: () -> Unit,
                        modifier: Modifier = Modifier.Companion
) {
    Column(
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            {
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Companion.White,
                contentColor = colorResource(R.color.gray_50)
            ),
            shape = HabiticaTheme.shapes.large,
            contentPadding = PaddingValues(15.dp),
            modifier = Modifier.Companion.widthIn(max = 480.dp).fillMaxWidth()
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
        Button(
            {
                onRegisterClicked()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Companion.White,
                contentColor = colorResource(R.color.gray_50)
            ),
            shape = HabiticaTheme.shapes.large,
            contentPadding = PaddingValues(15.dp),
            modifier = Modifier.Companion.widthIn(max = 480.dp).fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Image(painterResource(R.drawable.ic_email_color), contentDescription = null)
                Text(
                    stringResource(R.string.continue_with_email),
                    fontWeight = FontWeight.Companion.Bold,
                    fontSize = 18.sp
                )
            }
        }
        Button(
            {
                onLoginClicked()
            },
            colors = ButtonDefaults.textButtonColors(),
            contentPadding = PaddingValues(15.dp),
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.already_have_an_account),
                    color = colorResource(R.color.brand_600),
                    fontSize = 18.sp
                )
                Text(stringResource(R.string.login_btn), color = colorResource(R.color.white),
                    fontSize = 18.sp)
            }
        }
    }
}
