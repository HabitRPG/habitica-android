package com.habitrpg.android.habitica.ui.fragments.preferences

import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceCategory
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand
import com.habitrpg.android.habitica.helpers.QrCodeManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionDetailsView
import org.greenrobot.eventbus.EventBus
import rx.Observable
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
        configurePreference(findPreference("display_name"), user?.profile?.name)
        configurePreference(findPreference("photo_url"), user?.profile?.imageUrl)
        configurePreference(findPreference("about"), user?.profile?.blurb)
    }

    private fun configurePreference(preference: Preference?, value: String?) {
        val editPreference = preference as? EditTextPreference
        editPreference?.text = value
        preference?.summary = value
    }

    override fun setupPreferences() {
        updateUserFields()
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
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
                val clipMan = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipMan.primaryClip = ClipData.newPlainText(preference.key, preference.summary)
                Toast.makeText(activity, "Copied " + preference.key + " to clipboard.", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun showAccountDeleteConfirmation() {
        val input = EditText(context)
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
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

    private fun deleteAccount(password: String) {
        val dialog = ProgressDialog.show(context, context.getString(R.string.deleting_account), null, true)
        userRepository.deleteAccount(password).subscribe({ _ ->
            HabiticaApplication.logout(context)
            activity.finish()
        }) { throwable ->
            dialog.dismiss()
            RxErrorHandler.reportError(throwable)
        }
    }

    private fun showAccountResetConfirmation() {
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

    private fun resetAccount() {
        val dialog = ProgressDialog.show(context, context.getString(R.string.resetting_account), null, true)
        userRepository.resetAccount().subscribe({ _ -> dialog.dismiss() }) { throwable ->
            dialog.dismiss()
            RxErrorHandler.reportError(throwable)
        }
    }

    private fun showSubscriptionStatusDialog() {
        val view = SubscriptionDetailsView(context)
        view.setPlan(user?.purchased?.plan)
        val dialog = AlertDialog.Builder(context)
                .setView(view)
                .setTitle(R.string.subscription_status)
                .setPositiveButton(R.string.close) { dialogInterface, _ -> dialogInterface.dismiss() }.create()
        dialog.show()
    }
}
