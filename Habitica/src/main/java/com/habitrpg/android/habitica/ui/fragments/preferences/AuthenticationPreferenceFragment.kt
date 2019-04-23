package com.habitrpg.android.habitica.ui.fragments.preferences

import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionDetailsView
import io.reactivex.functions.Consumer
import javax.inject.Inject

class AuthenticationPreferenceFragment: BasePreferencesFragment() {

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
        HabiticaBaseApplication.component?.inject(this)
        super.onCreate(savedInstanceState)

        findPreference("login_name").title = context?.getString(R.string.username)
        findPreference("confirm_username").isVisible = user?.flags?.isVerifiedUsername != true
    }

    private fun updateUserFields() {
        configurePreference(findPreference("login_name"), user?.authentication?.localAuthentication?.username, false)
        configurePreference(findPreference("email"), user?.authentication?.localAuthentication?.email, true)
        findPreference("change_password").isVisible = user?.authentication?.localAuthentication?.email?.isNotEmpty() == true
        findPreference("add_local_auth").isVisible = user?.authentication?.localAuthentication?.email?.isNotEmpty() != true
        findPreference("confirm_username").isVisible = user?.flags?.isVerifiedUsername != true
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
                MainNavigationController.navigate(R.id.gemPurchaseActivity)
            }
            "reset_account" -> showAccountResetConfirmation()
            "delete_account" -> showAccountDeleteConfirmation()
            "add_local_auth" -> showAddLocalNotificationDialog()
            else -> {
                val clipMan = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipMan?.primaryClip = ClipData.newPlainText(preference.key, preference.summary)
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
        context.notNull { context ->
            val dialog = AlertDialog.Builder(context)
                    .setTitle(R.string.change_password)
                    .setPositiveButton(R.string.change) { thisDialog, _ ->
                        thisDialog.dismiss()
                        userRepository.updatePassword(oldPasswordEditText?.text.toString(), passwordEditText?.text.toString(), passwordRepeatEditText?.text.toString())
                                .subscribe(Consumer {
                                    Toast.makeText(activity, R.string.password_changed, Toast.LENGTH_SHORT).show()
                                }, RxErrorHandler.handleEmptyError())
                    }
                    .setNegativeButton(R.string.action_cancel) { thisDialog, _ -> thisDialog.dismiss() }
                    .create()
            dialog.setView(view)
            dialog.show()
        }
    }

    private fun showEmailDialog() {
        val inflater = context?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edittext_confirm_pw, null)
        val emailEditText = view?.findViewById<EditText>(R.id.editText)
        emailEditText?.setText(user?.authentication?.localAuthentication?.email)
        val passwordEditText = view?.findViewById<EditText>(R.id.passwordEditText)
        context.notNull { context ->
            val dialog = AlertDialog.Builder(context)
                    .setTitle(R.string.change_email)
                    .setPositiveButton(R.string.change) { thisDialog, _ ->
                        thisDialog.dismiss()
                        userRepository.updateEmail(emailEditText?.text.toString(), passwordEditText?.text.toString())
                                .subscribe(Consumer {
                                    configurePreference(findPreference("email"), emailEditText?.text.toString(), true)
                                }, RxErrorHandler.handleEmptyError())
                    }
                    .setNegativeButton(R.string.action_cancel) { thisDialog, _ -> thisDialog.dismiss() }
                    .create()
            dialog.setView(view)
            dialog.show()
        }
    }

    private fun showLoginNameDialog() {
        val inflater = context?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edittext, null)
        val loginNameEditText = view?.findViewById<EditText>(R.id.editText)
        loginNameEditText?.setText(user?.authentication?.localAuthentication?.username)
        context.notNull { context ->
            val builder = AlertDialog.Builder(context).setTitle(R.string.change_username)
            val dialog = builder.setPositiveButton(R.string.save) { thisDialog, _ ->
                        thisDialog.dismiss()
                        userRepository.updateLoginName(loginNameEditText?.text.toString())
                                .subscribe(Consumer {
                                    configurePreference(findPreference("login_name"), loginNameEditText?.text.toString(), true)
                                }, RxErrorHandler.handleEmptyError())
                    }
                    .setNegativeButton(R.string.action_cancel) { thisDialog, _ -> thisDialog.dismiss() }
                    .create()
            dialog.setView(view)
            dialog.show()
        }
    }

    private fun showAccountDeleteConfirmation() {
        val input = EditText(context)
        var deleteMessage = getString(R.string.delete_account_description)
        if (user?.authentication?.localAuthentication != null) {
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            deleteMessage = getString(R.string.delete_oauth_account_description)
            input.inputType = InputType.TYPE_CLASS_TEXT
        }
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        context.notNull { context ->
            val dialog = AlertDialog.Builder(context)
                    .setTitle(R.string.delete_account)
                    .setMessage(deleteMessage)
                    .setPositiveButton(R.string.delete_account_confirmation) { thisDialog, _ ->
                        thisDialog.dismiss()
                        deleteAccount(input.text.toString())
                    }
                    .setNegativeButton(R.string.nevermind) { thisDialog, _ -> thisDialog.dismiss() }
                    .create()
            dialog.setOnShowListener { _ -> dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.red_10)) }
            dialog.setView(input)
            dialog.show()
        }
    }

    private fun showAddLocalNotificationDialog() {
        val inflater = context?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edittext_add_local_auth, null)
        val emailEditText = view?.findViewById<EditText>(R.id.editText)
        val passwordEditText = view?.findViewById<EditText>(R.id.passwordEditText)
        val passwordRepeatEditText = view?.findViewById<EditText>(R.id.passwordRepeatEditText)
        context.notNull { context ->
            val dialog = AlertDialog.Builder(context)
                    .setTitle(R.string.add_local_authentication)
                    .setPositiveButton(R.string.save) { thisDialog, _ ->
                        if (passwordEditText?.text == passwordRepeatEditText?.text) {
                            return@setPositiveButton
                        }
                        thisDialog.dismiss()
                        apiClient.registerUser(user?.username ?: "", emailEditText?.text.toString(), passwordEditText?.text.toString(), passwordRepeatEditText?.text.toString())
                                .flatMap { userRepository.retrieveUser(false) }
                                .subscribe(Consumer {
                                    configurePreference(findPreference("email"), emailEditText?.text.toString(), true)
                                }, RxErrorHandler.handleEmptyError())
                    }
                    .setNegativeButton(R.string.action_cancel) { thisDialog, _ -> thisDialog.dismiss() }
                    .create()
            dialog.setView(view)
            dialog.show()
        }
    }

    private fun deleteAccount(password: String) {
        @Suppress("DEPRECATION")
        val dialog = ProgressDialog.show(context, context?.getString(R.string.deleting_account), null, true)
        userRepository.deleteAccount(password).subscribe({ _ ->
            context.notNull { HabiticaBaseApplication.logout(it) }
            activity?.finish()
        }) { throwable ->
            dialog.dismiss()
            RxErrorHandler.reportError(throwable)
        }
    }

    private fun showAccountResetConfirmation() {
        context.notNull { context ->
            val dialog = AlertDialog.Builder(context)
                    .setTitle(R.string.reset_account)
                    .setMessage(R.string.reset_account_description)
                    .setPositiveButton(R.string.reset_account_confirmation) { thisDialog, _ ->
                        thisDialog.dismiss()
                        resetAccount()
                    }
                    .setNegativeButton(R.string.nevermind) { thisDialog, _ -> thisDialog.dismiss() }
                    .create()
            dialog.setOnShowListener { _ -> dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.red_10)) }
            dialog.show()
        }
    }

    private fun showConfirmUsernameDialog() {
        context.notNull { context ->
            val dialog = AlertDialog.Builder(context)
                    .setTitle(R.string.confirm_username_title)
                    .setMessage(R.string.confirm_username_description)
                    .setPositiveButton(R.string.confirm) { thisDialog, _ ->
                        thisDialog.dismiss()
                        userRepository.updateLoginName(user?.authentication?.localAuthentication?.username ?: "")
                                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                    }
                    .setNegativeButton(R.string.cancel) { thisDialog, _ -> thisDialog.dismiss() }
                    .create()
            dialog.show()
        }
    }

    private fun resetAccount() {
        @Suppress("DEPRECATION")
        val dialog = ProgressDialog.show(context, context?.getString(R.string.resetting_account), null, true)
        userRepository.resetAccount().subscribe({ _ -> dialog.dismiss() }) { throwable ->
            dialog.dismiss()
            RxErrorHandler.reportError(throwable)
        }
    }

    private fun showSubscriptionStatusDialog() {
        context.notNull { context ->
            val view = SubscriptionDetailsView(context)
            user?.purchased?.plan?.notNull {
                view.setPlan(it)
            }
            val dialog = AlertDialog.Builder(context)
                    .setView(view)
                    .setTitle(R.string.subscription_status)
                    .setPositiveButton(R.string.close) { dialogInterface, _ -> dialogInterface.dismiss() }.create()
            dialog.show()
        }
    }
}