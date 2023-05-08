package com.habitrpg.android.habitica.ui.fragments.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewModel
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TavernFragment : BaseMainFragment<FragmentViewpagerBinding>() {

    @Inject
    lateinit var socialRepository: SocialRepository

    override var binding: FragmentViewpagerBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentViewpagerBinding {
        return FragmentViewpagerBinding.inflate(inflater, container, false)
    }

    internal val viewModel: GroupViewModel by viewModels()

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

        viewModel.groupViewType = GroupViewType.GUILD
        viewModel.setGroupID(Group.TAVERN_ID)

        setViewPagerAdapter()
        binding?.viewPager?.currentItem = 0
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tavern, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Suppress("ReturnCount")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_guild_refresh -> {
                viewModel.retrieveGroup { }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager
        binding?.viewPager?.adapter = object : FragmentStateAdapter(fragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> {
                        TavernDetailFragment()
                    }
                    1 -> {
                        ChatFragment()
                    }
                    else -> Fragment()
                }
            }

            override fun getItemCount(): Int {
                return if (viewModel.getGroupData().value?.quest?.active == true) {
                    3
                } else 2
            }
        }
        tabLayout?.let {
            binding?.viewPager?.let { it1 ->
                TabLayoutMediator(it, it1) { tab, position ->
                    tab.text = when (position) {
                        0 -> context?.getString(R.string.inn)
                        1 -> context?.getString(R.string.chat)
                        2 -> context?.getString(R.string.world_quest)
                        else -> ""
                    }
                }.attach()
            }
        }
    }
}
