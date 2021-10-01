package com.habitrpg.android.habitica.ui.fragments

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentStatsBinding
import com.habitrpg.android.habitica.models.user.Stats
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

class StatsScreen: Screen<StatsScreen>() {
    val strengthStatsView = KView { withId(R.id.strengthStatsView)}
    val strengthAllocateButton = KButton {
        withId(R.id.allocateButton)
        isDescendantOfA { withId(R.id.strengthStatsView) }
    }
    val intelligenceStatsView = KView { withId(R.id.intelligenceStatsView)}
    val intelligenceAllocateButton = KButton {
        withId(R.id.allocateButton)
        isDescendantOfA { withId(R.id.intelligenceStatsView) }
    }
    val constitutionStatsView = KView { withId(R.id.constitutionStatsView)}
    val constitutionAllocateButton = KButton {
        withId(R.id.allocateButton)
        isDescendantOfA { withId(R.id.constitutionStatsView) }
    }
    val perceptionStatsView = KView { withId(R.id.perceptionStatsView)}
    val perceptionAllocateButton = KButton {
        withId(R.id.allocateButton)
        isDescendantOfA { withId(R.id.perceptionStatsView) }
    }
    val bulkAllocateButton = KView { withId(R.id.statsAllocationButton) }
}

@LargeTest
@RunWith(AndroidJUnit4::class)
class StatsFragmentTest: FragmentTestCase<StatsFragment, FragmentStatsBinding, StatsScreen>() {


    override val screen = StatsScreen()

    override fun makeFragment() {
        scenario = launchFragmentInContainer(null, R.style.MainAppTheme) {
            fragment = spyk()
            fragment.shouldInitializeComponent = false
            fragment.userRepository = userRepository
            fragment.inventoryRepository = inventoryRepository
            fragment.tutorialRepository = tutorialRepository
            return@launchFragmentInContainer fragment
        }
    }

    @Before
    fun setUp() {
        user.stats?.lvl = 20
        user.stats?.points = 30
        userSubject.onNext(user)
    }

    @Test
    fun showStatsTest() {
        verify { userRepository.getUser() }
        screen {
            strengthStatsView.isVisible()
            intelligenceStatsView.isVisible()
            constitutionStatsView.isVisible()
            perceptionStatsView.isVisible()
        }
    }

    @Test
    fun showsAllocationButtons() {
        verify { userRepository.getUser() }
        screen {
            strengthAllocateButton.isVisible()
            intelligenceAllocateButton.isVisible()
            constitutionAllocateButton.isVisible()
            perceptionAllocateButton.isVisible()
            bulkAllocateButton.isVisible()
            bulkAllocateButton.isClickable()
        }
    }

    @Test
    fun allocatesOnClick() {
        screen {
            strengthAllocateButton.click()
            verify { userRepository.allocatePoint(Stats.STRENGTH) }
            intelligenceAllocateButton.click()
            verify { userRepository.allocatePoint(Stats.INTELLIGENCE) }
            constitutionAllocateButton.click()
            verify { userRepository.allocatePoint(Stats.CONSTITUTION) }
            perceptionAllocateButton.click()
            verify { userRepository.allocatePoint(Stats.PERCEPTION) }
        }
    }
}