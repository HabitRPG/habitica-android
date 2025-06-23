package com.habitrpg.android.habitica.ui.fragments.preferences

import android.accounts.AccountManager
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.util.PatternsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.FixCharacterValuesActivity
import com.habitrpg.android.habitica.ui.fragments.preferences.HabiticaAccountDialog.AccountUpdateConfirmed
import com.habitrpg.android.habitica.ui.helpers.KeyboardUtil
import com.habitrpg.android.habitica.ui.viewmodels.AuthenticationViewModel
import com.habitrpg.android.habitica.ui.views.ApiTokenBottomSheet
import com.habitrpg.android.habitica.ui.views.ExtraLabelPreference
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.SnackbarActivity
import com.habitrpg.android.habitica.ui.views.ValidatingEditText
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaProgressDialog
import com.habitrpg.android.habitica.ui.views.preferences.PrivacyPreferencesView
import com.habitrpg.android.habitica.ui.views.showAsBottomSheet
import com.habitrpg.common.habitica.api.HostConfig
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
class AccountPreferenceFragment :
    BasePreferencesFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    AccountUpdateConfirmed {
    val viewModel: AuthenticationViewModel by viewModels()

    @Inject
    lateinit var hostConfig: HostConfig

    private lateinit var accountDialog: HabiticaAccountDialog

    override var user: User? = null
        set(value) {
            field = value
            updateUserFields()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findPreference<Preference>("confirm_username")?.isVisible =
            user?.flags?.verifiedUsername == false
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
        configurePreference(
            findPreference("username"),
            user.authentication?.localAuthentication?.username,
        )
        configurePreference(
            findPreference("email"),
            user.authentication?.localAuthentication?.email ?: getString(R.string.not_set),
        )
        findPreference<Preference>("confirm_username")?.isVisible =
            user.flags?.verifiedUsername != true

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
            googlePref?.extraTextColor =
                context?.let { ContextCompat.getColor(it, R.color.text_red) }
        } else {
            googlePref?.summary = getString(R.string.not_connected)
            googlePref?.extraText = getString(R.string.connect)
            googlePref?.extraTextColor =
                context?.let { ContextCompat.getColor(it, R.color.text_ternary) }
        }
        val applePref = findPreference<ExtraLabelPreference>("apple_auth")
        if (user.authentication?.hasAppleAuth == true) {
            applePref?.summary = user.authentication?.appleAuthentication?.emails?.firstOrNull()
            applePref?.extraText = getString(R.string.disconnect)
            applePref?.extraTextColor =
                context?.let { ContextCompat.getColor(it, R.color.text_red) }
        } else {
            applePref?.isVisible = false
        }
        val facebookPref = findPreference<ExtraLabelPreference>("facebook_auth")
        if (user.authentication?.hasFacebookAuth == true) {
            facebookPref?.summary =
                user.authentication?.facebookAuthentication?.emails?.firstOrNull()
            facebookPref?.extraText = getString(R.string.disconnect)
            facebookPref?.extraTextColor =
                context?.let { ContextCompat.getColor(it, R.color.text_red) }
        } else {
            facebookPref?.isVisible = false
        }

        configurePreference(findPreference("display_name"), user.profile?.name)
        configurePreference(findPreference("photo_url"), user.profile?.imageUrl)
        configurePreference(findPreference("about"), user.profile?.blurb)

        configurePreference(findPreference("UserID"), user.id)
    }

    private fun configurePreference(
        preference: Preference?,
        value: String?,
    ) {
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
                if (user?.authentication?.hasPassword != true && user?.authentication?.localAuthentication?.email?.isNotBlank() != true) {
                    showAddPasswordDialog(true)
                } else {
                    showEmailDialog()
                }
            }

            "password" -> {
                if (user?.authentication?.hasPassword == true) {
                    showChangePasswordDialog()
                } else {
                    showAddPasswordDialog(user?.authentication?.localAuthentication?.email?.isNotBlank() != true)
                }
            }
            "privacy_preferences" -> {
                showAsBottomSheet { dismiss ->
                    var analyticsConsent by remember { mutableStateOf(user?.preferences?.analyticsConsent ?: false) }
                    var isSettingConsent by remember { mutableStateOf(false) }
                    PrivacyPreferencesView(
                        analyticsConsent,
                        { newValue ->
                            lifecycleScope.launchCatching {
                                val user = userRepository.updateUser("preferences.analyticsConsent", newValue)
                                analyticsConsent = user?.preferences?.analyticsConsent ?: false
                            }
                        },
                        isSettingConsent
                    )
                }
            }
            "UserID" -> {
                copyValue(getString(R.string.SP_userID), user?.id)
                return true
            }

            "APIToken" -> {
                ApiTokenBottomSheetFragment.newInstance(hostConfig.apiKey).show(childFragmentManager, ApiTokenBottomSheetFragment.TAG)
                return true
            }

            "display_name" ->
                updateUser(
                    "profile.name",
                    user?.profile?.name,
                    getString(R.string.display_name),
                )

            "photo_url" ->
                updateUser(
                    "profile.imageUrl",
                    user?.profile?.imageUrl,
                    getString(R.string.photo_url),
                )

            "about" -> updateUser("profile.blurb", user?.profile?.blurb, getString(R.string.about))
            "google_auth" -> {
                if (user?.authentication?.hasGoogleAuth == true) {
                    disconnect("google", "Google")
                } else {
                    activity?.let {
                        viewModel.startGoogleAuth(it)
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

    private fun disconnect(
        network: String,
        networkName: String,
    ) {
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.are_you_sure)
            dialog.addButton(R.string.disconnect, true) { _, _ ->
                lifecycleScope.launch {
                    viewModel.removeSocialAuth(network)
                    displayDisconnectSuccess(networkName)
                }
            }
            dialog.addCancelButton()
            dialog.show()
        }
    }

    private fun displayAuthenticationSuccess(network: String) {
        (activity as? SnackbarActivity)?.showSnackbar(
            content = context?.getString(R.string.added_social_auth, network),
            displayType = HabiticaSnackbar.SnackbarDisplayType.SUCCESS,
        )
    }

    private fun displayDisconnectSuccess(network: String) {
        (activity as? SnackbarActivity)?.showSnackbar(
            content = context?.getString(R.string.removed_social_auth, network),
            displayType = HabiticaSnackbar.SnackbarDisplayType.SUCCESS,
        )
    }

    private fun updateUser(
        path: String,
        value: String?,
        title: String,
    ) {
        showSingleEntryDialog(value, title) {
            if (value != it) {
                lifecycleScope.launchCatching {
                    userRepository.updateUser(path, it ?: "")
                }
            }
        }
    }

    private fun showChangePasswordDialog() {
        ChangePasswordBottomSheet(
            onForgotPassword = { showForgotPasswordDialog() },
            onPasswordChanged = { oldPassword, newPassword ->
                lifecycleScope.launchCatching {
                    KeyboardUtil.dismissKeyboard(activity)
                    lifecycleScope.launchCatching {
                        val response = userRepository.updatePassword(
                            oldPassword,
                            newPassword,
                            newPassword,
                        )
                        response?.apiToken?.let {
                            viewModel.saveTokens(it, user?.id ?: "")
                        }
                    }
                }
            }
        ).show(childFragmentManager, ChangePasswordBottomSheet.TAG)

    }

    private fun showForgotPasswordDialog() {
        val input = EditText(requireContext())
        input.setAutofillHints(EditText.AUTOFILL_HINT_EMAIL_ADDRESS)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.hint = getString(R.string.forgot_password_hint_example)
        input.textSize = 16f
        val lp =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        input.layoutParams = lp
        val alertDialog = HabiticaAlertDialog(requireContext())
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
        val alert = HabiticaAlertDialog(requireContext())
        alert.setMessage(R.string.forgot_password_confirmation)
        alert.addOkButton()
        alert.show()
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
        val passwordRepeatEditText =
            view?.findViewById<ValidatingEditText>(R.id.password_repeat_edit_text)
        passwordRepeatEditText?.validator = { it == passwordEditText?.text }
        passwordRepeatEditText?.errorText = getString(R.string.password_not_matching)
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            if (showEmail) {
                dialog.setTitle(R.string.add_email_and_password)
            } else {
                dialog.setTitle(R.string.add_password)
            }
            dialog.addButton(R.string.add, true, false, false) { _, _ ->
                KeyboardUtil.dismissKeyboard(activity)
                emailEditText?.showErrorIfNecessary()
                passwordEditText?.showErrorIfNecessary()
                passwordRepeatEditText?.showErrorIfNecessary()
                if ((showEmail && emailEditText?.isValid != true) || passwordEditText?.isValid != true || passwordRepeatEditText?.isValid != true) return@addButton
                val email =
                    if (showEmail) emailEditText?.text else user?.authentication?.findFirstSocialEmail()
                lifecycleScope.launchCatching {
                    val response = viewModel.register(
                        user?.username ?: "",
                        email ?: "",
                        passwordEditText.text ?: "",
                    )
                    (activity as? SnackbarActivity)?.showSnackbar(
                        content = context.getString(R.string.password_added),
                        displayType = HabiticaSnackbar.SnackbarDisplayType.SUCCESS,
                    )
                }
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
        emailEditText?.hint = context?.getString(R.string.email)
        val passwordEditText = view?.findViewById<ValidatingEditText>(R.id.password_edit_text)
        if (user?.authentication?.hasPassword != true) {
            passwordEditText?.isVisible = false
        }
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.change_email)
            dialog.addButton(R.string.change, true, false, false) { _, _ ->
                KeyboardUtil.dismissKeyboard(activity)
                emailEditText?.showErrorIfNecessary()
                if (emailEditText?.isValid != true) return@addButton
                lifecycleScope.launchCatching {
                    userRepository.updateEmail(
                        emailEditText.text.toString(),
                        passwordEditText?.text.toString(),
                    )
                    lifecycleScope.launch(ExceptionHandler.coroutine()) {
                        userRepository.retrieveUser(true, true)
                    }
                    configurePreference(findPreference("email"), emailEditText.text.toString())
                }
                dialog.dismiss()
            }
            dialog.addCancelButton()
            dialog.setAdditionalContentView(view)
            dialog.setAdditionalContentSidePadding(12.dpToPx(context))
            dialog.show()
        }
    }

    private val regex = "[^a-zA-Z0-9_-]".toRegex()

    private fun showLoginNameDialog() {
        showSingleEntryDialog(user?.username, getString(R.string.username), {
            it?.contains(" ") == false && it.length > 1 && it.length < 20 && !it.contains(regex)
        }) {
            lifecycleScope.launchCatching {
                val user = userRepository.updateLoginName(it ?: "")
                if (user == null || user.username != it) {
                    userRepository.retrieveUser(false, forced = true)
                }
            }
        }
    }

    private fun showSingleEntryDialog(
        value: String?,
        title: String,
        validator: ((String?) -> Boolean)? = null,
        onChange: (String?) -> Unit,
    ) {
        val inflater = context?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edittext, null)
        val editText = view?.findViewById<ValidatingEditText>(R.id.edit_text)
        editText?.text = value
        editText?.validator = validator
        editText?.errorText = getString(R.string.username_requirements)
        editText?.hint = title
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(title)
            dialog.addButton(R.string.save, true, autoDismiss = false) { _, _ ->
                KeyboardUtil.dismissKeyboard(activity)
                editText?.showErrorIfNecessary()
                if (editText?.isValid != true) return@addButton
                onChange(editText.text)
                dialog.dismiss()
            }
            dialog.addCancelButton()
            dialog.setAdditionalContentView(view)
            dialog.setAdditionalContentSidePadding(12.dpToPx(context))
            dialog.scrollView.isScrollable = false
            dialog.show()
        }
    }

    private fun showAccountDeleteConfirmation(user: User?) {
        if (user?.purchased?.plan?.isActive == true && user.purchased?.plan?.dateTerminated == null) {
            val dialog = context?.let { HabiticaAlertDialog(it) }
            dialog?.setTitle(R.string.unable_to_delete)
            dialog?.setMessage(R.string.delete_account_subscription_active)
            dialog?.addButton(R.string.go_to_subscription, false) { _, _ ->
                MainNavigationController.navigate(R.id.subscriptionPurchaseActivity)
            }
            dialog?.addCloseButton()
            dialog?.show()
            return
        }
        val habiticaAccountDialog = context?.let { HabiticaAccountDialog(it) }
        habiticaAccountDialog?.accountAction = "delete_account"
        habiticaAccountDialog?.accountUpdateConfirmed = this
        habiticaAccountDialog?.user = user
        habiticaAccountDialog?.show(childFragmentManager, HabiticaAccountDialog.TAG)

        if (habiticaAccountDialog != null) {
            accountDialog = habiticaAccountDialog
        }
    }

    private fun deleteAccount(password: String) {
        val dialog = activity?.let { HabiticaProgressDialog.show(it, R.string.deleting_account) }
        lifecycleScope.launchCatching({ throwable ->
            dialog?.dismiss()
            if (throwable is HttpException && throwable.code() == 401) {
                val errorDialog = context?.let { HabiticaAlertDialog(it) }
                errorDialog?.setTitle(R.string.authentication_error_title)
                errorDialog?.setMessage(R.string.incorrect_password)
                errorDialog?.addCloseButton()
                errorDialog?.show()
            }
            ExceptionHandler.reportError(throwable)
        }) {
            userRepository.deleteAccount(password)
            dialog?.dismiss()
            accountDialog.dismiss()
            context?.let {
                val user = userViewModel.user.value
                HabiticaBaseApplication.logout(it, user)
            }
            activity?.finish()
        }
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
            lifecycleScope.launchCatching {
                userRepository.updateLoginName(
                    user?.authentication?.localAuthentication?.username ?: "",
                )
            }
        }
        dialog.addCancelButton()
        dialog.show()
    }

    private fun resetAccount(confirmationString: String) {
        val dialog = activity?.let { HabiticaProgressDialog.show(it, R.string.resetting_account) }
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.resetAccount(confirmationString)
            dialog?.dismiss()
            accountDialog.dismiss()
        }
    }

    private fun copyValue(
        name: String,
        value: CharSequence?,
    ) {
        val clipboard: ClipboardManager? =
            context?.let { getSystemService(it, ClipboardManager::class.java) }
        clipboard?.setPrimaryClip(ClipData.newPlainText(name, value))
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            (activity as? SnackbarActivity)?.showSnackbar(
                content = context?.getString(R.string.copied_to_clipboard, name),
                displayType = HabiticaSnackbar.SnackbarDisplayType.SUCCESS,
            )
        }
    }

    override fun onSharedPreferenceChanged(
        p0: SharedPreferences?,
        p1: String?,
    ) {
    }

    override fun resetConfirmedClicked(confirmationString: String) {
        resetAccount(confirmationString)
    }

    override fun deletionConfirmClicked(confirmationString: String) {
        deleteAccount(confirmationString)
    }
}
