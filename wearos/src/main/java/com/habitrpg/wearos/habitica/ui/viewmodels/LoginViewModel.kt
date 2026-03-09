package com.habitrpg.wearos.habitica.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.content.edit
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.habitrpg.android.habitica.BuildConfig
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
@Inject
constructor(
    userRepository: UserRepository,
    taskRepository: TaskRepository,
    exceptionBuilder: ExceptionHandlerBuilder,
    private val keyHelper: KeyHelper?,
    val sharedPreferences: SharedPreferences,
    val apiClient: ApiClient,
    appStateManager: AppStateManager
) : BaseViewModel(userRepository, taskRepository, exceptionBuilder, appStateManager) {
    lateinit var onLoginCompleted: () -> Unit

    val isGoogleLoginSupported: Boolean
        get() = Build.VERSION.SDK_INT >= 34

    fun handleGoogleLogin(context: Context) {
        if (!isGoogleLoginSupported) return

        try {
            val googleIdOption = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_AUTH_CLIENT_ID)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            viewModelScope.launch(exceptionBuilder.userFacing(this)) {
                try {
                    val result = CredentialManager.create(context).getCredential(
                        request = request,
                        context = context,
                    )
                    handleSignIn(context, result)
                } catch (e: GetCredentialException) {
                    Log.e("LoginViewModel", "Get Credential Exception", e)
                } catch (e: ApiException) {
                    Log.e("LoginViewModel", "API Exception", e)
                }
            }
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Failed to start Google login", e)
        }
    }

    private suspend fun handleSignIn(context: Context, result: GetCredentialResponse) {
        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)
                val authorizationRequest = AuthorizationRequest.Builder()
                    .requestOfflineAccess(BuildConfig.GOOGLE_AUTH_CLIENT_ID)
                    .setRequestedScopes(
                        listOf(
                            Scope(Scopes.PROFILE),
                            Scope(Scopes.EMAIL),
                        )
                    )
                    .build()
                val authResult = Identity.getAuthorizationClient(context)
                    .authorize(authorizationRequest).await()
                if (authResult?.accessToken != null) {
                    val auth = UserAuthSocial()
                    auth.network = "google"
                    auth.authResponse = UserAuthSocialTokens()
                    auth.authResponse?.client_id = googleIdTokenCredential.id
                    auth.authResponse?.access_token = authResult.accessToken
                    val response = apiClient.loginSocial(auth)
                    handleAuthResponse(response.responseData)
                } else {
                    Log.e("LoginViewModel", "Received an empty access token response")
                }
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("LoginViewModel", "Invalid google id token response", e)
            }
        } else {
            Log.e("LoginViewModel", "Unexpected credential type: $credential")
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
    private fun saveTokens(
        api: String,
        user: String
    ) {
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
                putString("APIToken", api)
            }
        }
    }
}
