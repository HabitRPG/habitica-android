package com.habitrpg.android.habitica.ui.fragments.inventory.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment

class ItemsFragment : BaseMainFragment<FragmentViewpagerBinding>() {

    override var binding: FragmentViewpagerBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentViewpagerBinding {
        return FragmentViewpagerBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.usesTabLayout = true
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.viewPager?.currentItem = 0
        setViewPagerAdapter()

        arguments?.let {
            val args = ItemsFragmentArgs.fromBundle(it)
            binding?.viewPager?.currentItem = when (args.itemType) {
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

        binding?.viewPager?.adapter = object : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            override fun getItem(position: Int): androidx.fragment.app.Fragment {
                val fragment = ItemRecyclerFragment()

                fragment.itemType = when (position) {
                    0 -> "eggs"
                    1 -> "hatchingPotions"
                    2 -> "food"
                    3 -> "quests"
                    4 -> "special"
                    else -> ""
                }
                fragment.isHatching = false
                fragment.isFeeding = false
                fragment.user = this@ItemsFragment.user
                fragment.itemTypeText =
                    if (position == 4) getString(R.string.special_items)
                    else this.getPageTitle(position).toString()

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
        tabLayout?.setupWithViewPager(binding?.viewPager)
        tabLayout?.tabMode = TabLayout.MODE_SCROLLABLE
    }
}
