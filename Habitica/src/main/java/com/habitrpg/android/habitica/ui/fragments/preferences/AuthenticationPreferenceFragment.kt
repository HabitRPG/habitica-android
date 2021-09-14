package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.preference.Preference
import com.google.android.material.textfield.TextInputLayout
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaProgressDialog
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionDetailsView
import javax.inject.Inject

class AuthenticationPreferenceFragment : BasePreferencesFragment() {

    @Inject
    lateinit var configManager: AppConfigManager

    @Inject
    lateinit var apiClient: ApiClient

    override var user: User? = null
        set(value) {
            field = value
            updateUserFields()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.userComponent?.inject(this)
        super.onCreate(savedInstanceState)

        findPreference<Preference>("login_name")?.title = context?.getString(R.string.username)
        findPreference<Preference>("confirm_username")?.isVisible = user?.flags?.verifiedUsername != true
    }

    private fun updateUserFields() {
        configurePreference(findPreference("login_name"), user?.authentication?.localAuthentication?.username, false)
        configurePreference(findPreference("email"), user?.authentication?.localAuthentication?.email, true)
        findPreference<Preference>("change_password")?.isVisible = user?.authentication?.localAuthentication?.email?.isNotEmpty() == true
        findPreference<Preference>("add_local_auth")?.isVisible = user?.authentication?.localAuthentication?.email?.isNotEmpty() != true
        findPreference<Preference>("confirm_username")?.isVisible = user?.flags?.verifiedUsername != true
        val preference = findPreference<Preference>("authentication_methods")
        val methods = mutableListOf<String>()
        if (user?.authentication?.localAuthentication?.email != null) {
            context?.getString(R.string.local)?.let { methods.add(it) }
        }
        if (user?.authentication?.hasFacebookAuth == true) { context?.getString(R.string.facebook)?.let { methods.add(it) } }
        if (user?.authentication?.hasGoogleAuth == true) { context?.getString(R.string.google)?.let { methods.add(it) } }
        if (user?.authentication?.hasAppleAuth == true) { context?.getString(R.string.apple_sign_in)?.let { methods.add(it) } }
        preference?.summary = methods.joinToString(", ")
    }

    private fun configurePreference(preference: Preference?, value: String?, hideIfEmpty: Boolean) {
        preference?.summary = value
        if (hideIfEmpty) {
            preference?.isVisible = value?.isNotEmpty() == true
        }
    }

    override fun setupPreferences() {
        updateUserFields()
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "login_name" -> showLoginNameDialog()
            "confirm_username" -> showConfirmUsernameDialog()
            "email" -> showEmailDialog()
            "change_password" -> showChangePasswordDialog()
            "subscription_status" -> {
                val plan = user?.purchased?.plan
                if (plan?.isActive == true) {
                    showSubscriptionStatusDialog()
                    return super.onPreferenceTreeClick(preference)
                }
                MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", true)))
            }
            "reset_account" -> showAccountResetConfirmation()
            "delete_account" -> showAccountDeleteConfirmation()
            "add_local_auth" -> showAddLocalAuthDialog()
            else -> {
                val clipMan = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipMan?.setPrimaryClip(ClipData.newPlainText(preference.key, preference.summary))
                Toast.makeText(activity, "Copied " + preference.key + " to clipboard.", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onPreferenceTreeClick(preference)
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
                            configurePreference(findPreference("email"), emailEditText?.text.toString(), true)
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
                            configurePreference(findPreference("login_name"), loginNameEditText?.text.toString(), true)
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

    private fun showAddLocalAuthDialog() {
        val inflater = context?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edittext_add_local_auth, null)
        val emailEditText = view?.findViewById<EditText>(R.id.emailTitleTextView)
        val passwordEditText = view?.findViewById<EditText>(R.id.passwordEditText)
        val passwordRepeatEditText = view?.findViewById<EditText>(R.id.passwordRepeatEditText)
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.add_local_authentication)
            dialog.addButton(R.string.save, true) { thisDialog, _ ->
                if (passwordEditText?.text == passwordRepeatEditText?.text) {
                    return@addButton
                }
                thisDialog.dismiss()
                apiClient.registerUser(user?.username ?: "", emailEditText?.text.toString(), passwordEditText?.text.toString(), passwordRepeatEditText?.text.toString())
                    .flatMap { userRepository.retrieveUser(false) }
                    .subscribe(
                        {
                            configurePreference(findPreference("email"), emailEditText?.text.toString(), true)
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

    private fun showSubscriptionStatusDialog() {
        context?.let { context ->
            val view = SubscriptionDetailsView(context)
            user?.purchased?.plan?.let {
                view.setPlan(it)
            }
            val dialog = HabiticaAlertDialog(context)
            dialog.setAdditionalContentView(view)
            dialog.setTitle(R.string.subscription_status)
            dialog.addCloseButton()
            dialog.show()
        }
    }
}
