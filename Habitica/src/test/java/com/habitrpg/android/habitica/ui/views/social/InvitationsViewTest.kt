package com.habitrpg.android.habitica.ui.views.social

import android.view.View
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.invitations.GenericInvitation
import com.habitrpg.android.habitica.testing.fakes.FakeNavigator
import com.habitrpg.android.habitica.testing.withHiltActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class InvitationsViewTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var navigator: FakeNavigator

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun invitation(
        id: String?,
        inviter: String? = "leader-$id",
    ) = object : GenericInvitation {
        override var id: String? = id
        override var name: String? = "Party $id"
        override var inviter: String? = inviter
    }

    private fun withInvitations(
        vararg invitations: GenericInvitation,
        block: (InvitationsView, accepted: List<String>, rejected: List<String>) -> Unit,
    ) {
        withHiltActivity { activity ->
            val accepted = mutableListOf<String>()
            val rejected = mutableListOf<String>()
            val view =
                InvitationsView(activity).apply {
                    acceptCall = { accepted += it }
                    rejectCall = { rejected += it }
                    setInvitations(invitations.toList())
                }
            block(view, accepted, rejected)
        }
    }

    private fun InvitationsView.row(index: Int): View = getChildAt(index)

    @Test
    fun showsARowForEachInvitation() =
        withInvitations(invitation("party-1"), invitation("party-2")) { view, _, _ ->
            view.childCount shouldBe 2
        }

    @Test
    fun replacesPreviousInvitations() =
        withInvitations(invitation("party-1"), invitation("party-2")) { view, _, _ ->
            view.setInvitations(listOf(invitation("party-3")))
            view.childCount shouldBe 1
        }

    @Test
    fun acceptsTheTappedInvitation() =
        withInvitations(invitation("party-1"), invitation("party-2")) { view, accepted, rejected ->
            view.row(1).findViewById<View>(R.id.accept_button).performClick()
            accepted shouldContainExactly listOf("party-2")
            rejected.shouldBeEmpty()
        }

    @Test
    fun rejectsTheTappedInvitation() =
        withInvitations(invitation("party-1"), invitation("party-2")) { view, accepted, rejected ->
            view.row(0).findViewById<View>(R.id.reject_button).performClick()
            rejected shouldContainExactly listOf("party-1")
            accepted.shouldBeEmpty()
        }

    @Test
    fun ignoresInvitationsWithoutId() =
        withInvitations(invitation(null)) { view, accepted, rejected ->
            view.row(0).findViewById<View>(R.id.accept_button).performClick()
            view.row(0).findViewById<View>(R.id.reject_button).performClick()
            accepted.shouldBeEmpty()
            rejected.shouldBeEmpty()
        }

    @Test
    fun opensTheProfileOfTheInviter() =
        withInvitations(invitation("party-1", inviter = "leader-id")) { view, _, _ ->
            view.row(0).performClick()
            val navigation = navigator.lastNavigation.shouldBeInstanceOf<FakeNavigator.Navigation.ToDirections>()
            navigation.directions.actionId shouldBe R.id.openProfileActivity
            navigation.directions.arguments.getString("userID") shouldBe "leader-id"
        }

    @Test
    fun doesNotNavigateWithoutInviter() =
        withInvitations(invitation("party-1", inviter = null)) { view, _, _ ->
            view.row(0).performClick()
            navigator.navigations.shouldBeEmpty()
        }
}
