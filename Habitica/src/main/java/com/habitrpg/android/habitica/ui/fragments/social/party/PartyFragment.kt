package com.habitrpg.android.habitica.ui.fragments.social.party

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
import com.google.android.material.tabs.TabLayoutMediator
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.social.PartyChatFragment
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewType
import com.habitrpg.android.habitica.ui.viewmodels.PartyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PartyFragment : BaseMainFragment<FragmentViewpagerBinding>() {

    private var detailFragment: PartyDetailFragment? = null
    private var viewPagerAdapter: FragmentStateAdapter? = null

    internal val viewModel: PartyViewModel by viewModels()

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

        viewModel.groupViewType = GroupViewType.PARTY

        viewModel.getGroupData().observe(
            viewLifecycleOwner
        ) {
            updateGroupUI(it)
        }

        binding?.viewPager?.currentItem = 0

        setViewPagerAdapter()

        arguments?.let {
            val args = PartyFragmentArgs.fromBundle(it)
            binding?.viewPager?.post {
                binding?.viewPager?.currentItem = args.tabToOpen
            }
            if (args.partyID?.isNotBlank() == true) {
                viewModel.setGroupID(args.partyID ?: "")
            }
        }

        viewModel.loadPartyID()

        viewModel.retrieveGroup {}
    }

    override fun onResume() {
        if (viewModel.isLeader) {
            this.tutorialStepIdentifier = "party"
            this.tutorialTexts = listOf(getString(R.string.tutorial_party_created))
        }
        super.onResume()
    }

    private fun updateGroupUI(group: Group?) {
        viewPagerAdapter?.notifyDataSetChanged()

        if (group == null) {
            tabLayout?.visibility = View.GONE
            return
        } else {
            tabLayout?.visibility = View.VISIBLE
        }

        this.mainActivity?.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val group = viewModel.getGroupData().value
        if (viewModel.isLeader) {
            inflater.inflate(R.menu.menu_party_admin, menu)
            menu.findItem(R.id.menu_guild_leave).isVisible = group?.memberCount != 1
        } else {
            inflater.inflate(R.menu.menu_party, menu)
        }
    }

    @Suppress("ReturnCount")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_invite_item -> {
                MainNavigationController.navigate(R.id.partyInvitationFragment)
                return true
            }
            R.id.menu_guild_edit -> {
                this.displayEditForm()
                return true
            }
            R.id.menu_guild_leave -> {
                detailFragment?.leaveParty()
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

    private fun displayEditForm() {
        val bundle = Bundle()
        val group = viewModel.getGroupData().value
        bundle.putString("groupID", group?.id)
        bundle.putString("name", group?.name)
        bundle.putString("groupType", group?.type)
        bundle.putString("description", group?.description)
        bundle.putString("leader", group?.leaderID)
        bundle.putBoolean("leaderCreateChallenge", group?.leaderOnlyChallenges ?: false)

        val intent = Intent(mainActivity, GroupFormActivity::class.java)
        intent.putExtras(bundle)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        groupFormResult.launch(intent)
    }

    private val groupFormResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            viewModel.updateOrCreateGroup(it.data?.extras)
        }
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager

        viewPagerAdapter = object : FragmentStateAdapter(fragmentManager, lifecycle) {

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> {
                        detailFragment = PartyDetailFragment()
                        detailFragment
                    }
                    1 -> {
                        PartyChatFragment()
                    }
                    else -> Fragment()
                } ?: Fragment()
            }

            override fun getItemCount(): Int {
                return 2
            }
        }
        binding?.viewPager?.adapter = viewPagerAdapter

        tabLayout?.let {
            binding?.viewPager?.let { it1 ->
                TabLayoutMediator(it, it1) { tab, position ->
                    tab.text = when (position) {
                        0 -> context?.getString(R.string.party)
                        1 -> context?.getString(R.string.chat)
                        else -> ""
                    }
                }.attach()
            }
        }
    }
}
