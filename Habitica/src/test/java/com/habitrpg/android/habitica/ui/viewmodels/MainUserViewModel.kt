package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import app.cash.turbine.test
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.auth.LocalAuthentication
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.Authentication
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {}
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)

    afterObserve.invoke()

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        this.removeObserver(observer)
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}

/**
 * Observes a [LiveData] until the `block` is done executing.
 */
suspend fun <T> LiveData<T>.observeForTesting(block: suspend  () -> Unit) {
    val observer = Observer<T> { }
    try {
        observeForever(observer)
        block()
    } finally {
        removeObserver(observer)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainUserViewModelTest
    : WordSpec({
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    val authenticationHandler = mockk<AuthenticationHandler>()
    val userRepository = mockk<UserRepository>()
    val socialRepository = mockk<SocialRepository>()
    lateinit var viewModel: MainUserViewModel

    beforeEach {
        Dispatchers.setMain(testDispatcher)
    }

    afterEach {
        clearAllMocks()
    }

    "when user is null" should {
        beforeEach {
            every { userRepository.getUser() } returns flowOf(null)
            viewModel = MainUserViewModel(
                authenticationHandler,
                userRepository,
                socialRepository
            )
        }

        "formattedUsername should return empty string" {
            viewModel.formattedUsername shouldBe ""
        }

        "username should return empty string" {
            viewModel.username shouldBe ""
        }

        "isUserFainted should return false" {
            viewModel.isUserFainted shouldBe false
        }
    }

    "when user is not null" `when` {
        val user = User()
        beforeEach {
            every { userRepository.getUser() } returns flowOf(user)
            viewModel = MainUserViewModel(
                authenticationHandler,
                userRepository,
                socialRepository
            )
        }

        "formattedUsername" should {
            "return formatted username" {
                user.authentication = Authentication()
                user.authentication?.localAuthentication = LocalAuthentication()
                user.authentication?.localAuthentication?.username = "username"
                viewModel.user.getOrAwaitValue()
                viewModel.formattedUsername shouldBe "@username"
            }
        }

        "username" should {
            "return username" {
                user.authentication = Authentication()
                user.authentication?.localAuthentication = LocalAuthentication()
                user.authentication?.localAuthentication?.username = "username"
                viewModel.user.getOrAwaitValue()
                viewModel.username shouldBe "username"
            }
        }

        "isUserFainted" should {
            "return true if users health is 0" {
                user.stats = Stats()
                user.stats?.hp = 0.0
                viewModel.user.getOrAwaitValue()
                viewModel.isUserFainted shouldBe true
            }

            "return true if users health is less than 0" {
                user.stats = Stats()
                user.stats?.hp = -1.0
                viewModel.user.getOrAwaitValue()
                viewModel.isUserFainted shouldBe true
            }

            "return false if users health is above 0" {
                user.stats = Stats()
                user.stats?.hp = 1.0
                viewModel.user.getOrAwaitValue()
                viewModel.isUserFainted shouldBe false
            }
        }

        "currentTeamPlanGroup" should {
            "return group if currentTeamPlan is not null" {
                val teamPlan = TeamPlan()
                teamPlan.id = "123"
                val group = Group()
                every { socialRepository.getGroup(any()) } returns flowOf(group)
                viewModel.currentTeamPlan.emit(teamPlan)
                viewModel.currentTeamPlanGroup.first() shouldBe group
                verify(exactly = 1) { socialRepository.getGroup("123") }
            }

            "only calls getGroup once per plan id" {
                val teamPlan = TeamPlan()
                teamPlan.id = "123"
                val group = Group()
                every { socialRepository.getGroup(any()) } returns flowOf(group)
                viewModel.currentTeamPlan.emit(teamPlan)
                viewModel.currentTeamPlan.emit(teamPlan)
                viewModel.currentTeamPlanGroup.first() shouldBe group
                verify(exactly = 1) { socialRepository.getGroup("123") }
            }

            "calls getGroup again if currentTeamPlan changes" {
                val teamPlan = TeamPlan()
                teamPlan.id = "123"
                val group = Group()
                every { socialRepository.getGroup(any()) } returns flowOf(group)
                viewModel.currentTeamPlanGroup.test {
                    viewModel.currentTeamPlan.emit(teamPlan)
                    awaitItem() shouldBe group
                    val teamPlan2 = TeamPlan()
                    teamPlan2.id = "456"
                    viewModel.currentTeamPlan.emit(teamPlan2)
                    awaitItem()
                    verify(exactly = 1) { socialRepository.getGroup("123") }
                    verify(exactly = 1) { socialRepository.getGroup("456") }
                }
            }
        }

        "currentTeamPlanMembers" should {
            "return members if currentTeamPlan is not null" {
                val teamPlan = TeamPlan()
                teamPlan.id = "123"
                every { socialRepository.getGroupMembers(any()) } returns flowOf(emptyList())
                viewModel.currentTeamPlan.emit(teamPlan)
                viewModel.currentTeamPlanMembers.first() shouldBe emptyList()
                verify(exactly = 1) { socialRepository.getGroupMembers("123") }
            }
        }
    }
})
