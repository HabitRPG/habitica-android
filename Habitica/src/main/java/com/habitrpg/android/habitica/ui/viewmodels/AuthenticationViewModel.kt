package com.habitrpg.android.habitica.ui.viewmodels

import android.accounts.AccountManager
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.edit
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.Scopes
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.api.HostConfig
import com.habitrpg.common.habitica.helpers.AnalyticsManager
import com.habitrpg.common.habitica.helpers.KeyHelper
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class AuthenticationViewModel @Inject constructor(
    val apiClient : ApiClient,
    val userRepository : UserRepository,
    val sharedPrefs : SharedPreferences,
    val authenticationHandler : AuthenticationHandler,
    val hostConfig : HostConfig,
    val analyticsManager : AnalyticsManager,
    private val keyHelper : KeyHelper?
) {
    var googleEmail : String? = null

    fun handleGoogleLogin(
        activity : Activity,
        pickAccountResult : ActivityResultLauncher<Intent>
    ) {
        if (!checkPlayServices(activity)) {
            return
        }
        val accountTypes = arrayOf("com.google")
        val intent = AccountManager.newChooseAccountIntent(
            null, null,
            accountTypes, true, null, null, null, null
        )
        try {
            pickAccountResult.launch(intent)
        } catch (e : ActivityNotFoundException) {
            val alert = HabiticaAlertDialog(activity)
            alert.setTitle(R.string.authentication_error_title)
            alert.setMessage(R.string.google_services_missing)
            alert.addCloseButton()
            alert.show()
        }
    }

    fun handleGoogleLoginResult(
        activity : Activity,
        recoverFromPlayServicesErrorResult : ActivityResultLauncher<Intent>?,
        onSuccess : (Boolean) -> Unit
    ) {
        val scopesString = Scopes.PROFILE + " " + Scopes.EMAIL
        val scopes = "oauth2:$scopesString"
        var newUser : Boolean
        CoroutineScope(Dispatchers.IO).launchCatching({ throwable ->
            if (recoverFromPlayServicesErrorResult == null) return@launchCatching
            if (throwable is GoogleAuthException) {
                handleGoogleAuthException(
                    throwable,
                    activity,
                    recoverFromPlayServicesErrorResult
                )
            }
        }) {
            val token = GoogleAuthUtil.getToken(activity, googleEmail ?: "", scopes)
            val response =
                apiClient.connectSocial("google", googleEmail ?: "", token) ?: return@launchCatching
            newUser = response.newUser
            handleAuthResponse(response)
            onSuccess(newUser)
        }
    }

    private fun handleGoogleAuthException(
        e : Exception,
        activity : Activity,
        recoverFromPlayServicesErrorResult : ActivityResultLauncher<Intent>
    ) {
        if (e is GooglePlayServicesAvailabilityException) {
            GoogleApiAvailability.getInstance()
            GooglePlayServicesUtil.showErrorDialogFragment(
                e.connectionStatusCode,
                activity,
                null,
                REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR
            ) {
            }
            return
        } else if (e is UserRecoverableAuthException) {
            // Unable to authenticate, such as when the user has not yet granted
            // the app access to the account, but the user can fix this.
            // Forward the user to an activity in Google Play services.
            if (!activity.isFinishing) {
                val intent = e.intent
                recoverFromPlayServicesErrorResult.launch(intent)
            }
        }
    }

    private fun checkPlayServices(activity : Activity) : Boolean {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(activity)
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(
                    activity, result,
                    PLAY_SERVICES_RESOLUTION_REQUEST
                )?.show()
            }
            return false
        }

        return true
    }

    fun handleAuthResponse(userAuthResponse : UserAuthResponse) {
        try {
            saveTokens(userAuthResponse.apiToken, userAuthResponse.id)
        } catch (e : Exception) {
            analyticsManager.logException(e)
        }
    }

    @Throws(Exception::class)
    private fun saveTokens(api : String, user : String) {
        this.apiClient.updateAuthenticationCredentials(user, api)
        authenticationHandler.updateUserID(user)
        sharedPrefs.edit {
            putString("UserID", user)
            val encryptedKey =
                if (keyHelper != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        keyHelper.encrypt(api)
                    } catch (e : Exception) {
                        null
                    }
                } else null
            if ((encryptedKey?.length ?: 0) > 5) {
                putString(user, encryptedKey)
            } else {
                // Something might have gone wrong with encryption, so fall back to this.
                putString("APIToken", api)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    }
}
