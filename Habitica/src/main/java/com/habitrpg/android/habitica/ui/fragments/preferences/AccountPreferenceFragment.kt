package com.habitrpg.android.habitica.ui.fragments.preferences

import android.accounts.AccountManager
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.util.PatternsCompat
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import com.google.android.material.textfield.TextInputLayout
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.FixCharacterValuesActivity
import com.habitrpg.android.habitica.ui.fragments.preferences.HabiticaAccountDialog.AccountUpdateConfirmed
import com.habitrpg.android.habitica.ui.helpers.KeyboardUtil
import com.habitrpg.android.habitica.ui.viewmodels.AuthenticationViewModel
import com.habitrpg.android.habitica.ui.views.ExtraLabelPreference
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.SnackbarActivity
import com.habitrpg.android.habitica.ui.views.ValidatingEditText
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaProgressDialog
import javax.inject.Inject

class AccountPreferenceFragment :
    BasePreferencesFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener,
        AccountUpdateConfirmed {
    @Inject
    lateinit var hostConfig: HostConfig
    @Inject
    lateinit var apiClient: ApiClient

    private lateinit var viewModel: AuthenticationViewModel
    private lateinit var accountDialog: HabiticaAccountDialog

    override var user: User? = null
        set(value) {
            field = value
            updateUserFields()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.userComponent?.inject(this)
        super.onCreate(savedInstanceState)
        viewModel = AuthenticationViewModel()
        findPreference<Preference>("confirm_username")?.isVisible = user?.flags?.verifiedUsername == false
    }

    override fun setupPreferences() {
        updateUserFields()
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    private fun updateUserFields() {
        val user = user ?: return
        configurePreference(findPreference("username"), user.authentication?.localAuthentication?.username)
        configurePreference(findPreference("email"), user.authentication?.localAuthentication?.email ?: getString(R.string.not_set))
        findPreference<Preference>("confirm_username")?.isVisible = user.flags?.verifiedUsername != true

        val passwordPref = findPreference<ExtraLabelPreference>("password")
        if (user.authentication?.hasPassword == true) {
            passwordPref?.summary = "···········"
            passwordPref?.extraText = getString(R.string.change_password)
        } else {
            passwordPref?.summary = getString(R.string.not_set)
            passwordPref?.extraText = getString(R.string.add_password)
        }
        val googlePref = findPreference<ExtraLabelPreference>("google_auth")
        if (user.authentication?.hasGoogleAuth == true) {
            googlePref?.summary = user.authentication?.googleAuthentication?.emails?.firstOrNull()
            googlePref?.extraText = getString(R.string.disconnect)
            googlePref?.extraTextColor = context?.let { ContextCompat.getColor(it, R.color.text_red) }
        } else {
            googlePref?.summary = getString(R.string.not_connected)
            googlePref?.extraText = getString(R.string.connect)
            googlePref?.extraTextColor = context?.let { ContextCompat.getColor(it, R.color.text_ternary) }
        }
        val applePref = findPreference<ExtraLabelPreference>("apple_auth")
        if (user.authentication?.hasAppleAuth == true) {
            applePref?.summary = user.authentication?.appleAuthentication?.emails?.firstOrNull()
            applePref?.extraText = getString(R.string.disconnect)
            applePref?.extraTextColor = context?.let { ContextCompat.getColor(it, R.color.text_red) }
        } else {
            applePref?.isVisible = false
        }
        val facebookPref = findPreference<ExtraLabelPreference>("facebook_auth")
        if (user.authentication?.hasFacebookAuth == true) {
            facebookPref?.summary = user.authentication?.facebookAuthentication?.emails?.firstOrNull()
            facebookPref?.extraText = getString(R.string.disconnect)
            facebookPref?.extraTextColor = context?.let { ContextCompat.getColor(it, R.color.text_red) }
        } else {
            facebookPref?.isVisible = false
        }

        configurePreference(findPreference("display_name"), user.profile?.name)
        configurePreference(findPreference("photo_url"), user.profile?.imageUrl)
        configurePreference(findPreference("about"), user.profile?.blurb)

        configurePreference(findPreference("UserID"), user.id)
    }

    private fun configurePreference(preference: Preference?, value: String?) {
        (preference as? EditTextPreference)?.let {
            it.text = value
        }
        preference?.summary = value
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "username" -> showLoginNameDialog()
            "confirm_username" -> showConfirmUsernameDialog()
            "email" -> {
                if (user?.authentication?.hasPassword == true) {
                    showEmailDialog()
                } else {
                    showAddPasswordDialog(true)
                }
            }
            "password" -> {
                if (user?.authentication?.hasPassword == true) {
                    showChangePasswordDialog()
                } else {
                    showAddPasswordDialog(true)
                }
            }
            "UserID" -> {
                copyValue(getString(R.string.SP_userID), user?.id)
                return true
            }
            "APIToken" -> {
                copyValue(getString(R.string.SP_APIToken_title), hostConfig.apiKey)
                return true
            }
            "display_name" -> updateUser("profile.name", user?.profile?.name, getString(R.string.display_name))
            "photo_url" -> updateUser("profile.imageUrl", user?.profile?.imageUrl, getString(R.string.photo_url))
            "about" -> updateUser("profile.blurb", user?.profile?.blurb, getString(R.string.about))
            "google_auth" -> {
                if (user?.authentication?.hasGoogleAuth == true) {
                    disconnect("google", "Google")
                } else {
                    activity?.let {
                        viewModel.handleGoogleLogin(it, pickAccountResult)
                    }
                }
            }
            "apple_auth" -> {
                if (user?.authentication?.hasAppleAuth == true) {
                    disconnect("apple", "Apple")
                } else {
                    viewModel.connectApple(parentFragmentManager) {
                        viewModel.handleAuthResponse(it)
                    }
                }
            }
            "facebook_auth" -> {
                if (user?.authentication?.hasFacebookAuth == true) {
                    disconnect("facebook", "Facebook")
                }
            }
            "reset_account" -> showAccountResetConfirmation(user)
            "delete_account" -> showAccountDeleteConfirmation(user)
            "fixCharacterValues" -> {
                val intent = Intent(activity, FixCharacterValuesActivity::class.java)
                activity?.startActivity(intent)
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun disconnect(network: String, networkName: String) {
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.are_you_sure)
            dialog.addButton(R.string.disconnect, true) { _, _ ->
                apiClient.disconnectSocial(network)
                    .flatMap { userRepository.retrieveUser(true, true) }
                    .subscribe({ displayDisconnectSuccess(networkName) }, RxErrorHandler.handleEmptyError())
            }
            dialog.addCancelButton()
            dialog.show()
        }
    }

    private val pickAccountResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            viewModel.googleEmail = it?.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            activity?.let { it1 ->
                viewModel.handleGoogleLoginResult(it1, recoverFromPlayServicesErrorResult) { _, _ ->
                    displayAuthenticationSuccess(getString(R.string.google))
                }
            }
        }
    }

    private fun displayAuthenticationSuccess(network: String) {
        (activity as? SnackbarActivity)?.showSnackbar(
            content = context?.getString(R.string.added_social_auth, network),
            displayType = HabiticaSnackbar.SnackbarDisplayType.SUCCESS
        )
    }

    private fun displayDisconnectSuccess(network: String) {
        (activity as? SnackbarActivity)?.showSnackbar(
            content = context?.getString(R.string.removed_social_auth, network),
            displayType = HabiticaSnackbar.SnackbarDisplayType.SUCCESS
        )
    }

    private val recoverFromPlayServicesErrorResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != Activity.RESULT_CANCELED) {
            activity?.let { it1 ->
                viewModel.handleGoogleLoginResult(it1, null) { _, _ ->
                    displayAuthenticationSuccess(getString(R.string.google))
                }
            }
        }
    }

    private fun updateUser(path: String, value: String?, title: String) {
        showSingleEntryDialog(value, title) {
            if (value != it) {
                userRepository.updateUser(path, it ?: "")
                    .subscribe({}, RxErrorHandler.handleEmptyError())
            }
        }
    }

    private fun showChangePasswordDialog() {
        val inflater = context?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edittext_change_pw, null)
        val oldPasswordEditText = view?.findViewById<ValidatingEditText>(R.id.old_password_edit_text)
        val passwordEditText = view?.findViewById<ValidatingEditText>(R.id.new_password_edit_text)
        passwordEditText?.validator = { (it?.length ?: 0) >= 8 }
        passwordEditText?.errorText = getString(R.string.password_too_short, 8)
        val passwordRepeatEditText = view?.findViewById<ValidatingEditText>(R.id.new_password_repeat_edit_text)
        passwordRepeatEditText?.validator = { it == passwordEditText?.text }
        passwordRepeatEditText?.errorText = getString(R.string.password_not_matching)
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.change_password)
            dialog.addButton(R.string.change, true, false, false) { dialog, _ ->
                KeyboardUtil.dismissKeyboard(activity)
                passwordEditText?.showErrorIfNecessary()
                passwordRepeatEditText?.showErrorIfNecessary()
                if (passwordEditText?.isValid != true || passwordRepeatEditText?.isValid != true) return@addButton
                userRepository.updatePassword(
                    oldPasswordEditText?.text ?: "",
                    passwordEditText.text ?: "",
                    passwordRepeatEditText.text ?: ""
                )
                    .flatMap { userRepository.retrieveUser(true, true) }
                    .subscribe(
                        {
                            (activity as? SnackbarActivity)?.showSnackbar(
                                content = context.getString(R.string.password_changed),
                                displayType = HabiticaSnackbar.SnackbarDisplayType.SUCCESS
                            )
                        },
                        RxErrorHandler.handleEmptyError()
                    )
                dialog.dismiss()
            }
            dialog.addCancelButton()
            dialog.setAdditionalContentView(view)
            dialog.setAdditionalContentSidePadding(12)
            dialog.show()
        }
    }

    private fun showAddPasswordDialog(showEmail: Boolean) {
        val inflater = context?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edittext_add_local_auth, null)
        val emailEditText = view?.findViewById<ValidatingEditText>(R.id.email_edit_text)
        emailEditText?.visibility = if (showEmail) View.VISIBLE else View.GONE
        emailEditText?.validator = { PatternsCompat.EMAIL_ADDRESS.matcher(it ?: "").matches() }
        emailEditText?.errorText = getString(R.string.email_invalid)
        val passwordEditText = view?.findViewById<ValidatingEditText>(R.id.password_edit_text)
        passwordEditText?.validator = { (it?.length ?: 0) >= 8 }
        passwordEditText?.errorText = getString(R.string.password_too_short, 8)
        val passwordRepeatEditText = view?.findViewById<ValidatingEditText>(R.id.password_repeat_edit_text)
        passwordRepeatEditText?.validator = { it == passwordEditText?.text }
        passwordRepeatEditText?.errorText = getString(R.string.password_not_matching)
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            if (showEmail) {
                dialog.setTitle(R.string.add_email_and_password)
            } else {
                dialog.setTitle(R.string.add_password)
            }
            dialog.addButton(R.string.add, true, false, false) { dialog, _ ->
                KeyboardUtil.dismissKeyboard(activity)
                emailEditText?.showErrorIfNecessary()
                passwordEditText?.showErrorIfNecessary()
                passwordRepeatEditText?.showErrorIfNecessary()
                if (emailEditText?.isValid != true || passwordEditText?.isValid != true || passwordRepeatEditText?.isValid != true) return@addButton
                val email = if (showEmail) emailEditText.text else user?.authentication?.findFirstSocialEmail()
                apiClient.registerUser(user?.username ?: "", email ?: "", passwordEditText.text ?: "", passwordRepeatEditText?.text ?: "")
                    .flatMap { userRepository.retrieveUser(true, true) }
                    .subscribe(
                        {
                            (activity as? SnackbarActivity)?.showSnackbar(
                                content = context.getString(R.string.password_added),
                                displayType = HabiticaSnackbar.SnackbarDisplayType.SUCCESS
                            )
                        },
                        RxErrorHandler.handleEmptyError()
                    )
                dialog.dismiss()
            }
            dialog.addCancelButton()
            dialog.setAdditionalContentView(view)
            dialog.setAdditionalContentSidePadding(12)
            dialog.show()
        }
    }

    private fun showEmailDialog() {
        val inflater = context?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edittext_confirm_pw, null)
        val emailEditText = view?.findViewById<ValidatingEditText>(R.id.email_edit_text)
        emailEditText?.text = user?.authentication?.localAuthentication?.email
        emailEditText?.validator = { PatternsCompat.EMAIL_ADDRESS.matcher(it ?: "").matches() }
        emailEditText?.errorText = getString(R.string.email_invalid)
        view?.findViewById<TextInputLayout>(R.id.input_layout)?.hint = context?.getString(R.string.email)
        val passwordEditText = view?.findViewById<ValidatingEditText>(R.id.password_edit_text)
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.change_email)
            dialog.addButton(R.string.change, true, false, false) { dialog, _ ->
                KeyboardUtil.dismissKeyboard(activity)
                emailEditText?.showErrorIfNecessary()
                if (emailEditText?.isValid != true) return@addButton
                userRepository.updateEmail(emailEditText.text.toString(), passwordEditText?.text.toString())
                    .flatMap { userRepository.retrieveUser(true, true) }
                    .subscribe(
                        {
                            configurePreference(findPreference("email"), emailEditText.text.toString())
                        },
                        RxErrorHandler.handleEmptyError()
                    )
                dialog.dismiss()
            }
            dialog.addCancelButton()
            dialog.setAdditionalContentView(view)
            dialog.setAdditionalContentSidePadding(12.dpToPx(context))
            dialog.show()
        }
    }

    private fun showLoginNameDialog() {
        showSingleEntryDialog(user?.username, getString(R.string.username)) {
            userRepository.updateLoginName(it ?: "")
                .subscribe({}, RxErrorHandler.handleEmptyError())
        }
    }

    private fun showSingleEntryDialog(value: String?, title: String, onChange: (String?) -> Unit) {
        val inflater = context?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edittext, null)
        val editText = view?.findViewById<EditText>(R.id.editText)
        editText?.setText(value)
        editText?.maxLines = 15
        view?.findViewById<TextInputLayout>(R.id.input_layout)?.hint = title
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(title)
            dialog.addButton(R.string.save, true) { _, _ ->
                onChange(editText?.text?.toString())
            }
            dialog.addCancelButton()
            dialog.setAdditionalContentView(view)
            dialog.setAdditionalContentSidePadding(12.dpToPx(context))
            dialog.scrollView.isScrollable = false
            dialog.show()
        }
    }

    private fun showAccountDeleteConfirmation(user: User?) {
        val habiticaAccountDialog = context?.let { HabiticaAccountDialog(it) }
        habiticaAccountDialog?.accountAction = "delete_account"
        habiticaAccountDialog?.accountUpdateConfirmed = this
        habiticaAccountDialog?.user = user
        habiticaAccountDialog?.show(parentFragmentManager, "account")

        if (habiticaAccountDialog != null) {
            accountDialog = habiticaAccountDialog
        }
    }

    private fun deleteAccount(password: String) {
        val dialog = HabiticaProgressDialog.show(context, R.string.deleting_account)
        compositeSubscription.add(
            userRepository.deleteAccount(password).subscribe({ _ ->
                dialog?.dismiss()
                accountDialog.dismiss()
                context?.let { HabiticaBaseApplication.logout(it) }
                activity?.finish()
            }) { throwable ->
                dialog?.dismiss()
                RxErrorHandler.reportError(throwable)
            }
        )
    }

    private fun showAccountResetConfirmation(user: User?) {
        val habiticaAccountDialog = context?.let { HabiticaAccountDialog(it) }
        habiticaAccountDialog?.accountAction = "reset_account"
        habiticaAccountDialog?.accountUpdateConfirmed = this
        habiticaAccountDialog?.user = user
        habiticaAccountDialog?.show(parentFragmentManager, "account")

        if (habiticaAccountDialog != null) {
            accountDialog = habiticaAccountDialog
        }

    }

    private fun showConfirmUsernameDialog() {
        val context = context ?: return
        val dialog = HabiticaAlertDialog(context)
        dialog.setTitle(R.string.confirm_username_title)
        dialog.setMessage(R.string.confirm_username_description)
        dialog.addButton(R.string.confirm, true) { _, _ ->
            userRepository.updateLoginName(user?.authentication?.localAuthentication?.username ?: "")
                .subscribe({ }, RxErrorHandler.handleEmptyError())
        }
        dialog.addCancelButton()
        dialog.show()
    }

    private fun resetAccount() {
        val dialog = HabiticaProgressDialog.show(context, R.string.resetting_account)
        compositeSubscription.add(
            userRepository.resetAccount().subscribe({
                dialog?.dismiss()
                accountDialog.dismiss()
            }) { throwable ->
                dialog?.dismiss()
                RxErrorHandler.reportError(throwable)
            }
        )
    }

    private fun copyValue(name: String, value: CharSequence?) {
        val clipboard: ClipboardManager? = context?.let { getSystemService(it, ClipboardManager::class.java) }
        clipboard?.setPrimaryClip(ClipData.newPlainText(name, value))
        (activity as? SnackbarActivity)?.showSnackbar(
            content = context?.getString(R.string.copied_to_clipboard),
            displayType = HabiticaSnackbar.SnackbarDisplayType.SUCCESS
        )
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
    }

    override fun resetConfirmedClicked() {
        resetAccount()
    }

    override fun deletionConfirmClicked(confirmationString: String) {
        deleteAccount(confirmationString)
    }

}
