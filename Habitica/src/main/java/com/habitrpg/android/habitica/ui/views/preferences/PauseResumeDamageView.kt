package com.habitrpg.android.habitica.ui.views.preferences

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.android.habitica.ui.views.HabiticaButton
import com.habitrpg.common.habitica.theme.HabiticaTheme

@Composable
fun PauseResumeDamageView(
    isPaused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HabiticaTheme.colors
    val mainTextColor = colors.textPrimary
    Column(
        horizontalAlignment = Alignment.Start,
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (isPaused) {
            Text(
                stringResource(R.string.resume_damage),
                color = HabiticaTheme.colors.textSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier =
                Modifier
                    .padding(bottom = 18.dp)
                    .fillMaxWidth()
            )
            Text(
                stringResource(R.string.resume_damage_1_title),
                color = HabiticaTheme.colors.textPrimary,
                fontSize = 16.sp
            )
            Text(
                stringResource(R.string.resume_damage_1_description),
                color = HabiticaTheme.colors.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                stringResource(R.string.resume_damage_2_title),
                color = HabiticaTheme.colors.textPrimary,
                fontSize = 16.sp
            )
            Text(
                stringResource(R.string.resume_damage_2_description),
                color = HabiticaTheme.colors.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                stringResource(R.string.resume_damage_3_title),
                color = HabiticaTheme.colors.textPrimary,
                fontSize = 16.sp
            )
            Text(
                stringResource(R.string.resume_damage_3_description),
                color = HabiticaTheme.colors.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 18.dp)
            )
            HabiticaButton(
                background = colorResource(R.color.yellow_100),
                color = colorResource(R.color.yellow_1),
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                onClick = { onClick() }
            ) {
                Text(stringResource(R.string.resume_damage))
            }
        } else {
            Text(
                stringResource(R.string.pause_damage),
                fontSize = 21.sp,
                color = mainTextColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier =
                Modifier
                    .padding(bottom = 18.dp)
                    .fillMaxWidth()
            )
            Text(
                stringResource(R.string.pause_damage_1_title),
                color = HabiticaTheme.colors.textPrimary,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Text(
                stringResource(R.string.pause_damage_1_description),
                color = HabiticaTheme.colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                stringResource(R.string.pause_damage_2_title),
                color = HabiticaTheme.colors.textPrimary,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Text(
                stringResource(R.string.pause_damage_2_description),
                color = HabiticaTheme.colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
            Text(
                stringResource(R.string.pause_damage_3_title),
                color = HabiticaTheme.colors.textPrimary,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Text(
                stringResource(R.string.pause_damage_3_description),
                color = HabiticaTheme.colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            HabiticaButton(
                background = colorResource(R.color.yellow_100),
                color = colorResource(R.color.yellow_1),
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                onClick = { onClick() }
            ) {
                Text(stringResource(R.string.pause_damage))
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    name = "Pause Damage (isPaused = false)",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PauseDamagePreview() {
    HabiticaTheme {
        PauseResumeDamageView(
            isPaused = false,
            onClick = {}
        )
    }
}
