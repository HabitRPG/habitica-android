package com.habitrpg.android.habitica.ui.viewmodels


import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
import com.habitrpg.android.habitica.models.user.User
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
import kotlinx.coroutines.flow.filter
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
    val user = mutableStateOf<User?>(null)
    val isRegistering = mutableStateOf(false)

    private val _showAuthProgress = MutableStateFlow(false)
    private val _authenticationError: MutableSharedFlow<AuthenticationErrors?> = MutableSharedFlow()
    private val _authenticationSuccess = MutableStateFlow<Boolean?>(null)
    private val _isUsernameValid = MutableStateFlow<Boolean?>(null)
    private var _usernameIssues = MutableStateFlow<String?>(null)

    val showAuthProgress: Flow<Boolean> = _showAuthProgress
    val authenticationError: Flow<AuthenticationErrors> = _authenticationError
        .filterNotNull()
        .onEach { _showAuthProgress.value = false }
    val authenticationSuccess: Flow<Boolean?> = _authenticationSuccess
        .onEach { _showAuthProgress.value = false }
    val isUsernameValid: Flow<Boolean?> = _isUsernameValid
    val usernameIssues: Flow<String?> = _usernameIssues

    fun clearAuthenticationState() {
        _showAuthProgress.value = false
        _authenticationSuccess.value = null
    }

    fun checkUsername() {
        viewModelScope.launch {
            try {
                val response = apiClient.verifyUsername(username.value)
                _isUsernameValid.value = response?.isUsable == true
                _usernameIssues.value = response?.issues?.joinToString("\n") { it }
            } catch (e: Exception) {
                _isUsernameValid.value = null
                Analytics.logException(e)
            }
        }
    }

    fun checkEmail() {
        _showAuthProgress.value = true
        viewModelScope.launch {
            try {
                val response = apiClient.verifyEmail(email.value)
                if (response?.valid == true) {
                    _authenticationSuccess.value = true
                } else {
                    _authenticationError.emit(AuthenticationErrors.INVALID_EMAIL)
                }
                _showAuthProgress.value = false
            } catch (e: Exception) {
                Analytics.logException(e)
                _showAuthProgress.value = false
            }
        }
    }

    fun invalidateUsernameState() {
        _isUsernameValid.value = null
        _usernameIssues.value = null
    }

    suspend fun login() {
        _showAuthProgress.value = true
        isRegistering.value = false
        try {
            val response = apiClient.connectUser(email.value, password.value)
            handleAuthResponse(response)
        } catch (e: Exception) {
            authenticationError()
            Analytics.logException(e)
        }
    }

    suspend fun register(username: String? = null, email: String? = null, password: String? = null) {
        _showAuthProgress.value = true
        isRegistering.value = true
        try {
            val response = apiClient.registerUser(username ?: this@AuthenticationViewModel.username.value,
                email ?: this@AuthenticationViewModel.email.value,
                password ?: this@AuthenticationViewModel.password.value,
                password ?: this@AuthenticationViewModel.password.value)
            if (response?.id?.isBlank() == true && response.apiToken.isBlank()) {
                // User added password to social account
                return
            }
            handleAuthResponse(response)
        } catch (e: Exception) {
            authenticationError()
            Analytics.logException(e)
        }
    }

    suspend fun retrieveUser(): User? {
        user.value = userRepository.retrieveUser(true, true)
        return user.value
    }

    suspend fun removeSocialAuth(network: String): Boolean {
        val success = apiClient.disconnectSocial(network)
        if (success) {
            retrieveUser()
        }
        return success
    }

    private fun authenticationError(error: AuthenticationErrors? = null) {
        viewModelScope.launch {
            _showAuthProgress.value = false
            _authenticationError.emit(error)
        }
    }

    private suspend fun handleAuthResponse(response: UserAuthResponse?) {
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
        retrieveUser()
        _authenticationSuccess.value = response.newUser
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

    fun startGoogleAuth(context: Context, allowRegister: Boolean = false) {
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
                    handleSignIn(context, result, allowRegister)
                } catch (e: GetCredentialException) {
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

    private var googleIdTokenCredential: GoogleIdTokenCredential? = null
    private var accessToken: String? = null

    private suspend fun handleSignIn(context: Context, result: GetCredentialResponse, allowRegister: Boolean) {
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
                            this.googleIdTokenCredential = googleIdTokenCredential
                            this.accessToken = result.accessToken
                            attemptSocialLogin(allowRegister)
                        } else {
                            authenticationError(AuthenticationErrors.MISSING_TOKEN)
                            Log.e("AuthenticationViewModel", "Received an empty access token response")
                        }
                    } catch (e: GoogleIdTokenParsingException) {
                        authenticationError(AuthenticationErrors.INVALID_CREDENTIALS)
                        Log.e("AuthenticationViewModel", "Received an invalid google id token response", e)
                    }
                } else {
                    authenticationError(AuthenticationErrors.INVALID_CREDENTIAL_TYPE)
                    Log.e("AuthenticationViewModel", "Unexpected type of credential: $credential")
                }
            }

            else -> {
                authenticationError(AuthenticationErrors.UNKNOWN_CREDENTIAL_TYPE)
                Log.e("AuthenticationViewModel", "Unexpected type of credential: $credential")
            }
        }
    }

    suspend fun attemptSocialLogin(allowRegister: Boolean) {
        val tokenId = googleIdTokenCredential?.id ?: return
        val token = accessToken ?: return
        val response = apiClient.connectSocial("google", tokenId, token, allowRegister)
        Log.d("AuthenticationViewModel", "Social auth response: $response")
        if (response?.userExists == false) {
            isRegistering.value = true
            _authenticationSuccess.value = true
        } else {
            handleAuthResponse(response)
        }
    }

    suspend fun updateUsername() {
        apiClient.updateUsername(username.value)
    }

    fun startedSocialAuth(): Boolean {
        return googleIdTokenCredential != null && accessToken != null
    }

    suspend fun completeRegistration() {
        if (startedSocialAuth()) {
            attemptSocialLogin(true)
            updateUsername()
        } else {
            register()
        }
    }

    fun prefillUsername() {
        val email = if (googleIdTokenCredential != null) {
            googleIdTokenCredential?.id ?: return
        } else email.value.ifBlank {
            ""
        }
        if (email.isNotBlank()) {
            var suggested = email.substringBefore('@').takeIf { it.isNotBlank() } ?: ""
            suggested = suggested.replace(Regex("[+\\s]"), "")
            username.value = suggested
        }
        checkUsername()
        if (_isUsernameValid.value == false) {
            username.value = ""
            _isUsernameValid.value = null
        }
    }
}
