package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentPartyDetailBinding
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyDetailFragment
import com.habitrpg.android.habitica.ui.viewmodels.PartyViewModel
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.junit.runner.RunWith

class PartyDetailScreen : Screen<PartyDetailScreen>() {
    val titleView = KTextView { withId(R.id.title_view) }
    val newQuestButton = KButton { withId(R.id.new_quest_button) }
    val questDetailButton = KButton { withId(R.id.quest_detail_button) }
    val questImageWrtapper = KImageView { withId(R.id.quest_image_view) }
    val questProgressView = KView { withId(R.id.quest_progress_view) }
}

@LargeTest
@RunWith(AndroidJUnit4::class)
class PartyDetailFragmentTest : FragmentTestCase<PartyDetailFragment, FragmentPartyDetailBinding, PartyDetailScreen>() {
    private lateinit var viewModel: PartyViewModel
    override val screen = PartyDetailScreen()

    override fun makeFragment() {
        val group = Group()
        group.name = "Group Name"
        every { socialRepository.getGroup(any()) } returns flowOf(group)
        viewModel = PartyViewModel(false)
        viewModel.socialRepository = socialRepository
        viewModel.userRepository = userRepository
        viewModel.userViewModel = userViewModel
        viewModel.notificationsManager = mockk(relaxed = true)
        fragment = spyk()
        fragment.shouldInitializeComponent = false
        fragment.viewModel = viewModel
    }

    override fun launchFragment(args: Bundle?) {
        scenario = launchFragmentInContainer(args, R.style.MainAppTheme) {
            return@launchFragmentInContainer fragment
        }
    }

    @Test
    fun displaysParty() {
        viewModel.setGroupID("")
        screen {
            titleView.hasText("Group Name")
        }
    }
}
