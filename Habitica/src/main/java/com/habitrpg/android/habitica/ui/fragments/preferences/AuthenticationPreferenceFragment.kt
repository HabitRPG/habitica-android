package com.habitrpg.android.habitica.ui.fragments.preferences

import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.preference.Preference
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionDetailsView
import org.greenrobot.eventbus.EventBus
import rx.functions.Action1

class AuthenticationPreferenceFragment: BasePreferencesFragment() {

    override var user: User? = null
        set(value) {
            field = value
            updateUserFields()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.getComponent().inject(this)
        super.onCreate(savedInstanceState)
    }

    private fun updateUserFields() {
        configurePreference(findPreference("login_name"), user?.authentication?.localAuthentication?.username)
        configurePreference(findPreference("email"), user?.authentication?.localAuthentication?.email)
    }

    private fun configurePreference(preference: Preference?, value: String?) {
        preference?.summary = value
    }

    override fun setupPreferences() {
        updateUserFields()
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "login_name" -> showLoginNameDialog()
            "email" -> showEmailDialog()
            "change_password" -> showChangePasswordDialog()
            "subscription_status" -> {
                if (user != null && user!!.purchased != null && user!!.purchased.plan != null) {
                    val plan = user!!.purchased.plan
                    if (plan.isActive) {
                        showSubscriptionStatusDialog()
                        return super.onPreferenceTreeClick(preference)
                    }
                }
                EventBus.getDefault().post(OpenGemPurchaseFragmentCommand())
            }
            "reset_account" -> showAccountResetConfirmation()
            "delete_account" -> showAccountDeleteConfirmation()
            else -> {
                val clipMan = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipMan.primaryClip = ClipData.newPlainText(preference.key, preference.summary)
                Toast.makeText(activity, "Copied " + preference.key + " to clipboard.", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun showChangePasswordDialog() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun showEmailDialog() {
        val inflater = context?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_edittext_confirm_pw, null)
        val emailEditText = view?.findViewById<EditText>(R.id.editText)
        emailEditText?.setText(user?.authentication?.localAuthentication?.email)
        val passwordEditText = view?.findViewById<EditText>(R.id.passwordEditText)
        val context = context
        if (context != null) {
            val dialog = AlertDialog.Builder(context)
                    .setTitle(R.string.change_email)
                    .setPositiveButton(R.string.change) { thisDialog, _ ->
                        thisDialog.dismiss()
                        userRepository.updateEmail(emailEditText?.text.toString(), passwordEditText?.text.toString())
                                .subscribe(Action1 {
                                    configurePreference(findPreference("email"), emailEditText?.text.toString())
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
        val view = inflater?.inflate(R.layout.dialog_edittext_confirm_pw, null)
        val loginNameEditText = view?.findViewById<EditText>(R.id.editText)
        loginNameEditText?.setText(user?.authentication?.localAuthentication?.username)
        val passwordEditText = view?.findViewById<EditText>(R.id.passwordEditText)
        val context = context
        if (context != null) {
            val dialog = AlertDialog.Builder(context)

                    .setTitle(R.string.change_login_name)
                    .setPositiveButton(R.string.change) { thisDialog, _ ->
                        thisDialog.dismiss()
                        userRepository.updateLoginName(loginNameEditText?.text.toString(), passwordEditText?.text.toString())
                                .subscribe(Action1 {
                                    configurePreference(findPreference("login_name"), loginNameEditText?.text.toString())
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
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        val context = context
        if (context != null) {
            val dialog = AlertDialog.Builder(context)
                    .setTitle(R.string.delete_account)
                    .setMessage(R.string.delete_account_description)
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

    private fun deleteAccount(password: String) {
        val dialog = ProgressDialog.show(context, context?.getString(R.string.deleting_account), null, true)
        userRepository.deleteAccount(password).subscribe({ _ ->
            HabiticaApplication.logout(context)
            activity?.finish()
        }) { throwable ->
            dialog.dismiss()
            RxErrorHandler.reportError(throwable)
        }
    }

    private fun showAccountResetConfirmation() {
        val context = context
        if (context != null) {
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

    private fun resetAccount() {
        val dialog = ProgressDialog.show(context, context?.getString(R.string.resetting_account), null, true)
        userRepository.resetAccount().subscribe({ _ -> dialog.dismiss() }) { throwable ->
            dialog.dismiss()
            RxErrorHandler.reportError(throwable)
        }
    }

    private fun showSubscriptionStatusDialog() {
        val view = SubscriptionDetailsView(context)
        view.setPlan(user?.purchased?.plan)
        val context = context
        if (context != null) {
            val dialog = AlertDialog.Builder(context)
                    .setView(view)
                    .setTitle(R.string.subscription_status)
                    .setPositiveButton(R.string.close) { dialogInterface, _ -> dialogInterface.dismiss() }.create()
            dialog.show()
        }
    }
}