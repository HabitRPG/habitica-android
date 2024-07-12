package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentRecyclerviewBinding
import com.habitrpg.android.habitica.interactors.FeedPetUseCase
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.ui.fragments.FragmentTestCase
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class PetDetailScreen : Screen<PetDetailScreen>() {
    val recycler: KRecyclerView =
        KRecyclerView({
            withId(R.id.recyclerView)
        }, itemTypeBuilder = {
            itemType(::SectionItem)
            itemType(::PetItem)
        })
}

internal class PetDetailRecyclerFragmentTest :
    FragmentTestCase<PetDetailRecyclerFragment, FragmentRecyclerviewBinding, PetDetailScreen>(false) {
    override fun makeFragment() {
        every { inventoryRepository.getOwnedPets() } returns flowOf(user.items?.pets!!)
        every { inventoryRepository.getOwnedMounts() } returns flowOf(user.items?.mounts!!)
        every { inventoryRepository.getOwnedItems("food") } returns flowOf(user.items?.food!!.filter { it.numberOwned > 0 })
        val saddle = OwnedItem()
        saddle.numberOwned = 1
        every { inventoryRepository.getOwnedItems(true) } returns
            flowOf(
                mapOf(
                    Pair(
                        "Saddle-food",
                        saddle,
                    ),
                ),
            )

        fragment = spyk()
        fragment.shouldInitializeComponent = false
    }

    override fun launchFragment(args: Bundle?) {
        scenario =
            launchFragmentInContainer(args, R.style.MainAppTheme) {
                return@launchFragmentInContainer fragment
            }
    }

    override val screen = PetDetailScreen()

    @Test
    fun canFeedPet() {
        val slot = CapturingSlot<FeedPetUseCase.RequestValues>()
        coEvery { feedPetUseCase.callInterActor(capture(slot)) } returns mockk(relaxed = true)
        every {
            inventoryRepository.getPets(
                any(),
                any(),
                any(),
            )
        } returns flowOf(content.pets.filter { it.animal == "Cactus" })
        every {
            inventoryRepository.getMounts(
                any(),
                any(),
                any(),
            )
        } returns flowOf(content.mounts.filter { it.animal == "Cactus" })
        launchFragment(
            PetDetailRecyclerFragmentArgs.Builder("cactus", "drop", "").build().toBundle(),
        )
        screen {
            recycler {
                childWith<PetItem> { withContentDescription("Skeleton Cactus") }.click()
                KView { withText(R.string.feed) }.click()
                KView { withText("Meat") }.click()
                coVerify { feedPetUseCase.callInterActor(any()) }
                slot.captured.pet.key shouldBe "Cactus-Skeleton"
                slot.captured.food.key shouldBe "Meat"
            }
        }
    }

    @Test
    fun canUseSaddle() {
        val slot = CapturingSlot<FeedPetUseCase.RequestValues>()
        coEvery { feedPetUseCase.callInterActor(capture(slot)) } returns mockk(relaxed = true)
        every {
            inventoryRepository.getPets(
                any(),
                any(),
                any(),
            )
        } returns flowOf(content.pets.filter { it.animal == "Fox" })
        every {
            inventoryRepository.getMounts(
                any(),
                any(),
                any(),
            )
        } returns flowOf(content.mounts.filter { it.animal == "Fox" })
        launchFragment(PetDetailRecyclerFragmentArgs.Builder("fox", "drop", "").build().toBundle())
        screen {
            recycler {
                childWith<PetItem> { withContentDescription("Shade Fox") }.click()
                KView { withText(R.string.use_saddle) }.click()
                coVerify { feedPetUseCase.callInterActor(any()) }
                slot.captured.pet.key shouldBe "Fox-Shade"
                slot.captured.food.key shouldBe "Saddle"
            }
        }
    }
}
