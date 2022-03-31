package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import androidx.fragment.app.testing.launchFragmentInContainer
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentRecyclerviewBinding
import com.habitrpg.android.habitica.ui.fragments.FragmentTestCase
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.mockk.every
import io.mockk.spyk
import io.reactivex.rxjava3.core.Flowable

class PetDetailScreen : Screen<PetDetailScreen>() {
    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.recyclerView)
    }, itemTypeBuilder = {
        itemType(::SectionItem)
        itemType(::PetItem)
    })
}

internal class PetDetailRecyclerFragmentTest : FragmentTestCase<PetDetailRecyclerFragment, FragmentRecyclerviewBinding, PetDetailScreen>(false) {
    override fun makeFragment() {
        every { inventoryRepository.getOwnedPets() } returns Flowable.just(user.items?.pets!!)
        fragment = spyk()
        fragment.shouldInitializeComponent = false
        fragment.arguments = PetDetailRecyclerFragmentArgs.Builder("cactus", "drop", "").build().toBundle()
    }

    override fun launchFragment() {
        scenario = launchFragmentInContainer(null, R.style.MainAppTheme) {
            return@launchFragmentInContainer fragment
        }
    }

    override val screen = PetDetailScreen()
}