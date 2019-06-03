package com.habitrpg.android.habitica.ui.fragments.inventory.items

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.events.commands.HatchingCommand
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews

import org.greenrobot.eventbus.Subscribe

class ItemsFragment : BaseMainFragment() {

    private val viewPager: androidx.viewpager.widget.ViewPager? by bindView(R.id.viewPager)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.usesTabLayout = true
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        viewPager?.currentItem = 0
        setViewPagerAdapter()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager

        viewPager?.adapter = object : FragmentPagerAdapter(fragmentManager) {

            override fun getItem(position: Int): androidx.fragment.app.Fragment {

                val fragment = ItemRecyclerFragment()

                when (position) {
                    0 -> {
                        fragment.itemType = "eggs"
                    }
                    1 -> {
                        fragment.itemType = "hatchingPotions"
                    }
                    2 -> {
                        fragment.itemType = "food"
                    }
                    3 -> {
                        fragment.itemType = "quests"
                    }
                    4 -> {
                        fragment.itemType = "special"
                    }
                }
                fragment.isHatching = false
                fragment.isFeeding = false
                fragment.itemTypeText = this.getPageTitle(position).toString()
                fragment.user = this@ItemsFragment.user

                return fragment
            }

            override fun getCount(): Int {
                return 5
            }

            override fun getPageTitle(position: Int): CharSequence {
                return when (position) {
                    0 -> activity?.getString(R.string.eggs)
                    1 -> activity?.getString(R.string.hatching_potions)
                    2 -> activity?.getString(R.string.food)
                    3 -> activity?.getString(R.string.quests)
                    4 -> activity?.getString(R.string.special)
                    else -> ""
                } ?: ""
            }
        }
        tabLayout?.setupWithViewPager(viewPager)
        tabLayout?.tabMode = TabLayout.MODE_SCROLLABLE
    }

    @Subscribe
    fun showHatchingDialog(event: HatchingCommand) {
        if (event.usingEgg == null || event.usingHatchingPotion == null) {
            val fragment = ItemRecyclerFragment()
            if (event.usingEgg != null) {
                fragment.itemType = "hatchingPotions"
                fragment.hatchingItem = event.usingEgg
            } else {
                fragment.itemType = "eggs"
                fragment.hatchingItem = event.usingHatchingPotion
            }
            fragment.isHatching = true
            fragment.isFeeding = false
            fragment.show(fragmentManager!!, "hatchingDialog")
        }
    }


}
