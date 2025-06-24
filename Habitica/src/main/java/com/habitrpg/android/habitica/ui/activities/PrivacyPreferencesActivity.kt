package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.activity.addCallback
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityComposeBinding
import com.habitrpg.android.habitica.databinding.ActivityPartyInviteBinding
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.android.habitica.ui.views.preferences.PrivacyToggleView
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.HabiticaCircularProgressView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class PrivacyPreferencesActivity: BaseActivity() {
    private lateinit var binding: ActivityComposeBinding

    override fun getLayoutResId(): Int? {
        return R.layout.activity_compose
    }


    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityComposeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.root.setContent {
            var analyticsConsent by remember { mutableStateOf(false) }
            var isSaving by remember { mutableStateOf(false) }

            Column(
                horizontalAlignment = Alignment.Start,
                modifier =
                    Modifier
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 13.dp)
                ) {
                    Text(
                        stringResource(R.string.your_privacy_preferences),
                        color = colorResource(R.color.text_title),
                        fontSize = 30.sp,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold,
                        modifier =
                            Modifier
                                .padding(bottom = 30.dp)
                                .fillMaxWidth()
                    )
                    Text(
                        stringResource(R.string.your_privacy_preferences_description_full),
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
                    onCheckedChange = { analyticsConsent = it },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                PrivacyToggleView(
                    title = stringResource(R.string.strictlye_necessary_analytics),
                    description = stringResource(R.string.strictlye_necessary_analytics_description),
                    isChecked = true,
                    onCheckedChange = {},
                    disabled = true,
                    modifier = Modifier.padding(bottom = 18.dp)
                )
                AnimatedContent(isSaving) {
                    if (it) {
                        HabiticaCircularProgressView(modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(), indicatorSize = 80.dp)
                    } else {
                        Column {
                            Button({
                                analyticsConsent = true
                                lifecycleScope.launchCatching {
                                    delay(500)
                                    isSaving = true
                                    userRepository.updateUser("preferences.analyticsConsent", true)
                                    finish()
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.brand_400)),
                                shape = HabiticaTheme.shapes.small,
                                modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth().heightIn(60.dp)) {
                                Text(stringResource(R.string.accept_all))
                            }
                            val colors = if (LocalContext.current.isUsingNightModeResources()) {
                                ButtonDefaults.buttonColors().copy(containerColor = Color.White, contentColor = HabiticaTheme.colors.tintedUiSub)
                            } else {
                                ButtonDefaults.buttonColors().copy(containerColor = colorResource(R.color.gray_600), contentColor = HabiticaTheme.colors.textPrimary)
                            }
                            Button({
                                lifecycleScope.launchCatching {
                                    isSaving = true
                                    userRepository.updateUser("preferences.analyticsConsent", analyticsConsent)
                                    finish()
                                }
                            }, colors = colors,
                                shape = HabiticaTheme.shapes.small,
                                modifier = Modifier.padding(bottom = 27.dp).fillMaxWidth().heightIn(60.dp)) {
                                Text(stringResource(R.string.save_preferences))
                            }
                        }
                    }
                }

                Button(
                    {},
                    colors = ButtonDefaults.textButtonColors(),
                    modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()
                ) {
                    Text(stringResource(R.string.habiticas_privacy_policy))
                }
            }
        }
        onBackPressedDispatcher.addCallback {
        }
    }
}
