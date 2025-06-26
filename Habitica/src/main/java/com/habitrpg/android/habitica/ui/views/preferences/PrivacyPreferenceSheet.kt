package com.habitrpg.android.habitica.ui.views.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.HabiticaCircularProgressView

@Composable
fun PrivacyPreferenceSheet(analyticsConsent: Boolean, onConsentChanged: (Boolean) -> Unit, isSettingConsent: Boolean,
                           modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp)
        ) {
            Text(
                stringResource(R.string.your_privacy_preferences),
                color = HabiticaTheme.colors.textSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .padding(bottom = 18.dp)
                        .fillMaxWidth()
            )
            Text(
                stringResource(R.string.your_privacy_preferences_description),
                color = HabiticaTheme.colors.textPrimary,
                fontSize = 16.sp,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                modifier =
                    Modifier
                        .padding(bottom = 18.dp)
                        .fillMaxWidth()
            )
        }
        PrivacyToggleView(
            title = stringResource(R.string.performance_analytics),
            description = stringResource(R.string.performance_analytics_description),
            isChecked = analyticsConsent,
            onCheckedChange = { onConsentChanged(it) },
            isSetting = isSettingConsent,
            modifier = Modifier.padding(bottom = 8.dp)
            )
        PrivacyToggleView(
            title = stringResource(R.string.strictlye_necessary_analytics),
            description = stringResource(R.string.strictlye_necessary_analytics_description),
            isChecked = true,
            onCheckedChange = {},
            disabled = true
        )
    }
}

@Composable
fun PrivacyToggleView(title: String, description: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, disabled: Boolean = false, isSetting: Boolean = false) {
    Row(modifier = modifier.fillMaxWidth()
        .background(colorResource(if (LocalContext.current.isUsingNightModeResources()) R.color.gray_10 else R.color.gray_700), RoundedCornerShape(16.dp))
        .padding(vertical =16.dp)
        .padding(start = 16.dp, end = 12.dp)) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                color = HabiticaTheme.colors.textPrimary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = description,
                style = HabiticaTheme.typography.bodyMedium,
                color = HabiticaTheme.colors.textSecondary
            )
        }
        Spacer(Modifier.width(8.dp))
        if (isSetting) {
            HabiticaCircularProgressView(indicatorSize = 48.dp, modifier = Modifier.align(Alignment.CenterVertically))
        } else {
            Switch(checked = isChecked,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = if (LocalContext.current.isUsingNightModeResources()) colorResource(R.color.brand_100) else colorResource(R.color.white),
                    uncheckedThumbColor = if (LocalContext.current.isUsingNightModeResources()) colorResource(R.color.gray_300) else colorResource(R.color.gray_200),
                    checkedTrackColor = if (LocalContext.current.isUsingNightModeResources()) colorResource(R.color.brand_400) else colorResource(R.color.brand_100),
                    checkedBorderColor = if (LocalContext.current.isUsingNightModeResources()) colorResource(R.color.brand_400) else colorResource(R.color.brand_100),
                    uncheckedTrackColor = if (LocalContext.current.isUsingNightModeResources()) colorResource(R.color.gray_50) else colorResource(R.color.gray_500),
                    uncheckedBorderColor = if (LocalContext.current.isUsingNightModeResources()) colorResource(R.color.gray_300) else colorResource(R.color.gray_200)
                ),
                onCheckedChange = { if (!disabled) onCheckedChange(!isChecked) }, modifier = Modifier.align(Alignment.CenterVertically).alpha(if (disabled) 0.5f else 1f),)
        }
    }
}
