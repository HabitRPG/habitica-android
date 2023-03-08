package com.habitrpg.android.habitica.ui.fragments.inventory.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ItemsFragment : BaseMainFragment<FragmentViewpagerBinding>() {

    override var binding: FragmentViewpagerBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentViewpagerBinding {
        return FragmentViewpagerBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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


    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager

        binding?.viewPager?.adapter = object : FragmentStateAdapter(fragmentManager, lifecycle) {

            override fun createFragment(position: Int): androidx.fragment.app.Fragment {
                val fragment = ItemRecyclerFragment()

                fragment.itemType = when (position) {
                    0 -> "eggs"
                    1 -> "hatchingPotions"
                    2 -> "food"
                    3 -> "quests"
                    4 -> "special"
                    else -> ""
                }
                fragment.itemTypeText =
                    if (position == 4 && isAdded) getString(R.string.special_items)
                    else getPageTitle(position)

                return fragment
            }

            override fun getItemCount(): Int {
                return 5
            }
        }
        tabLayout?.let {
            binding?.viewPager?.let { it1 ->
                TabLayoutMediator(it, it1) { tab, position ->
                    tab.text = getPageTitle(position)
                }.attach()
            }
        }
        tabLayout?.tabMode = TabLayout.MODE_SCROLLABLE
    }

    private fun getPageTitle(position: Int): String {
        return when (position) {
            0 -> mainActivity?.getString(R.string.eggs)
            1 -> mainActivity?.getString(R.string.hatching_potions)
            2 -> mainActivity?.getString(R.string.food)
            3 -> mainActivity?.getString(R.string.quests)
            4 -> mainActivity?.getString(R.string.special)
            else -> ""
        } ?: ""
    }
}
