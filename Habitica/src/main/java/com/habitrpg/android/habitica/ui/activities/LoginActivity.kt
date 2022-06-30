package com.habitrpg.android.habitica.ui.activities

import android.accounts.AccountManager
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.databinding.ActivityLoginBinding
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.extensions.updateStatusBarColor
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.viewmodels.AuthenticationViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    private lateinit var viewModel: AuthenticationViewModel
    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var sharedPrefs: SharedPreferences
    @Inject
    lateinit var configManager: AppConfigManager

    private var isRegistering: Boolean = false
    private var isShowingForm: Boolean = false

    private val loginClick = View.OnClickListener {
        binding.PBAsyncTask.visibility = View.VISIBLE
        if (isRegistering) {
            val username: String = binding.username.text.toString().trim { it <= ' ' }
            val email: String = binding.email.text.toString().trim { it <= ' ' }
            val password: String = binding.password.text.toString()
            val confirmPassword: String = binding.confirmPassword.text.toString()
            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || confirmPassword.isEmpty()) {
                showValidationError(R.string.login_validation_error_fieldsmissing)
                return@OnClickListener
            }
            if (password.length < configManager.minimumPasswordLength()) {
                showValidationError(getString(R.string.password_too_short, configManager.minimumPasswordLength()))
                return@OnClickListener
            }
            apiClient.registerUser(username, email, password, confirmPassword)
                .subscribe(
                    { handleAuthResponse(it) },
                    {
                        hideProgress()
                        RxErrorHandler.reportError(it)
                    }
                )
        } else {
            val username: String = binding.username.text.toString().trim { it <= ' ' }
            val password: String = binding.password.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                showValidationError(R.string.login_validation_error_fieldsmissing)
                return@OnClickListener
            }
            Log.d("LoginActivity", ": $username, $password")
            apiClient.connectUser(username, password).subscribe(
                { handleAuthResponse(it) },
                {
                    hideProgress()
                    RxErrorHandler.reportError(it)
                    Log.d("LoginActivity", ": ${it.message}", it)
                }
            )
        }
    }

    override fun getLayoutResId(): Int {
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        return R.layout.activity_login
    }

    override fun getContentView(): View {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = AuthenticationViewModel()
        supportActionBar?.hide()
        // Set default values to avoid null-responses when requesting unedited settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences_fragment, false)

        binding.loginBtn.setOnClickListener(loginClick)

        val content = SpannableString(binding.forgotPassword.text)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        binding.forgotPassword.text = content
        binding.privacyPolicy.movementMethod = LinkMovementMethod.getInstance()

        this.isRegistering = true

        val additionalData = HashMap<String, Any>()
        additionalData["page"] = this.javaClass.simpleName

        binding.backgroundContainer.post { binding.backgroundContainer.scrollTo(0, binding.backgroundContainer.bottom) }
        binding.backgroundContainer.isScrollable = false

        window.statusBarColor = ContextCompat.getColor(this, R.color.black_20_alpha)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        binding.newGameButton.setOnClickListener { newGameButtonClicked() }
        binding.showLoginButton.setOnClickListener { showLoginButtonClicked() }
        binding.backButton.setOnClickListener { backButtonClicked() }
        binding.forgotPassword.setOnClickListener { onForgotPasswordClicked() }
        binding.googleLoginButton.setOnClickListener { viewModel.handleGoogleLogin(this, pickAccountResult) }
        binding.appleLoginButton.setOnClickListener {
            viewModel.connectApple(supportFragmentManager) {
                handleAuthResponse(it)
            }
        }
    }

    override fun loadTheme(sharedPreferences: SharedPreferences, forced: Boolean) {
        super.loadTheme(sharedPreferences, forced)
        window.updateStatusBarColor(R.color.black_20_alpha, false)
    }

    override fun onBackPressed() {
        if (isShowingForm) {
            hideForm()
        } else {
            super.onBackPressed()
        }
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    private fun resetLayout() {
        if (this.isRegistering) {
            if (binding.email.visibility == View.GONE) {
                show(binding.email)
            }
            if (binding.confirmPassword.visibility == View.GONE) {
                show(binding.confirmPassword)
            }
        } else {
            if (binding.email.visibility == View.VISIBLE) {
                hide(binding.email)
            }
            if (binding.confirmPassword.visibility == View.VISIBLE) {
                hide(binding.confirmPassword)
            }
        }
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

    private fun toggleRegistering() {
        this.isRegistering = (!this.isRegistering)
        this.setRegistering()
    }

    private fun setRegistering() {
        if (this.isRegistering) {
            binding.loginBtn.text = getString(R.string.register_btn)
            binding.username.setHint(R.string.username)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.username.setAutofillHints("newUsername")
                binding.password.setAutofillHints("newPassword")
            }
            binding.password.imeOptions = EditorInfo.IME_ACTION_NEXT
            binding.googleLoginButton.setText(R.string.register_btn_google)
        } else {
            binding.loginBtn.text = getString(R.string.login_btn)
            binding.username.setHint(R.string.email_username)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.username.setAutofillHints("username")
                binding.password.setAutofillHints("password")
            }
            binding.password.imeOptions = EditorInfo.IME_ACTION_DONE
            binding.googleLoginButton.setText(R.string.login_btn_google)
        }
        this.resetLayout()
    }

    private fun handleAuthResponse(response: UserAuthResponse) {
        viewModel.handleAuthResponse(response)
        compositeSubscription.add(
            userRepository.retrieveUser(true)
                .subscribe({
                    handleAuthResponse(it, response.newUser)
                }, RxErrorHandler.handleEmptyError())
        )
    }

    private fun handleAuthResponse(user: User, isNew: Boolean) {
        hideProgress()
        dismissKeyboard()

        if (isRegistering) {
            FirebaseAnalytics.getInstance(this).logEvent("user_registered", null)
        }
        compositeSubscription.add(
            userRepository.retrieveUser(withTasks = true, forced = true)
                .subscribe(
                    {
                        if (isNew) {
                            this.startSetupActivity()
                        } else {
                            this.startMainActivity()
                            AmplitudeManager.sendEvent("login", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT)
                        }
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_toggleRegistering -> toggleRegistering()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun hideProgress() {
        runOnUiThread {
            binding.PBAsyncTask.visibility = View.GONE
        }
    }

    private fun showValidationError(resourceMessageString: Int) {
        showValidationError(getString(resourceMessageString))
    }

    private fun showValidationError(message: String) {
        binding.PBAsyncTask.visibility = View.GONE
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(R.string.login_validation_error_title)
        alert.setMessage(message)
        alert.addOkButton()
        alert.show()
    }

    private val pickAccountResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            viewModel.googleEmail = it?.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            viewModel.handleGoogleLoginResult(this, recoverFromPlayServicesErrorResult) { user, isNew ->
                handleAuthResponse(user, isNew)
            }
        }
    }

    private val recoverFromPlayServicesErrorResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != Activity.RESULT_CANCELED) {
            viewModel.handleGoogleLoginResult(this, null) { user, isNew ->
                handleAuthResponse(user, isNew)
            }
        }
    }

    private fun newGameButtonClicked() {
        isRegistering = true
        showForm()
        setRegistering()
    }

    private fun showLoginButtonClicked() {
        isRegistering = false
        showForm()
        setRegistering()
    }

    private fun backButtonClicked() {
        if (isShowingForm) {
            hideForm()
        }
    }

    private fun showForm() {
        isShowingForm = true
        val panAnimation = ObjectAnimator.ofInt(binding.backgroundContainer, "scrollY", 0).setDuration(1000)
        val newGameAlphaAnimation = ObjectAnimator.ofFloat(binding.newGameButton, View.ALPHA, 0.toFloat())
        val showLoginAlphaAnimation = ObjectAnimator.ofFloat(binding.showLoginButton, View.ALPHA, 0.toFloat())
        val scaleLogoAnimation = ValueAnimator.ofInt(binding.logoView.measuredHeight, (binding.logoView.measuredHeight * 0.75).toInt())
        scaleLogoAnimation.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as? Int ?: 0
            val layoutParams = binding.logoView.layoutParams
            layoutParams.height = value
            binding.logoView.layoutParams = layoutParams
        }
        if (isRegistering) {
            newGameAlphaAnimation.startDelay = 600
            newGameAlphaAnimation.duration = 400
            showLoginAlphaAnimation.duration = 400
            newGameAlphaAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.newGameButton.visibility = View.GONE
                    binding.showLoginButton.visibility = View.GONE
                    binding.loginScrollview.visibility = View.VISIBLE
                    binding.loginScrollview.alpha = 1f
                }
            })
        } else {
            showLoginAlphaAnimation.startDelay = 600
            showLoginAlphaAnimation.duration = 400
            newGameAlphaAnimation.duration = 400
            showLoginAlphaAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.newGameButton.visibility = View.GONE
                    binding.showLoginButton.visibility = View.GONE
                    binding.loginScrollview.visibility = View.VISIBLE
                    binding.loginScrollview.alpha = 1f
                }
            })
        }
        val backAlphaAnimation = ObjectAnimator.ofFloat(binding.backButton, View.ALPHA, 1.toFloat()).setDuration(800)
        val showAnimation = AnimatorSet()
        showAnimation.playTogether(panAnimation, newGameAlphaAnimation, showLoginAlphaAnimation, scaleLogoAnimation)
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
        isShowingForm = false
        val panAnimation = ObjectAnimator.ofInt(binding.backgroundContainer, "scrollY", binding.backgroundContainer.bottom).setDuration(1000)
        val newGameAlphaAnimation = ObjectAnimator.ofFloat(binding.newGameButton, View.ALPHA, 1.toFloat()).setDuration(700)
        val showLoginAlphaAnimation = ObjectAnimator.ofFloat(binding.showLoginButton, View.ALPHA, 1.toFloat()).setDuration(700)
        val scaleLogoAnimation = ValueAnimator.ofInt(binding.logoView.measuredHeight, (binding.logoView.measuredHeight * 1.333333).toInt())
        scaleLogoAnimation.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as? Int
            val layoutParams = binding.logoView.layoutParams
            layoutParams.height = value ?: 0
            binding.logoView.layoutParams = layoutParams
        }
        showLoginAlphaAnimation.startDelay = 300
        val scrollViewAlphaAnimation = ObjectAnimator.ofFloat(binding.loginScrollview, View.ALPHA, 0.toFloat()).setDuration(800)
        scrollViewAlphaAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.newGameButton.visibility = View.VISIBLE
                binding.showLoginButton.visibility = View.VISIBLE
                binding.loginScrollview.visibility = View.INVISIBLE
            }
        })
        val backAlphaAnimation = ObjectAnimator.ofFloat(binding.backButton, View.ALPHA, 0.toFloat()).setDuration(800)
        val showAnimation = AnimatorSet()
        showAnimation.playTogether(panAnimation, scrollViewAlphaAnimation, backAlphaAnimation, scaleLogoAnimation)
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
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        val alertDialog = HabiticaAlertDialog(this)
        alertDialog.setTitle(R.string.forgot_password_title)
        alertDialog.setMessage(R.string.forgot_password_description)
        alertDialog.setAdditionalContentView(input)
        alertDialog.addButton(R.string.send, true) { _, _ ->
            userRepository.sendPasswordResetEmail(input.text.toString()).subscribe({ showPasswordEmailConfirmation() }, RxErrorHandler.handleEmptyError())
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

    companion object {
        internal const val REQUEST_CODE_PICK_ACCOUNT = 1000

        fun show(v: View) {
            v.visibility = View.VISIBLE
        }

        fun hide(v: View) {
            v.visibility = View.GONE
        }
    }
}
