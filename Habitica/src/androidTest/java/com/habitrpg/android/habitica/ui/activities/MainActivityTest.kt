package com.habitrpg.android.habitica.ui.activities

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.ui.fragments.NavigationDrawerFragment
import com.habitrpg.android.habitica.ui.fragments.tasks.TaskRecyclerViewFragment
import com.habitrpg.android.habitica.ui.fragments.tasks.TasksFragment
import com.habitrpg.android.habitica.ui.viewmodels.MainActivityViewModel
import com.habitrpg.android.habitica.ui.viewmodels.NotificationsViewModel
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.toolbar.KToolbar
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

class MainActivityScreen : Screen<MainActivityScreen>() {
    val toolbar = KToolbar { withId(R.id.toolbar) }
}

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest : ActivityTestCase() {

    val screen = MainActivityScreen()

    lateinit var scenario: ActivityScenario<MainActivity>
    @After
    fun cleanup() {
        scenario.close()
    }

    @Before
    fun setup() {
        every { hostConfig.hasAuthentication() } returns true
        val mockComponent: UserComponent = mockk(relaxed = true)
        every { mockComponent.inject(any<MainActivity>()) } answers { initializeInjects(this.args.first()) }
        every { mockComponent.inject(any<MainActivityViewModel>()) } answers { initializeInjects(this.args.first()) }
        every { mockComponent.inject(any<NotificationsViewModel>()) } answers { initializeInjects(this.args.first()) }
        every { mockComponent.inject(any<TasksFragment>()) } answers { initializeInjects(this.args.first()) }
        every { mockComponent.inject(any<TaskRecyclerViewFragment>()) } answers { initializeInjects(this.args.first()) }
        every { mockComponent.inject(any<NavigationDrawerFragment>()) } answers { initializeInjects(this.args.first()) }
        every { mockComponent.inject(any<TaskFormActivity>()) } answers { initializeInjects(this.args.first()) }
        mockkObject(HabiticaBaseApplication)
        every { HabiticaBaseApplication.userComponent } returns mockComponent
    }

    @Test
    fun showsCreateScreens() {
        every { sharedPreferences.getString("FirstDayOfTheWeek", any()) } returns "-1"
        scenario = launchActivity()
        screen {
            device.activities.isCurrent(MainActivity::class.java)
            KView { withId(R.id.add_button) }.click()
            KView { withText("Create Habit") }.isVisible()
            screen.pressBack()
            screen.pressBack()
            KButton { withText("Discard") }.click()
            KView { withId(R.id.dailies_tab)}.click()
            KView { withId(R.id.add_button) }.click()
            KView { withText("Create Daily") }.isVisible()
            screen.pressBack()
            screen.pressBack()
            KView { withId(R.id.todos_tab)}.click()
            KView { withId(R.id.add_button) }.click()
            KView { withText("Create To Do") }.isVisible()
        }
    }

    @Test
    fun showsFilterScreen() {
        scenario = launchActivity()
        screen {
            toolbar {
                KView { withId(R.id.action_filter) }.click()
                KView { withText(R.string.filters) }.isVisible()
                screen.pressBack()
                KView { withText(R.string.filters)}.doesNotExist()
            }
        }
    }
}
