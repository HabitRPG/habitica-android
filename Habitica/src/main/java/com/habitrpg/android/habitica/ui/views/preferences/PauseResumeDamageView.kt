package com.habitrpg.android.habitica.ui.views.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                onClick = { onClick() }
            ) {
                Text(stringResource(R.string.resume_damage))
            }
        } else {
            Text(
                stringResource(R.string.pause_damage),
                color = HabiticaTheme.colors.textSecondary,
                fontSize = 16.sp,
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
                fontSize = 16.sp
            )
            Text(
                stringResource(R.string.pause_damage_1_description),
                color = HabiticaTheme.colors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                stringResource(R.string.pause_damage_2_title),
                color = HabiticaTheme.colors.textPrimary,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )
            Text(
                stringResource(R.string.pause_damage_2_description),
                color = HabiticaTheme.colors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                stringResource(R.string.pause_damage_3_title),
                color = HabiticaTheme.colors.textPrimary,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )
            Text(
                stringResource(R.string.pause_damage_3_description),
                color = HabiticaTheme.colors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 18.dp)
            )
            HabiticaButton(
                background = colorResource(R.color.yellow_100),
                color = colorResource(R.color.yellow_1),
                onClick = { onClick() }
            ) {
                Text(stringResource(R.string.pause_damage))
            }
        }
    }
}
