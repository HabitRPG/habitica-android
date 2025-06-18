package com.habitrpg.android.habitica.ui.views

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.theme.HabiticaTheme
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource

@Composable
fun ApiTokenBottomSheet(
    apiToken: String,
    onCopyToken: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HabiticaTheme.colors

    val background = colors.windowBackground
    val fieldBackground = colorResource(id = R.color.gray600_gray10)
    val mainTextColor = colors.textPrimary
    val secondaryText = colors.textSecondary
    val tokenTextColor = colorResource(id = R.color.gray200_gray400)
    val buttonBg = colorResource(id = R.color.yellow_100)
    val buttonText = colorResource(id = R.color.yellow_1)
    val lockIconColor = colors.textSecondary

    Box(
        modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(22.dp))
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .background(colorResource(R.color.content_background_offset))
                    .size(24.dp, 3.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(id = R.string.api_token_title),
                color = mainTextColor,
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                stringResource(id = R.string.api_token_is_password),
                color = mainTextColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(id = R.string.api_token_is_password_info),
                color = secondaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
            )
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                stringResource(id = R.string.api_token_reset_title),
                color = mainTextColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(id = R.string.api_token_reset_info),
                color = secondaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
            )
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(fieldBackground)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_lock),
                    contentDescription = stringResource(R.string.locked),
                    tint = lockIconColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    apiToken,
                    color = tokenTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            HabiticaButton(
                background = buttonBg,
                color      = buttonText,
                onClick    = { onCopyToken(apiToken) },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentPadding = PaddingValues(0.dp),
                fontSize       = 16.sp
            ) {
                Text(stringResource(id = R.string.copy_token))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 550, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ApiTokenBottomSheetPreview() {
    HabiticaTheme {
        ApiTokenBottomSheet(
            apiToken = "sample_api_token_1234567890",
            onCopyToken = {}
        )
    }
}
