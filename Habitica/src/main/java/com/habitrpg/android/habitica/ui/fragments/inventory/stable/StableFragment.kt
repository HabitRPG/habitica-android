package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewmodels.StableViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StableFragment : BaseMainFragment<FragmentViewpagerBinding>() {
    override var binding: FragmentViewpagerBinding? = null

    private val viewModel: StableViewModel by viewModels()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentViewpagerBinding {
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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPager?.currentItem = 0

        setViewPagerAdapter()
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager

        binding?.viewPager?.adapter =
            object : FragmentStateAdapter(fragmentManager, lifecycle) {
                override fun createFragment(position: Int): androidx.fragment.app.Fragment {
                    val fragment = StableRecyclerFragment()
                    fragment.arguments =
                        bundleOf(StableRecyclerFragment.ITEM_TYPE_KEY to if (position == 0) "pets" else "mounts")
                    fragment.itemTypeText = getPageTitle(position)

                    return fragment
                }

                override fun getItemCount(): Int {
                    return 2
                }
            }
        tabLayout?.let {
            binding?.viewPager?.let { it1 ->
                TabLayoutMediator(it, it1) { tab, position ->
                    tab.text = getPageTitle(position)
                }.attach()
            }
        }
    }

    private fun getPageTitle(position: Int): String {
        return when (position) {
            0 -> mainActivity?.getString(R.string.pets)
            1 -> mainActivity?.getString(R.string.mounts)
            else -> ""
        } ?: ""
    }
}
