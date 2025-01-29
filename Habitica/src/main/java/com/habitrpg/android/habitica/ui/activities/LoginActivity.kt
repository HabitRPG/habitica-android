package com.habitrpg.android.habitica.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityLoginBinding
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.extensions.lifecycleLaunchWhen
import com.habitrpg.android.habitica.extensions.updateStatusBarColor
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.extensions.AuthenticationErrors
import com.habitrpg.android.habitica.ui.viewmodels.AuthenticationViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    val viewModel by viewModels<AuthenticationViewModel>()

    @Inject
    lateinit var configManager: AppConfigManager

    private var isShowingForm: Boolean = false

    override fun getLayoutResId(): Int {
        return R.layout.activity_login
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        // Set default values to avoid null-responses when requesting unedited settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences_fragment, false)

        configureSpecialUI()
        binding.backgroundContainer.isScrollable = false

        setupOnClickListeners()
        setupViewmodelObserving()
    }

    private fun setupViewmodelObserving() {
        lifecycleLaunchWhen(Lifecycle.State.RESUMED) {
            viewModel.showAuthProgress.collect { showProgress ->
                binding.progressView.isVisible = showProgress
            }
        }
        lifecycleLaunchWhen(Lifecycle.State.RESUMED) {
            viewModel.isRegistering.collect { isRegistering ->
                if (isRegistering) {
                    configureForRegistering()
                } else {
                    configureForLogin()
                }
            }
        }
        lifecycleLaunchWhen(Lifecycle.State.RESUMED) {
            viewModel.authenticationError
                .filterNotNull()
                .collect { showError(it) }
        }
        lifecycleLaunchWhen(Lifecycle.State.RESUMED) {
            viewModel.authenticationSuccess
                .filterNotNull()
                .collect { didRegister ->
                    if (didRegister) {
                        startSetupActivity()
                    } else {
                        startMainActivity()
                    }
                }
        }
    }

    private fun setupOnClickListeners() {
        onBackPressedDispatcher.addCallback(this) {
            if (isShowingForm) {
                hideForm()
            } else {
                finish()
            }
        }
        binding.newGameButton.setOnClickListener { newGameButtonClicked() }
        binding.showLoginButton.setOnClickListener { showLoginButtonClicked() }
        binding.backButton.setOnClickListener { backButtonClicked() }
        binding.forgotPassword.setOnClickListener { onForgotPasswordClicked() }
        binding.googleLoginButton.setOnClickListener {
            viewModel.startGoogleAuth(this)
        }
        binding.submitButton.setOnClickListener {
            if (viewModel.isRegistering.value) {
                registerWithPassword()
            } else {
                loginWithPassword()
            }
        }
    }

    private fun configureSpecialUI() {
        val content = SpannableString(binding.forgotPassword.text)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        binding.forgotPassword.text = content
        binding.privacyPolicy.movementMethod = LinkMovementMethod.getInstance()

        binding.backgroundContainer.post {
            binding.backgroundContainer.scrollTo(
                0,
                binding.backgroundContainer.bottom,
            )
        }
    }

    override fun loadTheme(
        sharedPreferences: SharedPreferences,
        forced: Boolean,
    ) {
        super.loadTheme(sharedPreferences, forced)
        window.updateStatusBarColor(R.color.black_20_alpha, false)
    }

    private fun resetLayout() {
        val isRegistering = viewModel.isRegistering.value
        binding.email.isVisible = isRegistering
        binding.confirmPassword.isVisible = isRegistering
    }

    private fun startMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun startSetupActivity() {
        val intent = Intent(this@LoginActivity, SetupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun configureForRegistering() {
        binding.submitButton.text = getString(R.string.register_btn)
        binding.username.setHint(R.string.username)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.username.setAutofillHints("newUsername")
            binding.password.setAutofillHints("newPassword")
        }
        binding.password.imeOptions = EditorInfo.IME_ACTION_NEXT
        binding.googleLoginButton.setText(R.string.register_btn_google)

        this.resetLayout()
    }

    private fun configureForLogin() {
        binding.submitButton.text = getString(R.string.login_btn)
        binding.username.setHint(R.string.email_username)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.username.setAutofillHints("username")
            binding.password.setAutofillHints("password")
        }
        binding.password.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.googleLoginButton.setText(R.string.login_btn_google)
        this.resetLayout()
    }

    private fun loginWithPassword() {
        val username: String = binding.username.text.toString()
        val password: String = binding.password.text.toString()
        viewModel.validateInputs(username, password)?.let {
            showError(it)
            return
        }
        viewModel.login(username, password)
    }

    private fun registerWithPassword() {
        val username = binding.username.text.toString()
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()
        viewModel.validateInputs(username, password, email, confirmPassword)?.let {
            showError(it)
            return
        }
        viewModel.register(username, email, password, confirmPassword)
    }

    private fun sendAuthToWearables(response: UserAuthResponse) {
        lifecycleScope.launch(Dispatchers.IO) {
            val messageClient: MessageClient = Wearable.getMessageClient(this@LoginActivity)
            val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(this@LoginActivity)
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

    private fun newGameButtonClicked() {
        viewModel.isRegistering.value = true
        showForm()
    }

    private fun showLoginButtonClicked() {
        viewModel.isRegistering.value = false
        showForm()
    }

    private fun backButtonClicked() {
        hideForm()
    }

    private fun showForm() {
        isShowingForm = true
        val panAnimation =
            ObjectAnimator.ofInt(binding.backgroundContainer, "scrollY", 0).setDuration(1000)
        val newGameAlphaAnimation =
            ObjectAnimator.ofFloat(binding.newGameButton, View.ALPHA, 0.toFloat())
        val showLoginAlphaAnimation =
            ObjectAnimator.ofFloat(binding.showLoginButton, View.ALPHA, 0.toFloat())
        val scaleLogoAnimation =
            ValueAnimator.ofInt(
                binding.logoView.measuredHeight,
                (binding.logoView.measuredHeight * 0.75).toInt(),
            )
        scaleLogoAnimation.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as? Int ?: 0
            val layoutParams = binding.logoView.layoutParams
            layoutParams.height = value
            binding.logoView.layoutParams = layoutParams
        }
        if (viewModel.isRegistering.value) {
            newGameAlphaAnimation.startDelay = 600
            newGameAlphaAnimation.duration = 400
            showLoginAlphaAnimation.duration = 400
            newGameAlphaAnimation.addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.newGameButton.visibility = View.GONE
                        binding.showLoginButton.visibility = View.GONE
                        binding.loginScrollview.visibility = View.VISIBLE
                        binding.loginScrollview.alpha = 1f
                    }
                },
            )
        } else {
            showLoginAlphaAnimation.startDelay = 600
            showLoginAlphaAnimation.duration = 400
            newGameAlphaAnimation.duration = 400
            showLoginAlphaAnimation.addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.newGameButton.visibility = View.GONE
                        binding.showLoginButton.visibility = View.GONE
                        binding.loginScrollview.visibility = View.VISIBLE
                        binding.loginScrollview.alpha = 1f
                    }
                },
            )
        }
        val backAlphaAnimation =
            ObjectAnimator.ofFloat(binding.backButton, View.ALPHA, 1.toFloat()).setDuration(800)
        val showAnimation = AnimatorSet()
        showAnimation.playTogether(
            panAnimation,
            newGameAlphaAnimation,
            showLoginAlphaAnimation,
            scaleLogoAnimation,
        )
        showAnimation.play(backAlphaAnimation).after(panAnimation)
        for (i in 0 until binding.formWrapper.childCount) {
            val view = binding.formWrapper.getChildAt(i)
            view.alpha = 0f
            val animator = ObjectAnimator.ofFloat(view, View.ALPHA, 1.toFloat()).setDuration(400)
            animator.startDelay = (100 * i).toLong()
            showAnimation.play(animator).after(panAnimation)
        }

        showAnimation.start()
    }

    private fun hideForm() {
        if (!isShowingForm) {
            return
        }
        isShowingForm = false
        val panAnimation =
            ObjectAnimator.ofInt(
                binding.backgroundContainer,
                "scrollY",
                binding.backgroundContainer.bottom,
            ).setDuration(1000)
        val newGameAlphaAnimation =
            ObjectAnimator.ofFloat(binding.newGameButton, View.ALPHA, 1.toFloat()).setDuration(700)
        val showLoginAlphaAnimation =
            ObjectAnimator.ofFloat(binding.showLoginButton, View.ALPHA, 1.toFloat())
                .setDuration(700)
        val scaleLogoAnimation =
            ValueAnimator.ofInt(
                binding.logoView.measuredHeight,
                (binding.logoView.measuredHeight * 1.333333).toInt(),
            )
        scaleLogoAnimation.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as? Int
            val layoutParams = binding.logoView.layoutParams
            layoutParams.height = value ?: 0
            binding.logoView.layoutParams = layoutParams
        }
        showLoginAlphaAnimation.startDelay = 300
        val scrollViewAlphaAnimation =
            ObjectAnimator.ofFloat(binding.loginScrollview, View.ALPHA, 0.toFloat())
                .setDuration(800)
        scrollViewAlphaAnimation.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.newGameButton.visibility = View.VISIBLE
                    binding.showLoginButton.visibility = View.VISIBLE
                    binding.loginScrollview.visibility = View.INVISIBLE
                }
            },
        )
        val backAlphaAnimation =
            ObjectAnimator.ofFloat(binding.backButton, View.ALPHA, 0.toFloat()).setDuration(800)
        val showAnimation = AnimatorSet()
        showAnimation.playTogether(
            panAnimation,
            scrollViewAlphaAnimation,
            backAlphaAnimation,
            scaleLogoAnimation,
        )
        showAnimation.play(newGameAlphaAnimation).after(scrollViewAlphaAnimation)
        showAnimation.play(showLoginAlphaAnimation).after(scrollViewAlphaAnimation)
        showAnimation.start()
        dismissKeyboard()
    }

    private fun onForgotPasswordClicked() {
        val input = EditText(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            input.setAutofillHints(EditText.AUTOFILL_HINT_EMAIL_ADDRESS)
        }
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
