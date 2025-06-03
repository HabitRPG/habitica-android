package com.habitrpg.android.habitica.ui.views

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

@Composable
fun ApiTokenBottomSheet(
    apiToken: String,
    onCopyToken: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HabiticaTheme.colors

    val background = colors.windowBackground
    val fieldBackground = colors.contentBackground
    val mainText = colors.textPrimary
    val secondaryText = colors.textSecondary
    val buttonBg = colors.tintedUiMain
    val buttonText = colors.tintedUiDetails
    val lockIconColor = colors.textSecondary

    Box(
        modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(22.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                Modifier
                    .size(width = 40.dp, height = 6.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(50))
                    .background(colors.contentBackgroundOffset)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(id = R.string.api_token_title),
                color = mainText,
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                stringResource(id = R.string.api_token_is_password),
                color = mainText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
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
                color = mainText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
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
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = lockIconColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    apiToken,
                    color = mainText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Button(
                onClick = { onCopyToken(apiToken) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    stringResource(id = R.string.copy_token),
                    color = buttonText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 550)
@Composable
fun ApiTokenBottomSheetPreview() {
    HabiticaTheme {
        ApiTokenBottomSheet(
            apiToken = "sample_api_token_1234567890",
            onCopyToken = {}
        )
    }
}
