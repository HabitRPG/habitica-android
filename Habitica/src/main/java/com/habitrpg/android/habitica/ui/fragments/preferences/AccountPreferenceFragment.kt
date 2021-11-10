package com.habitrpg.android.habitica.ui.fragments.preferences

import android.accounts.AccountManager
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
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
import com.habitrpg.android.habitica.ui.viewmodels.AuthenticationViewModel
import com.habitrpg.android.habitica.ui.views.ExtraLabelPreference
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaProgressDialog
import javax.inject.Inject

class AccountPreferenceFragment: BasePreferencesFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject
    lateinit var hostConfig: HostConfig
    @Inject
    lateinit var apiClient: ApiClient

    private lateinit var viewModel: AuthenticationViewModel

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

        viewModel.setupFacebookLogin { viewModel.handleAuthResponse(it) }
    }

    override fun setupPreferences() {
        updateUserFields()
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }


    private fun updateUserFields() {
        val user = user ?: return
        configurePreference(findPreference("username"), user.authentication?.localAuthentication?.username)
        configurePreference(findPreference("email"), user.authentication?.localAuthentication?.email)
        findPreference<Preference>("confirm_username")?.isVisible = user.flags?.verifiedUsername != true

        val passwordPref = findPreference<ExtraLabelPreference>("password")
        if (user.authentication?.hasPassword == true) {
            passwordPref?.summary = "··········"
            passwordPref?.extraText = getString(R.string.change_password)
        } else {
            passwordPref?.summary = getString(R.string.not_set)
            passwordPref?.extraText = getString(R.string.add_password)
        }
        val googlePref = findPreference<ExtraLabelPreference>("google_auth")
        if (user.authentication?.hasGoogleAuth == true) {
            googlePref?.summary = user.authentication?.googleAuthentication?.emails?.first()
            googlePref?.extraText = getString(R.string.disconnect)
            googlePref?.extraTextColor = context?.let { ContextCompat.getColor(it, R.color.text_red) }
        } else {
            googlePref?.summary = getString(R.string.not_connected)
            googlePref?.extraText = getString(R.string.connect)
        }
        val applePref = findPreference<ExtraLabelPreference>("apple_auth")
        if (user.authentication?.hasGoogleAuth == true) {
            applePref?.summary = user.authentication?.appleAuthentication?.emails?.first()
            applePref?.extraText = getString(R.string.disconnect)
            applePref?.extraTextColor = context?.let { ContextCompat.getColor(it, R.color.text_red) }
        } else {
            applePref?.summary = getString(R.string.not_connected)
            applePref?.extraText = getString(R.string.connect)
        }
        val facebookPref = findPreference<ExtraLabelPreference>("facebook_auth")
        if (user.authentication?.hasFacebookAuth == true) {
            facebookPref?.summary = user.authentication?.facebookAuthentication?.emails?.first()
            facebookPref?.extraText = getString(R.string.disconnect)
            facebookPref?.extraTextColor = context?.let { ContextCompat.getColor(it, R.color.text_red) }
        } else {
            facebookPref?.summary = getString(R.string.not_connected)
            facebookPref?.extraText = getString(R.string.connect)
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String) {
        val profileCategory = findPreference("profile") as? PreferenceCategory
        configurePreference(profileCategory?.findPreference(key), sharedPreferences?.getString(key, ""))
        if (sharedPreferences != null) {
            val newValue = sharedPreferences.getString(key, "") ?: return
            when (key) {
                "display_name" -> updateUser("profile.name", newValue, user?.profile?.name)
                "photo_url" -> updateUser("profile.imageUrl", newValue, user?.profile?.imageUrl)
                "about" -> updateUser("profile.blurb", newValue, user?.profile?.blurb)
            }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when(preference?.key) {
            "username" -> showLoginNameDialog()
            "confirm_username" -> showConfirmUsernameDialog()
            "email" -> showEmailDialog()
            "password" -> {
                if (user?.authentication?.hasPassword == true) {
                    showChangePasswordDialog()
                } else {
                    showAddPasswordDialog()
                }
            }
            "UserID" -> {
                copyValue(getString(R.string.SP_userID), user?.id)
                return true
            }
            "ApiToken" -> {
                copyValue(getString(R.string.SP_APIToken_title), hostConfig.apiKey)
                return true
            }
            "google_auth" -> {
                if (user?.authentication?.hasGoogleAuth == true) {
                    apiClient.disconnectSocial("google").subscribe({}, RxErrorHandler.handleEmptyError())
                } else {
                    activity?.let {
                        viewModel.handleGoogleLogin(it, pickAccountResult)
                    }
                }
            }
            "apple_auth" -> {
                if (user?.authentication?.hasAppleAuth == true) {
                    apiClient.disconnectSocial("apple").subscribe({}, RxErrorHandler.handleEmptyError())
                } else {
                    viewModel.connectApple(parentFragmentManager) {
                        viewModel.handleAuthResponse(it)
                    }
                }
            }
            "facebook_auth" -> {
                if (user?.authentication?.hasFacebookAuth == true) {
                    apiClient.disconnectSocial("facebook").subscribe({}, RxErrorHandler.handleEmptyError())
                } else {
                    viewModel.handleFacebookLogin(this)
                }
            }
            "reset_account" -> showAccountResetConfirmation()
            "delete_account" -> showAccountDeleteConfirmation()
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data) {
            viewModel.handleAuthResponse(it)
        }
    }

    private val pickAccountResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            viewModel.googleEmail = it?.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            activity?.let { it1 ->
                viewModel.handleGoogleLoginResult(it1, recoverFromPlayServicesErrorResult) {
                    viewModel.handleAuthResponse(it)
                }
            }
        }
    }

    private val recoverFromPlayServicesErrorResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != Activity.RESULT_CANCELED) {
            activity?.let { it1 ->
                viewModel.handleGoogleLoginResult(it1, null) {
                    viewModel.handleAuthResponse(it)
                }
            }
        }
    }

    private fun updateUser(path: String, newValue: String, oldValue: String?) {
        if (newValue != oldValue) {
            compositeSubscription.add(userRepository.updateUser(path, newValue).subscribe({}, RxErrorHandler.handleEmptyError()))
        }
    }

    private fun showChangePasswordDialog() {
        val inflater = context?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edittext_change_pw, null)
        val oldPasswordEditText = view?.findViewById<EditText>(R.id.editText)
        val passwordEditText = view?.findViewById<EditText>(R.id.passwordEditText)
        val passwordRepeatEditText = view?.findViewById<EditText>(R.id.passwordRepeatEditText)
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.change_password)
            dialog.addButton(R.string.change, true) { _, _ ->
                userRepository.updatePassword(oldPasswordEditText?.text.toString(), passwordEditText?.text.toString(), passwordRepeatEditText?.text.toString())
                    .subscribe(
                        {
                            Toast.makeText(activity, R.string.password_changed, Toast.LENGTH_SHORT).show()
                        },
                        RxErrorHandler.handleEmptyError()
                    )
            }
            dialog.addCancelButton()
            dialog.setAdditionalContentView(view)
            dialog.setAdditionalContentSidePadding(12)
            dialog.show()
        }
    }

    private fun showAddPasswordDialog() {
        val inflater = context?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edittext_change_pw, null)
        val oldPasswordEditText = view?.findViewById<EditText>(R.id.editText)
        oldPasswordEditText?.visibility = View.GONE
        val passwordEditText = view?.findViewById<EditText>(R.id.passwordEditText)
        val passwordRepeatEditText = view?.findViewById<EditText>(R.id.passwordRepeatEditText)
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.add_password)
            dialog.addButton(R.string.add, true) { _, _ ->
                val email = user?.authentication?.findFirstSocialEmail()
                apiClient.registerUser(user?.username ?: "", email ?: "", passwordEditText?.text.toString(), passwordRepeatEditText?.text.toString())
                    .subscribe(
                        {
                            Toast.makeText(activity, R.string.password_changed, Toast.LENGTH_SHORT).show()
                        },
                        RxErrorHandler.handleEmptyError()
                    )
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
        val emailEditText = view?.findViewById<EditText>(R.id.editText)
        emailEditText?.setText(user?.authentication?.localAuthentication?.email)
        view?.findViewById<TextInputLayout>(R.id.input_layout)?.hint = context?.getString(R.string.email)
        val passwordEditText = view?.findViewById<EditText>(R.id.passwordEditText)
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.change_email)
            dialog.addButton(R.string.change, true) { _, _ ->
                userRepository.updateEmail(emailEditText?.text.toString(), passwordEditText?.text.toString())
                    .subscribe(
                        {
                            configurePreference(findPreference("email"), emailEditText?.text.toString())
                        },
                        RxErrorHandler.handleEmptyError()
                    )
            }
            dialog.addCancelButton()
            dialog.setAdditionalContentView(view)
            dialog.setAdditionalContentSidePadding(12.dpToPx(context))
            dialog.show()
        }
    }

    private fun showLoginNameDialog() {
        val inflater = context?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edittext, null)
        val loginNameEditText = view?.findViewById<EditText>(R.id.editText)
        loginNameEditText?.setText(user?.authentication?.localAuthentication?.username)
        view?.findViewById<TextInputLayout>(R.id.input_layout)?.hint = context?.getString(R.string.username)
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.change_username)
            dialog.addButton(R.string.save, true) { _, _ ->
                userRepository.updateLoginName(loginNameEditText?.text.toString())
                    .subscribe(
                        {
                            configurePreference(findPreference("username"), loginNameEditText?.text.toString())
                        },
                        RxErrorHandler.handleEmptyError()
                    )
            }
            dialog.addCancelButton()
            dialog.setAdditionalContentView(view)
            dialog.setAdditionalContentSidePadding(12.dpToPx(context))
            dialog.show()
        }
    }

    private fun showAccountDeleteConfirmation() {
        val view = context?.layoutInflater?.inflate(R.layout.dialog_edittext, null)
        var deleteMessage = getString(R.string.delete_account_description)
        val editText = view?.findViewById<EditText>(R.id.editText)
        if (user?.authentication?.localAuthentication?.email != null) {
            editText?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            deleteMessage = getString(R.string.delete_oauth_account_description)
            editText?.inputType = InputType.TYPE_CLASS_TEXT
        }
        view?.findViewById<TextInputLayout>(R.id.input_layout)?.hint = context?.getString(R.string.confirm_deletion)
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.delete_account)
            dialog.setMessage(deleteMessage)
            dialog.addCancelButton()
            dialog.addButton(R.string.delete, isPrimary = true, isDestructive = true) { _, _ ->
                deleteAccount(editText?.text?.toString() ?: "")
            }
            dialog.setAdditionalContentView(view)
            dialog.setAdditionalContentSidePadding(12.dpToPx(context))
            dialog.buttonAxis = LinearLayout.HORIZONTAL
            dialog.show()
        }
    }

    private fun deleteAccount(password: String) {
        val dialog = HabiticaProgressDialog.show(context, R.string.deleting_account)
        compositeSubscription.add(
            userRepository.deleteAccount(password).subscribe({ _ ->
                dialog?.dismiss()
                context?.let { HabiticaBaseApplication.logout(it) }
                activity?.finish()
            }) { throwable ->
                dialog?.dismiss()
                RxErrorHandler.reportError(throwable)
            }
        )
    }

    private fun showAccountResetConfirmation() {
        val context = context ?: return

        val dialog = HabiticaAlertDialog(context)
        dialog.setTitle(R.string.reset_account)
        dialog.setMessage(R.string.reset_account_description)
        dialog.addButton(R.string.reset_account_confirmation, true, true) { _, _ ->
            resetAccount()
        }
        dialog.addCancelButton()
        dialog.setAdditionalContentSidePadding(12.dpToPx(context))
        dialog.show()
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
            userRepository.resetAccount().subscribe({ dialog?.dismiss() }) { throwable ->
                dialog?.dismiss()
                RxErrorHandler.reportError(throwable)
            }
        )
    }

    private fun copyValue(name: String, value: CharSequence?) {
        ClipData.newPlainText(name, value)
        Toast.makeText(activity, "Copied $name to clipboard.", Toast.LENGTH_SHORT).show()
    }
}