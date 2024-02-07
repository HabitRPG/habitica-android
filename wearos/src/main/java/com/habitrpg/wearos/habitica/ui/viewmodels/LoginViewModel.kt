package com.habitrpg.wearos.habitica.ui.viewmodels

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.UserRecoverableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.habitrpg.common.habitica.helpers.KeyHelper
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import com.habitrpg.common.habitica.models.auth.UserAuthSocial
import com.habitrpg.common.habitica.models.auth.UserAuthSocialTokens
import com.habitrpg.wearos.habitica.data.ApiClient
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.managers.AppStateManager
import com.habitrpg.wearos.habitica.util.ExceptionHandlerBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    userRepository: UserRepository,
    taskRepository: TaskRepository,
    exceptionBuilder: ExceptionHandlerBuilder,
    private val keyHelper: KeyHelper?,
    val sharedPreferences: SharedPreferences,
    val apiClient: ApiClient,
    appStateManager: AppStateManager
) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, appStateManager) {
    lateinit var onLoginCompleted: () -> Unit

    fun handleGoogleLogin(
        activity: Activity,
        pickAccountResult: ActivityResultLauncher<Intent>
    ) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(activity, gso)
        pickAccountResult.launch(client.signInIntent)
    }

    fun handleGoogleLoginResult(
        activity: Activity,
        task: Task<GoogleSignInAccount>,
        recoverFromPlayServicesErrorResult: ActivityResultLauncher<Intent>?
    ) {
        viewModelScope.launch(exceptionBuilder.userFacing(this)) {
            val account = async {
                try {
                    return@async task.getResult(
                        ApiException::class.java
                    )
                } catch (e: IOException) {
                    return@async null
                } catch (e: GoogleAuthException) {
                    if (recoverFromPlayServicesErrorResult != null) {
                        handleGoogleAuthException(e, activity, recoverFromPlayServicesErrorResult)
                    }
                    return@async null
                } catch (e: UserRecoverableException) {
                    return@async null
                }
            }.await()
            val scopesString = Scopes.PROFILE + " " + Scopes.EMAIL
            val scopes = "oauth2:$scopesString"
            val token = withContext(Dispatchers.IO) {
                account?.account?.let { GoogleAuthUtil.getToken(activity, it, scopes) }
            }
            val auth = UserAuthSocial()
            auth.network = "google"
            auth.authResponse = UserAuthSocialTokens()
            auth.authResponse?.client_id = account?.email
            auth.authResponse?.access_token = token
            val response = apiClient.loginSocial(auth)
            handleAuthResponse(response.responseData)
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

    private suspend fun handleAuthResponse(userAuthResponse: UserAuthResponse?) {
        if (userAuthResponse == null) return
        try {
            saveTokens(userAuthResponse.apiToken, userAuthResponse.id)
        } catch (e: Exception) {
            return
        }
        val user = userRepository.retrieveUser(true)
        taskRepository.retrieveTasks(user?.tasksOrder, true)
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

    companion object {
        private const val REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001
    }
}
