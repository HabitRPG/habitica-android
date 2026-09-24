package com.habitrpg.android.habitica.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences
import app.cash.turbine.test
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.AuthenticationErrors
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.android.habitica.widget.glance.work.WidgetRefreshWorker
import com.habitrpg.common.habitica.api.HostConfig
import com.habitrpg.common.habitica.api.ServerSettings
import com.habitrpg.common.habitica.helpers.KeyHelper
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import com.habitrpg.shared.habitica.models.responses.VerifyEmailResponse
import com.habitrpg.shared.habitica.models.responses.VerifyUsernameResponse
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationViewModelTest :
    WordSpec({
        val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
        val apiClient = mockk<ApiClient>(relaxed = true)
        val userRepository = mockk<UserRepository>(relaxed = true)
        val sharedPreferences = mockk<SharedPreferences>(relaxed = true)
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        val authenticationHandler = mockk<AuthenticationHandler>(relaxed = true)
        val configManager = mockk<AppConfigManager>()
        val hostConfig = mockk<HostConfig>()
        val keyHelper = mockk<KeyHelper>()
        val context = mockk<Context>()

        every { sharedPreferences.edit() } returns editor

        fun makeViewModel(keyHelper: KeyHelper? = null) =
            AuthenticationViewModel(apiClient, userRepository, sharedPreferences, authenticationHandler, configManager, hostConfig, keyHelper, context)

        beforeSpec {
            mockkObject(Analytics, WidgetRefreshWorker)
            every { Analytics.logException(any()) } just runs
            every { WidgetRefreshWorker.enqueueOneTime(any()) } just runs
        }
        afterSpec { unmockkObject(Analytics, WidgetRefreshWorker) }

        beforeEach { Dispatchers.setMain(testDispatcher) }
        afterEach { clearMocks(apiClient, userRepository, editor, authenticationHandler, answers = false) }

        "checkUsername" should {
            "mark usable usernames as valid" {
                coEvery { apiClient.verifyUsername("tester") } returns VerifyUsernameResponse().apply { isUsable = true }
                val viewModel = makeViewModel()
                viewModel.username.value = "tester"
                viewModel.checkUsername()
                viewModel.isUsernameValid.first() shouldBe true
                viewModel.usernameIssues.first() shouldBe ""
            }

            "report the issues of invalid usernames" {
                coEvery { apiClient.verifyUsername("x") } returns
                    VerifyUsernameResponse().apply { issues = listOf("Too short", "Taken") }
                val viewModel = makeViewModel()
                viewModel.username.value = "x"
                viewModel.checkUsername()
                viewModel.isUsernameValid.first() shouldBe false
                viewModel.usernameIssues.first() shouldBe "Too short\nTaken"
            }

            "reset the state if the check failed" {
                coEvery { apiClient.verifyUsername(any()) } throws IllegalStateException()
                val viewModel = makeViewModel()
                viewModel.checkUsername()
                viewModel.isUsernameValid.first() shouldBe null
            }
        }

        "invalidateUsernameState" should {
            "clear the username validation" {
                coEvery { apiClient.verifyUsername(any()) } returns VerifyUsernameResponse().apply { isUsable = true }
                val viewModel = makeViewModel()
                viewModel.checkUsername()
                viewModel.invalidateUsernameState()
                viewModel.isUsernameValid.first() shouldBe null
                viewModel.usernameIssues.first() shouldBe null
            }
        }

        "prefillUsername" should {
            "suggest a username from the email address" {
                coEvery { apiClient.verifyUsername("johndoetag") } returns VerifyUsernameResponse().apply { isUsable = true }
                val viewModel = makeViewModel()
                viewModel.email.value = "john doe+tag@example.com"
                viewModel.prefillUsername()
                viewModel.username.value shouldBe "johndoetag"
                viewModel.isUsernameValid.first() shouldBe true
            }

            "clear the suggestion if it is not usable" {
                coEvery { apiClient.verifyUsername("taken") } returns VerifyUsernameResponse()
                val viewModel = makeViewModel()
                viewModel.email.value = "taken@example.com"
                viewModel.prefillUsername()
                viewModel.username.value shouldBe ""
                viewModel.isUsernameValid.first() shouldBe null
            }
        }

        "checkEmail" should {
            "report success for valid emails" {
                coEvery { apiClient.verifyEmail("a@b.c") } returns VerifyEmailResponse().apply { valid = true }
                val viewModel = makeViewModel()
                viewModel.email.value = "a@b.c"
                viewModel.checkEmail()
                viewModel.authenticationSuccess.first() shouldBe true
                viewModel.showAuthProgress.first() shouldBe false
            }

            "report an error for invalid emails" {
                coEvery { apiClient.verifyEmail("invalid") } returns VerifyEmailResponse().apply { error = "Invalid" }
                val viewModel = makeViewModel()
                viewModel.email.value = "invalid"
                viewModel.authenticationError.test {
                    viewModel.checkEmail()
                    val error = awaitItem()
                    error shouldBe AuthenticationErrors.INVALID_EMAIL
                    error.message shouldBe "Invalid"
                }
            }
        }

        "login" should {
            "save the tokens and retrieve the user on success" {
                val user = User()
                coEvery { apiClient.connectUser("a@b.c", "secret") } returns UserAuthResponse("token", "user-1")
                coEvery { userRepository.retrieveUser(true, true) } returns user
                val viewModel = makeViewModel()
                viewModel.email.value = "a@b.c"
                viewModel.password.value = "secret"
                viewModel.login()
                verify { apiClient.updateAuthenticationCredentials("user-1", "token") }
                verify { authenticationHandler.updateUserID("user-1") }
                verify { editor.putString("APIToken", "token") }
                verify { editor.putBoolean("pending_login_event", true) }
                viewModel.user.value shouldBe user
                viewModel.authenticationSuccess.first() shouldBe false
            }

            "store the encrypted token if encryption is available" {
                every { keyHelper.encrypt("token") } returns "encrypted-token"
                coEvery { apiClient.connectUser(any(), any()) } returns UserAuthResponse("token", "user-1")
                makeViewModel(keyHelper).login()
                verify { editor.putString("user-1", "encrypted-token") }
                verify(exactly = 0) { editor.putString("APIToken", any()) }
            }

            "stop the progress if login failed" {
                coEvery { apiClient.connectUser(any(), any()) } returns null
                val viewModel = makeViewModel()
                viewModel.login()
                viewModel.showAuthProgress.first() shouldBe false
                coVerify(exactly = 0) { userRepository.retrieveUser(any(), any()) }
            }

            "stop the progress if the request threw" {
                coEvery { apiClient.connectUser(any(), any()) } throws IllegalStateException()
                val viewModel = makeViewModel()
                viewModel.login()
                viewModel.showAuthProgress.first() shouldBe false
            }
        }

        "register" should {
            "register with the passed values and mark a pending registration" {
                coEvery { apiClient.registerUser("tester", "a@b.c", "secret", "secret") } returns UserAuthResponse("token", "user-1", newUser = true)
                val viewModel = makeViewModel()
                viewModel.register("tester", "a@b.c", "secret")
                viewModel.isRegistering.value shouldBe true
                viewModel.authenticationSuccess.first() shouldBe true
                verify { editor.putBoolean("pending_registration_event", true) }
            }

            "stop after adding a password to a social account" {
                coEvery { apiClient.registerUser(any(), any(), any(), any()) } returns UserAuthResponse()
                makeViewModel().register()
                coVerify(exactly = 0) { userRepository.retrieveUser(any(), any()) }
            }
        }

        "completeRegistration" should {
            "register with the entered values if no social auth was started" {
                val viewModel = makeViewModel()
                viewModel.username.value = "tester"
                viewModel.email.value = "a@b.c"
                viewModel.password.value = "secret"
                viewModel.startedSocialAuth() shouldBe false
                viewModel.completeRegistration()
                coVerify(exactly = 1) { apiClient.registerUser("tester", "a@b.c", "secret", "secret") }
            }
        }

        "removeSocialAuth" should {
            "refresh the user if disconnecting worked" {
                coEvery { apiClient.disconnectSocial("google") } returns true
                makeViewModel().removeSocialAuth("google") shouldBe true
                coVerify(exactly = 1) { userRepository.retrieveUser(true, true) }
            }

            "not refresh the user if disconnecting failed" {
                coEvery { apiClient.disconnectSocial("google") } returns false
                makeViewModel().removeSocialAuth("google") shouldBe false
                coVerify(exactly = 0) { userRepository.retrieveUser(any(), any()) }
            }
        }

        "clearAuthenticationState" should {
            "reset progress and success" {
                coEvery { apiClient.verifyEmail(any()) } returns VerifyEmailResponse().apply { valid = true }
                val viewModel = makeViewModel()
                viewModel.checkEmail()
                viewModel.clearAuthenticationState()
                viewModel.authenticationSuccess.first() shouldBe null
                viewModel.showAuthProgress.first() shouldBe false
            }
        }

        "server settings" should {
            "reveal and show the current settings" {
                every { sharedPreferences.getString("server_url", null) } returns "https://custom"
                val viewModel = makeViewModel()
                viewModel.customServerUrl.value shouldBe "https://custom"
                viewModel.onServerSettingsUnlocked()
                viewModel.serverSettingsRevealed.value shouldBe true
                viewModel.showServerSettingsDialog.first() shouldBe ServerSettings(BuildConfig.BASE_URL, "https://custom")
            }

            "store a changed server url" {
                val viewModel = makeViewModel()
                viewModel.showServerSettings()
                viewModel.onServerSettingsChanged("https://new")
                verify { editor.putString("server_url", "https://new") }
                verify { apiClient.updateServerUrl("https://new") }
                viewModel.customServerUrl.value shouldBe "https://new"
                viewModel.showServerSettingsDialog.first() shouldBe null
            }

            "reset to the base url" {
                val viewModel = makeViewModel()
                viewModel.onServerSettingsReset("https://base")
                verify { editor.remove("server_url") }
                verify { apiClient.updateServerUrl("https://base") }
                viewModel.customServerUrl.value shouldBe null
            }
        }
    })
