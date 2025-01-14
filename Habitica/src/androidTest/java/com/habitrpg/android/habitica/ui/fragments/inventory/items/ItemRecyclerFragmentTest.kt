package com.habitrpg.android.habitica.ui.fragments.inventory.items

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentRecyclerviewBinding
import com.habitrpg.android.habitica.interactors.HatchPetUseCase
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.ui.fragments.FragmentTestCase
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.Matcher
import org.hamcrest.Matchers.isA
import org.junit.Test

private val KTextView.text: CharSequence?
    get() {
        var string: CharSequence? = null
        (
            this.view.perform(
                object : ViewAction {
                    override fun getConstraints(): Matcher<View> {
                        return isA(TextView::class.java)
                    }

                    override fun getDescription(): String {
                        return "getting text from a TextView"
                    }

                    override fun perform(
                        uiController: UiController?,
                        view: View?
                    ) {
                        val tv = view as TextView
                        string = tv.text
                    }
                }
            )
            )
        return string
    }

class ItemItem(parent: Matcher<View>) : KRecyclerItem<ItemItem>(parent) {
    val title = KTextView(parent) { withId(R.id.titleTextView) }
    val owned = KTextView(parent) { withId(R.id.ownedTextView) }
}

class ItemScreen : Screen<ItemScreen>() {
    val recycler: KRecyclerView =
        KRecyclerView({
            withId(R.id.recyclerView)
        }, itemTypeBuilder = {
                itemType(::ItemItem)
            })
}

internal class ItemRecyclerFragmentTest : FragmentTestCase<ItemRecyclerFragment, FragmentRecyclerviewBinding, ItemScreen>(false) {
    override fun makeFragment() {
        every { inventoryRepository.getOwnedItems("eggs") } answers {
            flowOf(user.items?.eggs!!.filter { it.numberOwned > 0 })
        }
        every { inventoryRepository.getOwnedItems("hatchingPotions") } answers {
            flowOf(user.items?.hatchingPotions!!.filter { it.numberOwned > 0 })
        }
        fragment = spyk()
        fragment.shouldInitializeComponent = false
    }

    override fun launchFragment(args: Bundle?) {
        scenario =
            launchFragmentInContainer(args, R.style.MainAppTheme) {
                return@launchFragmentInContainer fragment
            }
    }

    override val screen = ItemScreen()

    @Test
    fun hasOnlyOwnedItems() {
        fragment.itemType = "eggs"
        launchFragment()
        screen {
            recycler {
                isVisible()
                children<ItemItem> {
                    isVisible()
                    owned.hasNoText("0")
                }
            }
        }
    }

    @Test
    fun doesNotHaveDuplicates() {
        clearMocks(inventoryRepository)
        every { inventoryRepository.getOwnedItems("food") } answers {
            var items = user.items?.food!!.filter { it.numberOwned > 0 }
            items = (items + items).sortedBy { it.key }
            flowOf(items)
        }
        every { inventoryRepository.getItems(Food::class.java, any()) } answers {
            flowOf((content.eggs + content.eggs).sortedBy { it.key })
        }
        fragment.itemType = "food"
        val foundItems = mutableListOf<CharSequence?>()
        launchFragment()
        screen {
            recycler {
                children<ItemItem> {
                    foundItems.add(title.text)
                }
            }
        }
        assert(foundItems.distinct().size == foundItems.size)
    }

    @Test
    fun canHatchPetWithEggs() {
        val slot = CapturingSlot<HatchPetUseCase.RequestValues>()
        coEvery { hatchPetUseCase.callInteractor(capture(slot)) } returns mockk(relaxed = true)
        fragment.itemType = "eggs"
        launchFragment()
        screen {
            recycler {
                childWith<ItemItem> { withDescendant { withText("Wolf") } }.click()
                KView { withText(R.string.hatch_with_potion) }.click()
                KView { withText("Shade") }.click()
                coVerify { hatchPetUseCase.callInteractor(any()) }
                slot.captured.egg.key shouldBe "Wolf"
                slot.captured.potion.key shouldBe "Shade"
            }
        }
    }

    @Test
    fun canHatchPetWithPotions() {
        val slot = CapturingSlot<HatchPetUseCase.RequestValues>()
        coEvery { hatchPetUseCase.callInteractor(capture(slot)) } returns mockk(relaxed = true)
        fragment.itemType = "hatchingPotions"
        launchFragment()
        screen {
            recycler {
                childWith<ItemItem> { withDescendant { withText("Shade") } }.click()
                KView { withText(R.string.hatch_egg) }.click()
                KView { withText("Wolf") }.click()
                coVerify { hatchPetUseCase.callInteractor(any()) }
                slot.captured.egg.key shouldBe "Wolf"
                slot.captured.potion.key shouldBe "Shade"
            }
        }
    }

    @Test
    fun canSellItems() {
        val slot = CapturingSlot<OwnedItem>()
        coEvery { inventoryRepository.sellItem(capture(slot)) } returns mockk(relaxed = true)
        fragment.itemType = "eggs"
        launchFragment()
        screen {
            recycler {
                childWith<ItemItem> { withDescendant { withText("Cactus") } }.click()
                KView { withText("Sell (3 Gold)") }.click()
                coVerify { inventoryRepository.sellItem(any()) }
                slot.captured.key shouldBe "Cactus"

                childWith<ItemItem> { withDescendant { withText("Panda Cub") } }.click()
                KView { withText("Sell (3 Gold)") }.click()
                coVerify { inventoryRepository.sellItem(any()) }
                slot.captured.key shouldBe "PandaCub"
            }
        }
    }
}
