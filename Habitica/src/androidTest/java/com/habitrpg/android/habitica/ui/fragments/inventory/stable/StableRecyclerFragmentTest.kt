package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.os.Bundle
import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentRecyclerviewBinding
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.ui.fragments.FragmentTestCase
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.Matcher
import org.junit.Test

class PetItem(parent: Matcher<View>) : KRecyclerItem<PetItem>(parent) {
    val title = KTextView(parent) { withId(R.id.titleTextView) }
    val owned = KTextView(parent) { withId(R.id.ownedTextView) }
}

class SectionItem(parent: Matcher<View>) : KRecyclerItem<PetItem>(parent) {
    val title = KTextView(parent) { withId(R.id.titleTextView) }
    val owned = KTextView(parent) { withId(R.id.ownedTextView) }
}

class StableScreen : Screen<StableScreen>() {
    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.recyclerView)
    }, itemTypeBuilder = {
            itemType(::SectionItem)
            itemType(::PetItem)
        })
}

internal class StableRecyclerFragmentTest : FragmentTestCase<StableRecyclerFragment, FragmentRecyclerviewBinding, StableScreen>(false) {
    override fun makeFragment() {
        every { inventoryRepository.getOwnedPets() } returns flowOf(user.items?.pets!!)
        every { inventoryRepository.getOwnedMounts() } returns flowOf(user.items?.mounts!!)
        fragment = spyk()
        fragment.shouldInitializeComponent = false
        fragment.itemType = "pets"
    }

    override fun launchFragment(args: Bundle?) {
        scenario = launchFragmentInContainer(args, R.style.MainAppTheme) {
            return@launchFragmentInContainer fragment
        }
    }

    override val screen = StableScreen()

    @Test
    fun displaysPets() {
        launchFragment()
        screen {
            recycler {
                isVisible()
                childWith<SectionItem> { withDescendant { withText("STANDARD PETS") } }.isVisible()
                childWith<PetItem> { withDescendant { withText("Bear") } }.isVisible()
                childWith<PetItem> { withDescendant { withText("Wolf") } }.isVisible()
                childWith<SectionItem> { withDescendant { withText("QUEST PETS") } }.isVisible()
                childWith<PetItem> { withDescendant { withText("Spider") } }.isVisible()
                childWith<SectionItem> { withDescendant { withText("SPECIAL PETS") } }.isVisible()
                childWith<SectionItem> { withDescendant { withText("WACKY PETS") } }.isVisible()
            }
        }
    }

    @Test
    fun displaysMounts() {
        fragment.itemType = "mounts"
        launchFragment()
        screen {
            recycler {
                isVisible()
                childWith<SectionItem> { withDescendant { withText("STANDARD MOUNTS") } }.isVisible()
                childWith<PetItem> { withDescendant { withText("Panda") } }.isVisible()
                childWith<PetItem> { withDescendant { withText("Dragon") } }.isVisible()
                childWith<SectionItem> { withDescendant { withText("QUEST MOUNTS") } }.isVisible()
                childWith<PetItem> { withDescendant { withText("Sloth") } }.isVisible()
                childWith<SectionItem> { withDescendant { withText("SPECIAL MOUNTS") } }.isVisible()
            }
        }
    }

    @Test
    fun displaysMenuForSpecialAndWacky() {
        launchFragment()
        screen {
            recycler {
                isVisible()

                childWith<PetItem> { withContentDescription("Phoenix") } perform {
                    isVisible()
                    click()
                }
                KView { withText("Phoenix") }.isVisible()
                KView { withText("Equip") }.isVisible()
                KView { withText("Feed") }.doesNotExist()
                this@screen.pressBack()

                childWith<PetItem> { withContentDescription("Confection Dragon") } perform {
                    isVisible()
                    click()
                }
                KView { withText("Confection Dragon") }.isVisible()
                KView { withText("Equip") }.isVisible()
                KView { withText("Feed") }.doesNotExist()
            }
        }
    }

    @Test fun displaysPetDetail() {
        launchFragment()
        screen {
            recycler {
                isVisible()
                childWith<PetItem> { withContentDescription("Lion 11 / 53") }.click()
                verify { MainNavigationController.navigate(StableFragmentDirections.openPetDetail("LionCub", "drop", null)) }
                childWith<PetItem> { withContentDescription("Falcon 1 / 10") }.click()
                verify { MainNavigationController.navigate(StableFragmentDirections.openPetDetail("Falcon", "quest", null)) }
                childWith<PetItem> { withContentDescription("Dolphin 0 / 10") }.click()
                verify { MainNavigationController.navigate(StableFragmentDirections.openPetDetail("Dolphin", "quest", null)) }
            }
        }
    }

    @Test fun displaysMountDetail() {
        fragment.itemTypeText = "mounts"
        launchFragment()
        screen {
            recycler {
                isVisible()
                childWith<PetItem> { withContentDescription("Fox 5 / 53") }.click()
                verify { MainNavigationController.navigate(StableFragmentDirections.openMountDetail("Fox", "drop", null)) }
                childWith<PetItem> { withContentDescription("Butterfly 1 / 10") }.click()
                verify { MainNavigationController.navigate(StableFragmentDirections.openMountDetail("Butterfly", "quest", null)) }
                childWith<PetItem> { withContentDescription("Frog 0 / 10") }.click()
                verify { MainNavigationController.navigate(StableFragmentDirections.openMountDetail("Frog", "quest", null)) }
            }
        }
    }
}
