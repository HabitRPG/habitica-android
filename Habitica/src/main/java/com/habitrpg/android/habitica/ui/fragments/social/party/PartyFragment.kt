package com.habitrpg.android.habitica.ui.fragments.social.party

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.activities.GroupInviteActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.social.ChatFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewType
import com.habitrpg.android.habitica.ui.viewmodels.PartyViewModel
import java.util.*


class PartyFragment : BaseMainFragment() {

    private var detailFragment: PartyDetailFragment? = null
    private val viewPager: ViewPager? by bindView(R.id.viewPager)
    private var chatFragment: ChatFragment? = null
    private var viewPagerAdapter: FragmentPagerAdapter? = null

    internal lateinit var viewModel: PartyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.usesTabLayout = true
        this.hidesToolbar = true
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_viewpager, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        resetViews()

        viewModel = ViewModelProviders.of(this)
                .get(PartyViewModel::class.java)
        viewModel.groupViewType = GroupViewType.PARTY

        viewModel.getGroupData().observe(viewLifecycleOwner,
                Observer {
                    updateGroupUI(it)
                })


        viewPager?.currentItem = 0

        setViewPagerAdapter()

        arguments?.let {
            val args = PartyFragmentArgs.fromBundle(it)
            viewPager?.currentItem = args.tabToOpen
            if (args.partyID?.isNotBlank() == true) {
                viewModel.setGroupID(args.partyID ?: "")
            }
        }

        viewModel.loadPartyID()

        this.tutorialStepIdentifier = "party"
        this.tutorialText = getString(R.string.tutorial_party)

        viewModel.retrieveGroup {  }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun updateGroupUI(group: Group?) {
        viewPagerAdapter?.notifyDataSetChanged()

        if (group == null) {
            tabLayout?.visibility = View.GONE
            return
        } else {
            tabLayout?.visibility = View.VISIBLE
        }

        this.activity?.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val group = viewModel.getGroupData().value
        if (group != null && this.user != null) {
            if (group.leaderID == this.user?.id) {
                inflater.inflate(R.menu.menu_party_admin, menu)
                if (group.memberCount > 1) {
                    menu.findItem(R.id.menu_guild_leave).isVisible = false
                }
            } else {
                inflater.inflate(R.menu.menu_party, menu)
            }
        }
    }

    @Suppress("ReturnCount")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_invite_item -> {
                val intent = Intent(activity, GroupInviteActivity::class.java)
                intent.putExtra("groupType", "party")
                startActivityForResult(intent, GroupInviteActivity.RESULT_SEND_INVITES)
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
                viewModel.retrieveGroup {  }
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
                    viewModel.updateOrCreateGroup(data?.extras)
                }
            }
            GroupInviteActivity.RESULT_SEND_INVITES -> {
                if (resultCode == Activity.RESULT_OK) {
                    val inviteData = HashMap<String, Any>()
                    inviteData["inviter"] = user?.profile?.name ?: ""
                    if (data?.getBooleanExtra(GroupInviteActivity.IS_EMAIL_KEY, false) == true) {
                        val emails = data.getStringArrayExtra(GroupInviteActivity.EMAILS_KEY)
                        val invites = ArrayList<HashMap<String, String>>()
                        emails?.forEach { email ->
                            val invite = HashMap<String, String>()
                            invite["name"] = ""
                            invite["email"] = email
                            invites.add(invite)
                        }
                        inviteData["emails"] = invites
                    } else {
                        val userIDs = data?.getStringArrayExtra(GroupInviteActivity.USER_IDS_KEY)
                        val invites = ArrayList<String>()
                        userIDs?.forEach { invites.add(it) }
                        inviteData["usernames"] = invites
                    }
                    viewModel.inviteToGroup(inviteData)
                }
            }
        }
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager
        if (this.user == null) {
            return
        }

        viewPagerAdapter = object : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> {
                        detailFragment = PartyDetailFragment()
                        detailFragment?.viewModel = viewModel
                        detailFragment
                    }
                    1 -> {
                        if (chatFragment == null) {
                            chatFragment = ChatFragment()
                            chatFragment?.viewModel = viewModel
                        }
                        chatFragment
                    }
                    else -> Fragment()
                } ?: Fragment()

            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> context?.getString(R.string.party)
                    1 -> context?.getString(R.string.chat)
                    else -> ""
                } ?: ""
            }
        }
        this.viewPager?.adapter = viewPagerAdapter

        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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

            override fun onPageScrollStateChanged(state: Int) { /* no-op */ }
        })
        tabLayout?.setupWithViewPager(viewPager)
    }
}
