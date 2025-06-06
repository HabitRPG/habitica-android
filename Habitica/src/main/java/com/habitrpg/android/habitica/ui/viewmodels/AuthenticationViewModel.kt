package com.habitrpg.android.habitica.ui.viewmodels


import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
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
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.AuthenticationErrors
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.AnalyticsTarget
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.EventCategory
import com.habitrpg.android.habitica.helpers.HitType
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.common.habitica.api.HostConfig
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.KeyHelper
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    val apiClient: ApiClient,
    val userRepository: UserRepository,
    val sharedPrefs: SharedPreferences,
    val authenticationHandler: AuthenticationHandler,
    val configManager: AppConfigManager,
    val hostConfig: HostConfig,
    private val keyHelper: KeyHelper?,
) : ViewModel() {
    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val username = mutableStateOf("")

    private val _showAuthProgress = MutableStateFlow(false)
    val showAuthProgress: Flow<Boolean> = _showAuthProgress
    val isRegistering = MutableStateFlow(false)
    private val _authenticationError: MutableSharedFlow<AuthenticationErrors?> = MutableSharedFlow()
    val authenticationError: Flow<AuthenticationErrors?> = _authenticationError
        .onEach { _showAuthProgress.value = false }
    private val _authenticationSuccess = MutableStateFlow<Boolean?>(null)
    val authenticationSuccess: Flow<Boolean> = _authenticationSuccess
        .filterNotNull()
        .onEach { _showAuthProgress.value = false }
    private val _isUsernameValid = MutableStateFlow<Boolean?>(null)
    val isUsernameValid: Flow<Boolean?> = _isUsernameValid
    private var _usernameIssues = MutableStateFlow<String?>(null)
    val usernameIssues: Flow<String?> = _usernameIssues

    fun validateInputs(
        username: String,
        password: String,
        email: String? = null,
        confirmPassword: String? = null,
    ): AuthenticationErrors? {
        if (username.isBlank() || password.isBlank()) {
            return AuthenticationErrors.MISSING_FIELDS
        }
        if (isRegistering.value) {
            if (email.isNullOrBlank()) {
                return AuthenticationErrors.MISSING_FIELDS
            }
            if (password.length < configManager.minimumPasswordLength()) {
                return AuthenticationErrors.PASSWORD_TOO_SHORT.apply {
                     minPasswordLength = configManager.minimumPasswordLength()
                }
            }
            if (password != confirmPassword) {
                return AuthenticationErrors.PASSWORD_MISMATCH
            }
        }
        return null
    }

    fun checkUsername(username: String) {
        viewModelScope.launch {
            try {
                val response = apiClient.verifyUsername(username)
                _isUsernameValid.value = response?.isUsable == true
                _usernameIssues.value = response?.issues?.joinToString("\n") { it }
            } catch (e: Exception) {
                _isUsernameValid.value = null
                Analytics.logException(e)
            }
        }
    }

    fun invalidateUsernameState() {
        _isUsernameValid.value = null
        _usernameIssues.value = null
    }

    fun login(username: String, password: String) {
        _showAuthProgress.value = true
        viewModelScope.launch {
            try {
                val response = apiClient.connectUser(username, password)
                handleAuthResponse(response)
            } catch (e: Exception) {
                authenticationError()
                Analytics.logException(e)
            }
        }
    }

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        _showAuthProgress.value = true
        viewModelScope.launch {
            try {
                val response = apiClient.registerUser(username, email, password, confirmPassword)
                handleAuthResponse(response)
            } catch (e: Exception) {
                authenticationError()
                Analytics.logException(e)
            }
        }
    }

    suspend fun removeSocialAuth(network: String) {
        apiClient.disconnectSocial(network)
        userRepository.retrieveUser(true, forced = true)
    }

    private fun authenticationError(error: AuthenticationErrors? = null) {
        viewModelScope.launch { _authenticationError.emit(error) }
    }

    private fun handleAuthResponse(response: UserAuthResponse?) {
        if (response == null) {
            authenticationError()
            return
        }
        try {
            saveTokens(response.apiToken, response.id)
        } catch (e: Exception) {
            Analytics.logException(e)
        }

        if (isRegistering.value) {
            Analytics.sendEvent("user_registered", EventCategory.BEHAVIOUR, HitType.EVENT, target = AnalyticsTarget.FIREBASE)
        } else {
            Analytics.sendEvent("login", EventCategory.BEHAVIOUR, HitType.EVENT)
        }
        viewModelScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(true, true)
            _authenticationSuccess.value = isRegistering.value
        }
    }

    @Throws(Exception::class)
    fun saveTokens(
        api: String,
        user: String,
    ) {
        this.apiClient.updateAuthenticationCredentials(user, api)
        authenticationHandler.updateUserID(user)
        sharedPrefs.edit {
            putString("UserID", user)
            val encryptedKey =
                if (keyHelper != null) {
                    try {
                        keyHelper.encrypt(api)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            if ((encryptedKey?.length ?: 0) > 5) {
                putString(user, encryptedKey)
            } else {
                putString("APIToken", api)
            }
        }
    }

    fun startGoogleAuth(context: Context) {
        try {
            val googleIdOption = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_AUTH_CLIENT_ID)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            viewModelScope.launch {
                try {
                    val result = CredentialManager.create(context).getCredential(
                        request = request,
                        context = context,
                    )
                    handleSignIn(context, result)
                } catch (e: GetCredentialException) {
                    authenticationError(AuthenticationErrors.GET_CREDENTIALS_ERROR)
                    Log.e("AuthenticationViewModel", "Get Credential Exception", e)
                }
            }
        } catch (e: ApiException) {
            authenticationError(AuthenticationErrors.GET_CREDENTIALS_ERROR)
            Log.e("AuthenticationViewModel", "API Exception", e)
        } catch (e: Exception) {
            authenticationError(AuthenticationErrors.GET_CREDENTIALS_ERROR)
            Log.e("AuthenticationViewModel", "Unknown Exception", e)
        }
    }

    private suspend fun handleSignIn(context: Context, result: GetCredentialResponse) {
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
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
                        val result = Identity.getAuthorizationClient(context)
                            .authorize(authorizationRequest).await()
                        if (result != null && result.accessToken != null) {
                            val response = result.accessToken?.let { apiClient.connectSocial("google", googleIdTokenCredential.id, it) }
                            handleAuthResponse(response)
                        }
                    } catch (e: GoogleIdTokenParsingException) {
                        authenticationError(AuthenticationErrors.INVALID_CREDENTIALS)
                        Log.e("AuthenticationViewModel", "Received an invalid google id token response", e)
                    }
                } else {
                    authenticationError(AuthenticationErrors.INVALID_CREDENTIALS)
                    Log.e("AuthenticationViewModel", "Unexpected type of credential")
                }
            }
            else -> {
                authenticationError(AuthenticationErrors.INVALID_CREDENTIALS)
                Log.e("AuthenticationViewModel", "Unexpected type of credential")
            }
        }
    }
}
