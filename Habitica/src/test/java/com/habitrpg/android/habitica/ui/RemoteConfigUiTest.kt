package com.habitrpg.android.habitica.ui

import android.content.Intent
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.testing.launchFragmentInHiltContainer
import com.habitrpg.android.habitica.testing.withHiltActivity
import com.habitrpg.android.habitica.ui.adapter.NavigationDrawerAdapter
import com.habitrpg.android.habitica.ui.fragments.AboutFragment
import com.habitrpg.android.habitica.ui.fragments.NavigationDrawerFragment
import com.habitrpg.android.habitica.ui.fragments.support.BugFixFragment
import com.habitrpg.android.habitica.ui.fragments.support.SupportMainFragment
import com.habitrpg.android.habitica.ui.views.social.ChatBarView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import javax.inject.Inject

// Checks that screens read the remote config and follow later updates to it
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en-rUS")
class RemoteConfigUiTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var configManager: AppConfigManager

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun config(json: String) {
        configManager.updateConfig(Json.parseToJsonElement(json).jsonObject)
        idle()
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    @Test
    fun aboutShowsAnUpdateOnlyForNewerVersions() {
        launchFragmentInHiltContainer<AboutFragment> {
            idle()
            val wrapper = requireView().findViewById<View>(R.id.update_available_wrapper)
            wrapper.visibility shouldBe View.GONE

            config("""{"lastVersionCode":999999,"lastVersionNumber":"9.9"}""")
            wrapper.visibility shouldBe View.VISIBLE
            requireView().findViewById<TextView>(R.id.update_available_textview).text.toString() shouldContain "9.9 (999999)"

            config("""{"lastVersionCode":0}""")
            wrapper.visibility shouldBe View.GONE
        }.close()
    }

    @Test
    fun supportOpensTheFeedbackUrl() {
        config("""{"feedbackURL":"https://habitica.com/feedback"}""")
        launchFragmentInHiltContainer<SupportMainFragment> {
            requireView().findViewById<View>(R.id.suggestions_feedback_wrapper).performClick()
            val intent = shadowOf(requireActivity()).nextStartedActivity
            intent.action shouldBe Intent.ACTION_VIEW
            intent.data.toString() shouldBe "https://habitica.com/feedback"
        }.close()
    }

    @Test
    fun supportIgnoresABlankFeedbackUrl() {
        launchFragmentInHiltContainer<SupportMainFragment> {
            requireView().findViewById<View>(R.id.suggestions_feedback_wrapper).performClick()
            shadowOf(requireActivity()).nextStartedActivity.shouldBeNull()
        }.close()
    }

    @Test
    fun supportSurvivesAFeedbackUrlNoAppCanOpen() {
        config("""{"feedbackURL":"www.habitica.com"}""")
        shadowOf(ApplicationProvider.getApplicationContext<android.app.Application>()).checkActivities(true)
        launchFragmentInHiltContainer<SupportMainFragment> {
            requireView().findViewById<View>(R.id.suggestions_feedback_wrapper).performClick()
        }.close()
    }

    @Test
    fun bugFixListsTheKnownIssues() {
        launchFragmentInHiltContainer<BugFixFragment> {
            idle()
            val layout = requireView().findViewById<LinearLayout>(R.id.known_issues_layout)
            layout.childCount shouldBe 0

            config("""{"knownIssues":[{"title":"Sync","text":"a"},{"title":"Login","text":"b"}]}""")
            (0 until layout.childCount).map { (layout.getChildAt(it) as TextView).text.toString() } shouldBe listOf("Sync", "Login")

            config("""{"knownIssues":[{"title":"Login","text":"b"}]}""")
            layout.childCount shouldBe 1
        }.close()
    }

    @Test
    fun drawerFollowsHideChallenges() {
        launchFragmentInHiltContainer<NavigationDrawerFragment> {
            idle()
            val adapter = requireView().findViewById<RecyclerView>(R.id.recyclerView).adapter as NavigationDrawerAdapter

            fun challengesVisible() = adapter.getItemWithIdentifier(NavigationDrawerFragment.SIDEBAR_CHALLENGES)?.isVisible
            challengesVisible() shouldBe true

            config("""{"hideChallenges":true}""")
            challengesVisible() shouldBe false

            config("""{"hideChallenges":false}""")
            challengesVisible() shouldBe true
        }.close()
    }

    @Test
    fun drawerFollowsShowSubscriptionBanner() {
        launchFragmentInHiltContainer<NavigationDrawerFragment> {
            idle()
            val adapter = requireView().findViewById<RecyclerView>(R.id.recyclerView).adapter as NavigationDrawerAdapter

            fun promoVisible() = adapter.getItemWithIdentifier(NavigationDrawerFragment.SIDEBAR_SUBSCRIPTION_PROMO)?.isVisible
            promoVisible() shouldBe false

            config("""{"showSubscriptionBanner":true}""")
            promoVisible() shouldBe true
        }.close()
    }

    @Test
    fun chatBarBlocksMessagesOverTheMaxLength() =
        withHiltActivity { activity ->
            val bar = ChatBarView(activity, null)
            bar.maxChatLength = 5
            val input = bar.findViewById<EditText>(R.id.chatEditText)
            val send = bar.findViewById<View>(R.id.sendButton)

            input.setText("12345")
            send.isEnabled shouldBe true
            input.setText("123456")
            send.isEnabled shouldBe false
        }
}
