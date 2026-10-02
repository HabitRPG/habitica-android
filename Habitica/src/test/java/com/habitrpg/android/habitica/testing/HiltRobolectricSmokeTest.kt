package com.habitrpg.android.habitica.testing

import androidx.test.core.app.ApplicationProvider
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.modules.navigator
import com.habitrpg.android.habitica.testing.fakes.FakeNavigator
import com.habitrpg.android.habitica.testing.fakes.FakeRemoteConfig
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.isMockKMock
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class HiltRobolectricSmokeTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var navigator: FakeNavigator

    @Inject
    lateinit var remoteConfig: FakeRemoteConfig

    @Inject
    lateinit var configManager: AppConfigManager

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun providesMockedRepositories() {
        isMockKMock(userRepository) shouldBe true
        every { userRepository.getUser() } returns flowOf(null)
    }

    @Test
    fun resolvesTheFakeNavigatorForViews() {
        ApplicationProvider.getApplicationContext<android.app.Application>().navigator shouldBeSameInstanceAs navigator
    }

    @Test
    fun readsRemoteConfigFromTheFake() {
        remoteConfig.values["enableLocalTaskScoring"] = true
        configManager.enableLocalTaskScoring() shouldBe true
    }

    @Test
    fun launchesTheHostActivity() {
        withHiltActivity { it.shouldNotBeNull() }
    }
}
