package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.tasks.TaskDifficulty
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest :
    WordSpec({
        val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
        val userRepository = mockk<UserRepository>(relaxed = true)
        val mainUserViewModel = mockk<MainUserViewModel>()
        val user = User()

        every { mainUserViewModel.user } returns MutableLiveData(user)

        fun makeViewModel() = TaskFormViewModel(userRepository, mainUserViewModel)

        beforeEach { Dispatchers.setMain(testDispatcher) }
        afterEach { clearMocks(userRepository, answers = false) }

        "user" should {
            "come from the main user view model" {
                makeViewModel().user.value shouldBe user
            }
        }

        "updateUser" should {
            "delegate to the user repository" {
                makeViewModel().updateUser("preferences.sleep", true)
                coVerify(exactly = 1) { userRepository.updateUser("preferences.sleep", true) }
            }
        }

        "refreshUser" should {
            "force retrieving the user with tasks" {
                makeViewModel().refreshUser()
                coVerify(exactly = 1) { userRepository.retrieveUser(true, true) }
            }
        }

        "TaskFormViewModel" should {
            "start with the default task settings" {
                val viewModel = makeViewModel()
                viewModel.taskDifficulty.value shouldBe TaskDifficulty.EASY
                viewModel.habitScoringPositive.value shouldBe true
                viewModel.habitScoringNegative.value shouldBe false
            }
        }
    })
