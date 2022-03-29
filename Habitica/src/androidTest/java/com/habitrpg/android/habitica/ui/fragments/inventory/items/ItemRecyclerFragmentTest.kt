package com.habitrpg.android.habitica.ui.fragments.inventory.items

import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentRecyclerviewBinding
import com.habitrpg.android.habitica.ui.fragments.FragmentTestCase
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView
import io.mockk.every
import io.mockk.spyk
import io.reactivex.rxjava3.core.Flowable
import org.hamcrest.Matcher
import org.junit.Test

class ItemItem(parent: Matcher<View>) : KRecyclerItem<ItemItem>(parent) {
    val title = KTextView(parent) { withId(R.id.titleTextView) }
    val owned = KTextView(parent) { withId(R.id.ownedTextView) }
}

class ItemScreen : Screen<ItemScreen>() {
    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.recyclerView)
    }, itemTypeBuilder = {
        itemType(::ItemItem)
    })
}

internal class ItemRecyclerFragmentTest : FragmentTestCase<ItemRecyclerFragment, FragmentRecyclerviewBinding, ItemScreen>() {
    override fun makeFragment() {
        every { inventoryRepository.getOwnedItems("eggs") } answers {
            Flowable.just(user.items?.eggs!!.filter { it.numberOwned > 0 })
        }
        fragment = spyk()
        fragment.shouldInitializeComponent = false
        fragment.itemType = "eggs"
    }

    override fun launchFragment() {
        scenario = launchFragmentInContainer(null, R.style.MainAppTheme) {
            return@launchFragmentInContainer fragment
        }
    }

    override val screen = ItemScreen()

    @Test
    fun hasOnlyOwnedItems() {
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
}