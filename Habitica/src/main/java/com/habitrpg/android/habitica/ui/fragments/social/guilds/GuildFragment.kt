package com.habitrpg.android.habitica.ui.fragments.social.guilds

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.social.ChatFragment
import com.habitrpg.android.habitica.ui.fragments.social.guilds.GuildFragmentArgs
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewModel
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewType

class GuildFragment : BaseMainFragment<FragmentViewpagerBinding>() {

    internal lateinit var viewModel: GroupViewModel
    private var guildInformationFragment: GuildDetailFragment? = null
    private var chatFragment: ChatFragment? = null

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

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showsBackButton = true
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(GroupViewModel::class.java)
        viewModel.groupViewType = GroupViewType.GUILD
        viewModel.getGroupData().observe(viewLifecycleOwner, { setGroup(it) })
        viewModel.getIsMemberData().observe(viewLifecycleOwner, { activity?.invalidateOptionsMenu() })

        arguments?.let {
            val args = GuildFragmentArgs.fromBundle(it)
            viewModel.setGroupID(args.groupID)
        }

        binding?.viewPager?.currentItem = 0

        setViewPagerAdapter()
        setFragments()

        arguments?.let {
            val args = GuildFragmentArgs.fromBundle(it)
            binding?.viewPager?.currentItem = args.tabToOpen
        }

        if (viewModel.groupID == "f2db2a7f-13c5-454d-b3ee-ea1f5089e601") {
            context?.let { FirebaseAnalytics.getInstance(it).logEvent("opened_no_party_guild", null) }
        }

        viewModel.retrieveGroup {  }
    }

    private fun setFragments() {
        val fragments = childFragmentManager.fragments
        for (childFragment in fragments) {
            if (childFragment is ChatFragment) {
                chatFragment = childFragment
                chatFragment?.viewModel = viewModel
            }
            if (childFragment is GuildDetailFragment) {
                guildInformationFragment = childFragment
                childFragment.viewModel = viewModel
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val guild = viewModel.getGroupData().value
        if (this.activity != null && guild != null) {
            if (viewModel.isMember) {
                if (this.user != null && this.user?.id == guild.leaderID) {
                    this.activity?.menuInflater?.inflate(R.menu.guild_admin, menu)
                } else {
                    this.activity?.menuInflater?.inflate(R.menu.guild_member, menu)
                }
            } else {
                this.activity?.menuInflater?.inflate(R.menu.guild_nonmember, menu)
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
            R.id.action_reload -> {
                viewModel.retrieveGroup { }
                return true
            }
            R.id.menu_guild_refresh -> {
                viewModel.retrieveGroup {  }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager

        binding?.viewPager?.adapter = object : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            override fun getItem(position: Int): Fragment {
                val fragment: Fragment?

                when (position) {
                    0 -> {
                        guildInformationFragment = GuildDetailFragment.newInstance(viewModel)
                        fragment = guildInformationFragment
                    }
                    1 -> {
                        chatFragment = ChatFragment()
                        chatFragment?.viewModel = viewModel
                        fragment = chatFragment
                    }
                    else -> fragment = Fragment()
                }

                return fragment ?: Fragment()
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 ->  context?.getString(R.string.guild)
                    1 ->  context?.getString(R.string.chat)
                    else -> ""
                }
            }
        }

        binding?.viewPager?.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (position == 1) {
                    chatFragment?.setNavigatedToFragment()
                }
            }

            override fun onPageSelected(position: Int) {
                if (position == 1) {
                    chatFragment?.setNavigatedToFragment()
                }
            }

            override fun onPageScrollStateChanged(state: Int) { /* no-on */ }
        })

        tabLayout?.setupWithViewPager(binding?.viewPager)
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


        val intent = Intent(activity, GroupFormActivity::class.java)
        intent.putExtras(bundle)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivityForResult(intent, GroupFormActivity.GROUP_FORM_ACTIVITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GroupFormActivity.GROUP_FORM_ACTIVITY -> {
                if (resultCode == Activity.RESULT_OK) {
                    val bundle = data?.extras
                    viewModel.updateGroup(bundle)
                }
            }
        }
    }

    private fun setGroup(group: Group?) {
        this.activity?.invalidateOptionsMenu()

        if (group?.privacy == "public") {
            chatFragment?.autocompleteContext = "publicGuild"
        } else {
            chatFragment?.autocompleteContext = "privateGuild"
        }
    }

}
