package com.habitrpg.android.habitica.ui.fragments.social.guilds

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.social.ChatFragment
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewModel
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GuildFragment : BaseMainFragment<FragmentViewpagerBinding>() {
    internal val viewModel: GroupViewModel by viewModels()
    private var guildInformationFragment: GuildDetailFragment? = null
    private var chatFragment: ChatFragment? = null

    override var binding: FragmentViewpagerBinding? = null

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
        showsBackButton = true
        super.onViewCreated(view, savedInstanceState)

        viewModel.groupViewType = GroupViewType.GUILD
        viewModel.getGroupData().observe(viewLifecycleOwner) { setGroup(it) }
        viewModel.getIsMemberData()
            .observe(viewLifecycleOwner) { mainActivity?.invalidateOptionsMenu() }

        arguments?.let {
            val args = GuildFragmentArgs.fromBundle(it)
            viewModel.setGroupID(args.groupID)
        }

        setViewPagerAdapter()
        setFragments()

        viewModel.retrieveGroup { }
    }

    override fun onResume() {
        super.onResume()

        arguments?.let {
            val args = GuildFragmentArgs.fromBundle(it)
            binding?.viewPager?.setCurrentItem(args.tabToOpen, false)
        }
    }

    private fun setFragments() {
        val fragments = childFragmentManager.fragments
        for (childFragment in fragments) {
            if (childFragment is ChatFragment) {
                chatFragment = childFragment
            }
            if (childFragment is GuildDetailFragment) {
                guildInformationFragment = childFragment
            }
        }
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        if (this.mainActivity != null) {
            if (viewModel.isMember) {
                if (viewModel.isLeader) {
                    this.mainActivity?.menuInflater?.inflate(R.menu.guild_admin, menu)
                } else {
                    this.mainActivity?.menuInflater?.inflate(R.menu.guild_member, menu)
                }
            } else {
                this.mainActivity?.menuInflater?.inflate(R.menu.guild_nonmember, menu)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Suppress("ReturnCount")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_guild_join -> {
                viewModel.joinGroup()
                return true
            }

            R.id.menu_guild_leave -> {
                guildInformationFragment?.leaveGuild()
                return true
            }

            R.id.menu_guild_edit -> {
                this.displayEditForm()
                return true
            }

            R.id.menu_guild_refresh -> {
                viewModel.retrieveGroupChat { }
                viewModel.retrieveGroup { }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager

        binding?.viewPager?.adapter =
            object : FragmentStateAdapter(fragmentManager, lifecycle) {
                override fun createFragment(position: Int): Fragment {
                    val fragment: Fragment?

                    when (position) {
                        0 -> {
                            guildInformationFragment = GuildDetailFragment.newInstance()
                            fragment = guildInformationFragment
                        }

                        1 -> {
                            chatFragment = ChatFragment()
                            fragment = chatFragment
                        }

                        else -> fragment = Fragment()
                    }

                    return fragment ?: Fragment()
                }

                override fun getItemCount(): Int {
                    return 2
                }
            }

        binding?.viewPager?.registerOnPageChangeCallback(
            object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    if (position == 1) {
                        chatFragment?.setNavigatedToFragment()
                    }
                }

                override fun onPageSelected(position: Int) {
                    if (position == 1) {
                        chatFragment?.setNavigatedToFragment()
                    }
                }
            }
        )

        tabLayout?.let {
            binding?.viewPager?.let { it1 ->
                TabLayoutMediator(it, it1) { tab, position ->
                    tab.text =
                        when (position) {
                            0 -> context?.getString(R.string.guild)
                            1 -> context?.getString(R.string.chat)
                            else -> ""
                        }
                }.attach()
            }
        }
    }

    private fun displayEditForm() {
        val bundle = Bundle()
        val guild = viewModel.getGroupData().value
        bundle.putString("groupID", guild?.id)
        bundle.putString("name", guild?.name)
        bundle.putString("description", guild?.description)
        bundle.putString("privacy", guild?.privacy)
        bundle.putString("leader", guild?.leaderID)
        bundle.putBoolean("leaderOnlyChallenges", guild?.leaderOnlyChallenges ?: true)

        val intent = Intent(mainActivity, GroupFormActivity::class.java)
        intent.putExtras(bundle)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        groupFormResult.launch(intent)
    }

    private val groupFormResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val bundle = it?.data?.extras
                viewModel.updateGroup(bundle)
            }
        }

    private fun setGroup(group: Group?) {
        this.mainActivity?.invalidateOptionsMenu()

        if (viewModel.isPublicGuild) {
            chatFragment?.autocompleteContext = "publicGuild"
        } else {
            chatFragment?.autocompleteContext = "privateGuild"
        }
    }
}
