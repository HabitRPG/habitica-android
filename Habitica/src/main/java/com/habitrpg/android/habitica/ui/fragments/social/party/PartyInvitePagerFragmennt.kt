package com.habitrpg.android.habitica.ui.fragments.social.party

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PartyInvitePagerFragment : BaseMainFragment<FragmentViewpagerBinding>() {

    override var binding : FragmentViewpagerBinding? = null

    override fun createBinding(
        inflater : LayoutInflater,
        container : ViewGroup?
    ) : FragmentViewpagerBinding {
        return FragmentViewpagerBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater : LayoutInflater,
        container : ViewGroup?,
        savedInstanceState : Bundle?
    ) : View? {
        this.usesTabLayout = true
        this.hidesToolbar = true
        showsBackButton = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewPagerAdapter()
        binding?.viewPager?.currentItem = 0
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager
        binding?.viewPager?.adapter = object : FragmentStateAdapter(fragmentManager, lifecycle) {
            override fun createFragment(position : Int) : Fragment {
                return when (position) {
                    0 -> {
                        PartySeekingFragment()
                    }

                    1 -> {
                        PartyInviteFragment()
                    }

                    else -> Fragment()
                }
            }

            override fun getItemCount() : Int {
                return 2
            }
        }
        tabLayout?.let {
            binding?.viewPager?.let { it1 ->
                TabLayoutMediator(it, it1) { tab, position ->
                    tab.text = when (position) {
                        0 -> context?.getString(R.string.list)
                        1 -> context?.getString(R.string.by_invite)
                        else -> ""
                    }
                }.attach()
            }
        }
    }
}
