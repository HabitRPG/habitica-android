package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentRecyclerviewBinding
import com.habitrpg.android.habitica.interactors.FeedPetUseCase
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.ui.fragments.FragmentTestCase
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemDialogFragment
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.rxjava3.core.Flowable
import org.junit.Test

class PetDetailScreen : Screen<PetDetailScreen>() {
    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.recyclerView)
    }, itemTypeBuilder = {
            itemType(::SectionItem)
            itemType(::PetItem)
        })
}

internal class PetDetailRecyclerFragmentTest :
    FragmentTestCase<PetDetailRecyclerFragment, FragmentRecyclerviewBinding, PetDetailScreen>(false) {
    override fun makeFragment() {
        every { inventoryRepository.getOwnedPets() } returns Flowable.just(user.items?.pets!!)
        every { inventoryRepository.getOwnedMounts() } returns Flowable.just(user.items?.mounts!!)
        every { inventoryRepository.getOwnedItems("food") } returns Flowable.just(user.items?.food!!.filter { it.numberOwned > 0 })
        val saddle = OwnedItem()
        saddle.numberOwned = 1
        every { inventoryRepository.getOwnedItems(true) } returns Flowable.just(
            mapOf(
                Pair(
                    "Saddle-food",
                    saddle
                )
            )
        )

        fragment = spyk()
        fragment.shouldInitializeComponent = false

        val mockComponent: UserComponent = mockk(relaxed = true)
        every { mockComponent.inject(any<ItemDialogFragment>()) } answers { initializeInjects(this.args.first()) }
        mockkObject(HabiticaBaseApplication)
        every { HabiticaBaseApplication.userComponent } returns mockComponent
    }

    override fun launchFragment(args: Bundle?) {
        scenario = launchFragmentInContainer(args, R.style.MainAppTheme) {
            return@launchFragmentInContainer fragment
        }
    }

    override val screen = PetDetailScreen()

    @Test
    fun canFeedPet() {
        val slot = CapturingSlot<FeedPetUseCase.RequestValues>()
        every { feedPetUseCase.callInteractor(capture(slot)) } returns mockk(relaxed = true)
        every {
            inventoryRepository.getPets(
                any(),
                any(),
                any()
            )
        } returns Flowable.just(content.pets.filter { it.animal == "Cactus" })
        every {
            inventoryRepository.getMounts(
                any(),
                any(),
                any()
            )
        } returns Flowable.just(content.mounts.filter { it.animal == "Cactus" })
        launchFragment(
            PetDetailRecyclerFragmentArgs.Builder("cactus", "drop", "").build().toBundle()
        )
        screen {
            recycler {
                childWith<PetItem> { withContentDescription("Skeleton Cactus") }.click()
                KView { withText(R.string.feed) }.click()
                KView { withText("Meat") }.click()
                verify { feedPetUseCase.callInteractor(any()) }
                slot.captured.pet.key shouldBe "Cactus-Skeleton"
                slot.captured.food.key shouldBe "Meat"
            }
        }
    }

    @Test
    fun canUseSaddle() {
        val slot = CapturingSlot<FeedPetUseCase.RequestValues>()
        every { feedPetUseCase.callInteractor(capture(slot)) } returns mockk(relaxed = true)
        every {
            inventoryRepository.getPets(
                any(),
                any(),
                any()
            )
        } returns Flowable.just(content.pets.filter { it.animal == "Fox" })
        every {
            inventoryRepository.getMounts(
                any(),
                any(),
                any()
            )
        } returns Flowable.just(content.mounts.filter { it.animal == "Fox" })
        launchFragment(PetDetailRecyclerFragmentArgs.Builder("fox", "drop", "").build().toBundle())
        screen {
            recycler {
                childWith<PetItem> { withContentDescription("Shade Fox") }.click()
                KView { withText(R.string.use_saddle) }.click()
                verify { feedPetUseCase.callInteractor(any()) }
                slot.captured.pet.key shouldBe "Fox-Shade"
                slot.captured.food.key shouldBe "Saddle"
            }
        }
    }
}
