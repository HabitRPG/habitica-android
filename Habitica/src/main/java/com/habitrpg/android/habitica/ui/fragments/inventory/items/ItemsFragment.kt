package com.habitrpg.android.habitica.ui.fragments.inventory.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews

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

        arguments?.let {
            val args = ItemsFragmentArgs.fromBundle(it)
                viewPager?.currentItem = when (args.itemType) {
                    "hatchingPotions" -> 1
                    "food" -> 2
                    "quests" -> 3
                    "special" -> 4
                    else -> 0
                }
        }
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
}
