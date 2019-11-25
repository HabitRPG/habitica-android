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
import android.text.style.UnderlineSpan
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
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
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.KeyHelper
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.auth.UserAuthResponse
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.login.LockableScrollView
import com.habitrpg.android.habitica.ui.views.login.LoginBackgroundView
import io.reactivex.Flowable
import io.reactivex.exceptions.Exceptions
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import javax.inject.Inject

class LoginActivity : BaseActivity(), Consumer<UserAuthResponse> {

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
    lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    lateinit var configManager: AppConfigManager

    private var isRegistering: Boolean = false
    private var isShowingForm: Boolean = false

    private val backgroundContainer: LockableScrollView by bindView(R.id.background_container)
    internal val backgroundView: LoginBackgroundView by bindView(R.id.background_view)
    internal val newGameButton: Button by bindView(R.id.new_game_button)
    internal val showLoginButton: Button by bindView(R.id.show_login_button)
    internal val scrollView: ScrollView by bindView(R.id.login_scrollview)
    private val formWrapper: LinearLayout by bindView(R.id.login_linear_layout)
    private val backButton: Button by bindView(R.id.back_button)
    private val logoView: ImageView by bindView(R.id.logo_view)
    private val mLoginNormalBtn: Button by bindView(R.id.login_btn)
    private val mProgressBar: ProgressBar by bindView(R.id.PB_AsyncTask)
    private val mUsernameET: EditText by bindView(R.id.username)
    private val mPasswordET: EditText by bindView(R.id.password)
    private val mEmail: EditText by bindView(R.id.email)
    private val mConfirmPassword: EditText by bindView(R.id.confirm_password)
    private val forgotPasswordButton: Button by bindView(R.id.forgot_password)
    private val facebookLoginButton: Button by bindView(R.id.fb_login_button)
    private val googleLoginButton: Button by bindView(R.id.google_login_button)

    private var callbackManager = CallbackManager.Factory.create()
    private var googleEmail: String? = null
    private var loginManager = LoginManager.getInstance()

    private val loginClick = View.OnClickListener {
        mProgressBar.visibility = View.VISIBLE
        if (isRegistering) {
            val username: String = mUsernameET.text.toString().trim { it <= ' ' }
            val email: String = mEmail.text.toString().trim { it <= ' ' }
            val password: String = mPasswordET.text.toString()
            val confirmPassword: String = mConfirmPassword.text.toString()
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
                            Consumer {
                                hideProgress()
                                RxErrorHandler.reportError(it)
                            })
        } else {
            val username: String = mUsernameET.text.toString().trim { it <= ' ' }
            val password: String = mPasswordET.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                showValidationError(R.string.login_validation_error_fieldsmissing)
                return@OnClickListener
            }
            apiClient.connectUser(username, password).subscribe(this@LoginActivity,
                    Consumer {
                        hideProgress()
                        RxErrorHandler.reportError(it)
                    })
        }
    }

    override fun getLayoutResId(): Int {
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        return R.layout.activity_login
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            supportActionBar?.hide()
        //Set default values to avoid null-responses when requesting unedited settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences_fragment, false)

        setupFacebookLogin()

        mLoginNormalBtn.setOnClickListener(loginClick)

        val content = SpannableString(forgotPasswordButton.text)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        forgotPasswordButton.text = content

        this.isRegistering = true

        val additionalData = HashMap<String, Any>()
        additionalData["page"] = this.javaClass.simpleName
        AmplitudeManager.sendEvent("navigate", AmplitudeManager.EVENT_CATEGORY_NAVIGATION, AmplitudeManager.EVENT_HITTYPE_PAGEVIEW, additionalData)

        backgroundContainer.post { backgroundContainer.scrollTo(0, backgroundContainer.bottom) }
        backgroundContainer.setScrollingEnabled(false)

        val window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black_20_alpha)
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        newGameButton.setOnClickListener { newGameButtonClicked() }
        showLoginButton.setOnClickListener { showLoginButtonClicked() }
        backButton.setOnClickListener { backButtonClicked() }
        forgotPasswordButton.setOnClickListener { onForgotPasswordClicked() }
        facebookLoginButton.setOnClickListener { handleFacebookLogin() }
        googleLoginButton.setOnClickListener { handleGoogleLogin() }
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
            if (this.mEmail.visibility == View.GONE) {
                show(this.mEmail)
            }
            if (this.mConfirmPassword.visibility == View.GONE) {
                show(this.mConfirmPassword)
            }
        } else {
            if (this.mEmail.visibility == View.VISIBLE) {
                hide(this.mEmail)
            }
            if (this.mConfirmPassword.visibility == View.VISIBLE) {
                hide(this.mConfirmPassword)
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
            this.mLoginNormalBtn.text = getString(R.string.register_btn)
            mUsernameET.setHint(R.string.username)
            mPasswordET.imeOptions = EditorInfo.IME_ACTION_NEXT
            facebookLoginButton.setText(R.string.register_btn_fb)
            googleLoginButton.setText(R.string.register_btn_google)
        } else {
            this.mLoginNormalBtn.text = getString(R.string.login_btn)
            mUsernameET.setHint(R.string.email_username)
            mPasswordET.imeOptions = EditorInfo.IME_ACTION_DONE
            facebookLoginButton.setText(R.string.login_btn_fb)
            googleLoginButton.setText(R.string.login_btn_google)
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
            handleGoogleLoginResult()
        }

        if (requestCode == FacebookSdk.getCallbackRequestCodeOffset()) {
            //This is necessary because the regular login callback is not called for some reason
            val accessToken = AccessToken.getCurrentAccessToken()
            if (accessToken != null && accessToken.token != null) {
                compositeSubscription.add(apiClient.connectSocial("facebook", accessToken.userId, accessToken.token)
                        .subscribe(this@LoginActivity, Consumer { hideProgress() }))
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
            mProgressBar.visibility = View.GONE
        }
    }

    private fun showValidationError(resourceMessageString: Int) {
        showValidationError(getString(resourceMessageString))
    }

    private fun showValidationError(message: String) {
        mProgressBar.visibility = View.GONE
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(R.string.login_validation_error_title)
        alert.setMessage(message)
        alert.addOkButton()
        alert.show()
    }

    override fun accept(userAuthResponse: UserAuthResponse) {
        hideProgress()
        try {
            saveTokens(userAuthResponse.token, userAuthResponse.id)
        } catch (e: Exception) {
            crashlyticsProxy.logException(e)
        }

        HabiticaBaseApplication.reloadUserComponent()

        compositeSubscription.add(userRepository.retrieveUser(true)
                .subscribe(Consumer {
                    if (userAuthResponse.newUser) {
                        this.startSetupActivity()
                    } else {
                        AmplitudeManager.sendEvent("login", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT)
                        this.startMainActivity()
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
        Flowable.defer<String> {
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
                .subscribe(this@LoginActivity, Consumer { throwable ->
                    throwable.printStackTrace()
                    hideProgress()
                    throwable.cause?.let {
                        if (GoogleAuthException::class.java.isAssignableFrom(it.javaClass)) {
                            handleGoogleAuthException(throwable.cause as GoogleAuthException)
                        }
                    }

                })
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
        val panAnimation = ObjectAnimator.ofInt(backgroundContainer, "scrollY", 0).setDuration(1000)
        val newGameAlphaAnimation = ObjectAnimator.ofFloat<View>(newGameButton, View.ALPHA, 0.toFloat())
        val showLoginAlphaAnimation = ObjectAnimator.ofFloat<View>(showLoginButton, View.ALPHA, 0.toFloat())
        val scaleLogoAnimation = ValueAnimator.ofInt(logoView.measuredHeight, (logoView.measuredHeight * 0.75).toInt())
        scaleLogoAnimation.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as? Int ?: 0
            val layoutParams = logoView.layoutParams
            layoutParams.height = value
            logoView.layoutParams = layoutParams
        }
        if (isRegistering) {
            newGameAlphaAnimation.startDelay = 600
            newGameAlphaAnimation.duration = 400
            showLoginAlphaAnimation.duration = 400
            newGameAlphaAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    newGameButton.visibility = View.GONE
                    showLoginButton.visibility = View.GONE
                    scrollView.visibility = View.VISIBLE
                    scrollView.alpha = 1f
                }
            })
        } else {
            showLoginAlphaAnimation.startDelay = 600
            showLoginAlphaAnimation.duration = 400
            newGameAlphaAnimation.duration = 400
            showLoginAlphaAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    newGameButton.visibility = View.GONE
                    showLoginButton.visibility = View.GONE
                    scrollView.visibility = View.VISIBLE
                    scrollView.alpha = 1f
                }
            })
        }
        val backAlphaAnimation = ObjectAnimator.ofFloat<View>(backButton, View.ALPHA, 1.toFloat()).setDuration(800)
        val showAnimation = AnimatorSet()
        showAnimation.playTogether(panAnimation, newGameAlphaAnimation, showLoginAlphaAnimation, scaleLogoAnimation)
        showAnimation.play(backAlphaAnimation).after(panAnimation)
        for (i in 0 until formWrapper.childCount) {
            val view = formWrapper.getChildAt(i)
            view.alpha = 0f
            val animator = ObjectAnimator.ofFloat<View>(view, View.ALPHA, 1.toFloat()).setDuration(400)
            animator.startDelay = (100 * i).toLong()
            showAnimation.play(animator).after(panAnimation)
        }

        showAnimation.start()
    }

    private fun hideForm() {
        isShowingForm = false
        val panAnimation = ObjectAnimator.ofInt(backgroundContainer, "scrollY", backgroundContainer.bottom).setDuration(1000)
        val newGameAlphaAnimation = ObjectAnimator.ofFloat<View>(newGameButton, View.ALPHA, 1.toFloat()).setDuration(700)
        val showLoginAlphaAnimation = ObjectAnimator.ofFloat<View>(showLoginButton, View.ALPHA, 1.toFloat()).setDuration(700)
        val scaleLogoAnimation = ValueAnimator.ofInt(logoView.measuredHeight, (logoView.measuredHeight * 1.333333).toInt())
        scaleLogoAnimation.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as? Int
            val layoutParams = logoView.layoutParams
            layoutParams.height = value ?: 0
            logoView.layoutParams = layoutParams
        }
        showLoginAlphaAnimation.startDelay = 300
        val scrollViewAlphaAnimation = ObjectAnimator.ofFloat<View>(scrollView, View.ALPHA, 0.toFloat()).setDuration(800)
        scrollViewAlphaAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                newGameButton.visibility = View.VISIBLE
                showLoginButton.visibility = View.VISIBLE
                scrollView.visibility = View.INVISIBLE
            }
        })
        val backAlphaAnimation = ObjectAnimator.ofFloat<View>(backButton, View.ALPHA, 0.toFloat()).setDuration(800)
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
                    userRepository.sendPasswordResetEmail(input.text.toString()).subscribe(Consumer { showPasswordEmailConfirmation() }, RxErrorHandler.handleEmptyError())
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
