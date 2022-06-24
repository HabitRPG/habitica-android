package com.habitrpg.wearos.habitica.ui.viewmodels

import android.accounts.AccountManager
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.UserRecoverableException
import com.habitrpg.common.habitica.helpers.KeyHelper
import com.habitrpg.common.habitica.models.auth.UserAuth
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import com.habitrpg.common.habitica.models.auth.UserAuthSocial
import com.habitrpg.wearos.habitica.data.ApiClient
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.LoadingManager
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(userRepository: UserRepository,
    exceptionBuilder: ExceptionHandlerBuilder,
    val keyHelper: KeyHelper?,
    val sharedPreferences: SharedPreferences,
    val apiClient: ApiClient, loadingManager: LoadingManager
) : BaseViewModel(userRepository, exceptionBuilder, loadingManager) {
    lateinit var onLoginCompleted: () -> Unit
    var googleEmail: String? = null

    fun handleGoogleLogin(
        activity: Activity,
        pickAccountResult: ActivityResultLauncher<Intent>
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
        } catch (e: ActivityNotFoundException) {
            /*val alert = AlertDialog.Builder(activity).create()
            alert.setTitle(R.string.authentication_error_title)
            alert.setMessage(R.string.google_services_missing)
            alert.addCloseButton()
            alert.show()*/
        }
    }

    fun handleGoogleLoginResult(
        activity: Activity,
        recoverFromPlayServicesErrorResult: ActivityResultLauncher<Intent>?,
    ) {
        val scopesString = Scopes.PROFILE + " " + Scopes.EMAIL
        val scopes = "oauth2:$scopesString"
        viewModelScope.launch(exceptionBuilder.userFacing(this)) {
            val token = launch(Dispatchers.IO) {
                try {
                    GoogleAuthUtil.getToken(activity, googleEmail ?: "", scopes)
                } catch (e: IOException) {
                    return@launch
                } catch (e: GoogleAuthException) {
                    if (recoverFromPlayServicesErrorResult != null) {
                        handleGoogleAuthException(e, activity, recoverFromPlayServicesErrorResult)
                    }
                    return@launch
                } catch (e: UserRecoverableException) {
                    return@launch
                }
            }
            val response = apiClient.loginSocial(UserAuthSocial())
            handleAuthResponse(response)
        }
    }

    private fun handleGoogleAuthException(
        e: Exception,
        activity: Activity,
        recoverFromPlayServicesErrorResult: ActivityResultLauncher<Intent>
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
            val intent = e.intent
            recoverFromPlayServicesErrorResult.launch(intent)
            return
        }
    }

    private fun checkPlayServices(activity: Activity): Boolean {
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

    suspend fun handleAuthResponse(userAuthResponse: UserAuthResponse?) {
        if (userAuthResponse == null) return
        try {
            saveTokens(userAuthResponse.apiToken, userAuthResponse.id)
        } catch (e: Exception) {
        }
        userRepository.retrieveUser()
        onLoginCompleted()
    }

    @Throws(Exception::class)
    private fun saveTokens(api: String, user: String) {
        this.apiClient.updateAuthenticationCredentials(user, api)
        sharedPreferences.edit {
            putString("UserID", user)
            val encryptedKey =
                try {
                    keyHelper?.encrypt(api)
                } catch (e: Exception) {
                    null
                }
            if ((encryptedKey?.length ?: 0) > 5) {
                putString(user, encryptedKey)
            } else {
                // Something might have gone wrong with encryption, so fall back to this.
                putString("APIToken", api)
            }
        }
    }

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(exceptionBuilder.userFacing(this)) {
            val response = apiClient.loginLocal(UserAuth(username, password))
            handleAuthResponse(response)
            onResult(response?.id != null)
        }.invokeOnCompletion {
            onResult(it == null)
        }
    }

    companion object {
        private const val REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    }
}
