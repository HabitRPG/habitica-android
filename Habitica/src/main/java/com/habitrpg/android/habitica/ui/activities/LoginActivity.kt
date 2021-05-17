package com.habitrpg.android.habitica.ui.activities

import android.accounts.AccountManager
import android.animation.*
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.Scopes
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.ActivityLoginBinding
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.extensions.updateStatusBarColor
import com.habitrpg.android.habitica.helpers.*
import com.habitrpg.android.habitica.models.auth.UserAuthResponse
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.willowtreeapps.signinwithapplebutton.SignInWithAppleConfiguration
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.exceptions.Exceptions
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.IOException
import javax.inject.Inject

class LoginActivity : BaseActivity(), Consumer<UserAuthResponse> {

    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var sharedPrefs: SharedPreferences
    @Inject
    lateinit var hostConfig: HostConfig
    @Inject
    internal lateinit var userRepository: UserRepository
    @Inject
    @JvmField
    var keyHelper: KeyHelper? = null
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    @Inject
    lateinit var configManager: AppConfigManager

    private var isRegistering: Boolean = false
    private var isShowingForm: Boolean = false

    private var callbackManager = CallbackManager.Factory.create()
    private var googleEmail: String? = null
    private var loginManager = LoginManager.getInstance()

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
                    .subscribe(this@LoginActivity,
                            {
                                hideProgress()
                                RxErrorHandler.reportError(it)
                            })
        } else {
            val username: String = binding.username.text.toString().trim { it <= ' ' }
            val password: String = binding.password.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                showValidationError(R.string.login_validation_error_fieldsmissing)
                return@OnClickListener
            }
            apiClient.connectUser(username, password).subscribe(this@LoginActivity,
                    {
                        hideProgress()
                        RxErrorHandler.reportError(it)
                    })
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
            supportActionBar?.hide()
        //Set default values to avoid null-responses when requesting unedited settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences_fragment, false)

        setupFacebookLogin()

        binding.loginBtn.setOnClickListener(loginClick)

        val content = SpannableString(binding.forgotPassword.text)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        binding.forgotPassword.text = content
        binding.privacyPolicy.movementMethod = LinkMovementMethod.getInstance()

        this.isRegistering = true

        val additionalData = HashMap<String, Any>()
        additionalData["page"] = this.javaClass.simpleName
        AmplitudeManager.sendEvent("navigate", AmplitudeManager.EVENT_CATEGORY_NAVIGATION, AmplitudeManager.EVENT_HITTYPE_PAGEVIEW, additionalData)

        binding.backgroundContainer.post { binding.backgroundContainer.scrollTo(0, binding.backgroundContainer.bottom) }
        binding.backgroundContainer.isScrollable = false

        window.statusBarColor = ContextCompat.getColor(this, R.color.black_20_alpha)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        binding.newGameButton.setOnClickListener { newGameButtonClicked() }
        binding.showLoginButton.setOnClickListener { showLoginButtonClicked() }
        binding.backButton.setOnClickListener { backButtonClicked() }
        binding.forgotPassword.setOnClickListener { onForgotPasswordClicked() }
        binding.fbLoginButton.setOnClickListener { handleFacebookLogin() }
        binding.googleLoginButton.setOnClickListener { handleGoogleLogin() }
        binding.appleLoginButton.setOnClickListener {
            val configuration = SignInWithAppleConfiguration(
                    clientId = BuildConfig.APPLE_AUTH_CLIENT_ID,
                    redirectUri = "${hostConfig.address}/api/v4/user/auth/apple",
                    scope = "name email"
            )
            val fragmentTag = "SignInWithAppleButton-SignInWebViewDialogFragment"

            SignInWithAppleService(supportFragmentManager, fragmentTag, configuration) { result ->
                when (result) {
                    is SignInWithAppleResult.Success -> {
                        val response = UserAuthResponse()
                        response.id = result.userID
                        response.apiToken = result.apiKey
                        response.newUser = result.newUser
                    }
                }
            }.show()
        }
    }

    override fun loadTheme(sharedPreferences: SharedPreferences, forced: Boolean) {
        super.loadTheme(sharedPreferences, forced)
        window.updateStatusBarColor(R.color.black_20_alpha, false)
    }

    private fun setupFacebookLogin() {
        callbackManager = CallbackManager.Factory.create()
        loginManager.registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        val accessToken = AccessToken.getCurrentAccessToken()
                        compositeSubscription.add(apiClient.connectSocial("facebook", accessToken.userId, accessToken.token)
                                .subscribe(this@LoginActivity, RxErrorHandler.handleEmptyError()))
                    }

                    override fun onCancel() { /* no-on */ }

                    override fun onError(exception: FacebookException) {
                        exception.printStackTrace()
                    }
                })

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
            binding.fbLoginButton.setText(R.string.register_btn_fb)
            binding.googleLoginButton.setText(R.string.register_btn_google)
        } else {
            binding.loginBtn.text = getString(R.string.login_btn)
            binding.username.setHint(R.string.email_username)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.username.setAutofillHints("username")
                binding.password.setAutofillHints("password")
            }
            binding.password.imeOptions = EditorInfo.IME_ACTION_DONE
            binding.fbLoginButton.setText(R.string.login_btn_fb)
            binding.googleLoginButton.setText(R.string.login_btn_google)
        }
        this.resetLayout()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == Activity.RESULT_OK) {
                googleEmail = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                handleGoogleLoginResult()
            }
        }
        if (requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR) {
            // RESULT_CANCELED occurs when user denies requested permissions. In this case we don't
            // want to immediately ask them to accept permissions again. See Issue #1290 on github.
            if (resultCode != Activity.RESULT_CANCELED) {
                handleGoogleLoginResult()
            }
        }

        if (requestCode == FacebookSdk.getCallbackRequestCodeOffset()) {
            //This is necessary because the regular login callback is not called for some reason
            val accessToken = AccessToken.getCurrentAccessToken()
            if (accessToken != null && accessToken.token != null) {
                compositeSubscription.add(apiClient.connectSocial("facebook", accessToken.userId, accessToken.token)
                        .subscribe(this@LoginActivity, { hideProgress() }))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_toggleRegistering -> toggleRegistering()
        }
        return super.onOptionsItemSelected(item)
    }

    @Throws(Exception::class)
    private fun saveTokens(api: String, user: String) {
        this.apiClient.updateAuthenticationCredentials(user, api)
        sharedPrefs.edit {
            putString(getString(R.string.SP_userID), user)
                val encryptedKey = if (keyHelper != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        keyHelper?.encrypt(api)
                    } catch (e: Exception) {
                        null
                    }
                } else null
                if (encryptedKey?.length ?: 0 > 5) {
                    putString(user, encryptedKey)
                } else {
                    //Something might have gone wrong with encryption, so fall back to this.
                    putString(getString(R.string.SP_APIToken), api)
                }

        }
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

    override fun accept(userAuthResponse: UserAuthResponse) {
        hideProgress()
        dismissKeyboard()
        try {
            saveTokens(userAuthResponse.apiToken, userAuthResponse.id)
        } catch (e: Exception) {
            analyticsManager.logException(e)
        }

        HabiticaBaseApplication.reloadUserComponent()

        if (isRegistering) {
            FirebaseAnalytics.getInstance(this).logEvent("user_registered", null)
        }

        compositeSubscription.add(userRepository.retrieveUser(true, true)
                .subscribe({
                    if (userAuthResponse.newUser) {
                        this.startSetupActivity()
                    } else {
                        this.startMainActivity()
                        AmplitudeManager.sendEvent("login", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT)
                    }
                }, RxErrorHandler.handleEmptyError()))
    }

    private fun handleFacebookLogin() {
        loginManager.logInWithReadPermissions(this, listOf("user_friends"))
    }

    private fun handleGoogleLogin() {
        if (!checkPlayServices()) {
            return
        }
        val accountTypes = arrayOf("com.google")
        val intent = AccountManager.newChooseAccountIntent(null, null,
                accountTypes, true, null, null, null, null)
        try {
            startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT)
        } catch (e: ActivityNotFoundException) {
            val alert = HabiticaAlertDialog(this)
            alert.setTitle(R.string.authentication_error_title)
            alert.setMessage(R.string.google_services_missing)
            alert.addCloseButton()
            alert.show()
        }

    }

    private fun handleGoogleLoginResult() {
        val scopesString = Scopes.PROFILE + " " + Scopes.EMAIL
        val scopes = "oauth2:$scopesString"
        compositeSubscription.add(Flowable.defer {
            try {
                @Suppress("Deprecation")
                return@defer Flowable.just(GoogleAuthUtil.getToken(this, googleEmail, scopes))
            } catch (e: IOException) {
                throw Exceptions.propagate(e)
            } catch (e: GoogleAuthException) {
                throw Exceptions.propagate(e)
            }
        }
                .subscribeOn(Schedulers.io())
                .flatMap { token -> apiClient.connectSocial("google", googleEmail ?: "", token) }
                .subscribe(this@LoginActivity, { throwable ->
                    throwable.printStackTrace()
                    hideProgress()
                    throwable.cause?.let {
                        if (GoogleAuthException::class.java.isAssignableFrom(it.javaClass)) {
                            handleGoogleAuthException(throwable.cause as GoogleAuthException)
                        }
                    }

                }))
    }

    private fun handleGoogleAuthException(e: Exception) {
        if (e is GooglePlayServicesAvailabilityException) {
            // The Google Play services APK is old, disabled, or not present.
            // Show a dialog created by Google Play services that allows
            // the user to update the APK
            val statusCode = e
                    .connectionStatusCode
            GoogleApiAvailability.getInstance()
            @Suppress("DEPRECATION")
            GooglePlayServicesUtil.showErrorDialogFragment(statusCode,
                    this@LoginActivity,
                    REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR) {

            }
        } else if (e is UserRecoverableAuthException) {
            // Unable to authenticate, such as when the user has not yet granted
            // the app access to the account, but the user can fix this.
            // Forward the user to an activity in Google Play services.
            val intent = e.intent
            startActivityForResult(intent, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
        }
    }

    private fun checkPlayServices(): Boolean {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, PLAY_SERVICES_RESOLUTION_REQUEST).show()
            }
            return false
        }

        return true
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
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
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
        private const val REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000


        fun show(v: View) {
            v.visibility = View.VISIBLE
        }

        fun hide(v: View) {
            v.visibility = View.GONE
        }
    }
}
