package com.habitrpg.android.habitica.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityLoginBinding
import com.habitrpg.android.habitica.extensions.AuthenticationErrors
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.extensions.setNavigationBarDarkIcons
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.viewmodels.AuthenticationViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.intro.IntroScreen
import com.habitrpg.android.habitica.ui.views.login.LoginScreen
import com.habitrpg.android.habitica.ui.views.setup.SetupScreen
import com.habitrpg.android.habitica.ui.views.setup.UsernameSelectionScreen
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import com.habitrpg.common.habitica.theme.HabiticaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

enum class OnboardingSteps {
    INTRO,
    LOGIN,
    USERNAME,
    SETUP
}

@AndroidEntryPoint
class OnboardingActivity: BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    val authenticationViewModel: AuthenticationViewModel by viewModels()

    val currentStep = mutableStateOf(OnboardingSteps.LOGIN)

    @Inject
    lateinit var configManager: AppConfigManager

    override fun getLayoutResId(): Int {
        return R.layout.activity_login
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        // Set default values to avoid null-responses when requesting unedited settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences_fragment, false)

        if (authenticationViewModel.hostConfig.hasAuthentication()) {
            startMainActivity()
            return
        }

        binding.composeView.setContent {
            val step by currentStep
            HabiticaTheme {
                AnimatedContent(step,
                    transitionSpec = {
                        (expandVertically(
                            initialHeight = { fullHeight -> (fullHeight * 0.3f).roundToInt() }
                        )+fadeIn())
                            .togetherWith(
                                slideOutVertically(
                                    targetOffsetY = { fullHeight -> (-fullHeight * 0.1f).roundToInt() }
                                )
                            )
            },) {
                    when (it) {
                        OnboardingSteps.INTRO -> IntroScreen({
                            currentStep.value = OnboardingSteps.LOGIN
                        })
                        OnboardingSteps.LOGIN -> LoginScreen(authenticationViewModel,{ newUser ->
                            if (newUser) {
                                currentStep.value = OnboardingSteps.USERNAME
                            } else {
                                startMainActivity()
                            }
                        })
                        OnboardingSteps.USERNAME -> UsernameSelectionScreen(authenticationViewModel,
                            {
                                currentStep.value = OnboardingSteps.LOGIN
                            }, {
                                currentStep.value = OnboardingSteps.SETUP
                            }
                        )
                        OnboardingSteps.SETUP -> SetupScreen({
                            startMainActivity()
                        })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightNavigationBars = false
            controller.isAppearanceLightStatusBars = false
            window.setNavigationBarDarkIcons(false)
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this@OnboardingActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun sendAuthToWearables(response: UserAuthResponse) {
        lifecycleScope.launch(Dispatchers.IO) {
            val messageClient: MessageClient = Wearable.getMessageClient(this@OnboardingActivity)
            val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(this@OnboardingActivity)
            try {
                val info =
                    Tasks.await(
                        capabilityClient.getCapability(
                            "receive_message",
                            CapabilityClient.FILTER_REACHABLE,
                        ),
                    )
                info.nodes.forEach {
                    Tasks.await(
                        messageClient.sendMessage(
                            it.id,
                            "/auth",
                            "${response.id}:${response.apiToken}".toByteArray(),
                        ),
                    )
                }
            } catch (_: Exception) {
                // Wearable API is not available on this device.
            }
        }
    }

    private fun showError(error: AuthenticationErrors) {
        val alert = HabiticaAlertDialog(this)
        if (error.isValidationError) {
            alert.setTitle(R.string.login_validation_error_title)
        } else {
            alert.setTitle(R.string.authentication_error_title)
        }
        alert.setMessage(error.translatedMessage(this))
        alert.addOkButton()
        alert.show()
    }

    private fun onForgotPasswordClicked() {
        val input = EditText(this)
        input.setAutofillHints(EditText.AUTOFILL_HINT_EMAIL_ADDRESS)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.hint = getString(R.string.forgot_password_hint_example)
        input.textSize = 16f
        val lp =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
            )
        input.layoutParams = lp
        val alertDialog = HabiticaAlertDialog(this)
        alertDialog.setTitle(R.string.forgot_password_title)
        alertDialog.setMessage(R.string.forgot_password_description)
        alertDialog.setAdditionalContentView(input)
        alertDialog.addButton(R.string.send, true) { _, _ ->
            lifecycleScope.launchCatching {
                userRepository.sendPasswordResetEmail(input.text.toString())
                showPasswordEmailConfirmation()
            }
        }
        alertDialog.addCancelButton()
        alertDialog.show()
    }

    private fun showPasswordEmailConfirmation() {
        val alert = HabiticaAlertDialog(this)
        alert.setMessage(R.string.forgot_password_confirmation)
        alert.addOkButton()
        alert.show()
    }

    override fun finish() {
        dismissKeyboard()
        super.finish()
    }
}

fun AuthenticationErrors.translatedMessage(context: Context): String {
    return when (this) {
        AuthenticationErrors.GET_CREDENTIALS_ERROR -> context.getString(R.string.auth_get_credentials_error)
        AuthenticationErrors.INVALID_CREDENTIALS -> context.getString(R.string.auth_invalid_credentials)

        AuthenticationErrors.MISSING_FIELDS -> context.getString(R.string.login_validation_error_fieldsmissing)
        AuthenticationErrors.PASSWORD_MISMATCH -> context.getString(R.string.password_not_matching)
        AuthenticationErrors.PASSWORD_TOO_SHORT -> context.getString(R.string.password_too_short, minPasswordLength)
    }
}
