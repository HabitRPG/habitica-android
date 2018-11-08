package com.habitrpg.android.habitica.ui.fragments.social.party

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.activities.PartyInviteActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.social.ChatFragment
import com.habitrpg.android.habitica.ui.fragments.social.ChatListFragment
import com.habitrpg.android.habitica.ui.fragments.social.GroupInformationFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewType
import com.habitrpg.android.habitica.ui.viewmodels.PartyViewModel
import java.util.*

class PartyFragment : BaseMainFragment() {

    private val viewPager: ViewPager? by bindView(R.id.viewPager)
    private var partyMemberListFragment: PartyMemberListFragment? = null
    private var chatFragment: ChatFragment? = null
    private var viewPagerAdapter: androidx.fragment.app.FragmentPagerAdapter? = null

    private lateinit var viewModel: PartyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.usesTabLayout = true
        hideToolbar()
        disableToolbarScrolling()
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

        viewModel.loadPartyID()

        // Get the full group data
        if (userHasParty()) {
            viewModel.retrieveGroup {}
        }

        viewPager?.currentItem = 0

        setViewPagerAdapter()
        this.tutorialStepIdentifier = "party"
        this.tutorialText = getString(R.string.tutorial_party)
    }

    private fun userHasParty(): Boolean {
        return user?.party?.id?.isNotEmpty() == true
    }

    override fun onDestroyView() {
        showToolbar()
        enableToolbarScrolling()
        super.onDestroyView()
    }

    override fun injectFragment(component: AppComponent) {
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

        partyMemberListFragment?.setPartyId(group.id)

        chatFragment?.groupId = group.id

        this.activity?.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        val group = viewModel.getGroupData().value
        if (group != null && this.user != null) {
            if (group.leaderID == this.user?.id) {
                inflater?.inflate(R.menu.menu_party_admin, menu)
            } else {
                inflater?.inflate(R.menu.menu_party, menu)
            }
        }
    }

    @Suppress("ReturnCount")
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId

        when (id) {
            R.id.menu_invite_item -> {
                val intent = Intent(activity, PartyInviteActivity::class.java)
                startActivityForResult(intent, PartyInviteActivity.RESULT_SEND_INVITES)
                return true
            }
            R.id.menu_guild_edit -> {
                this.displayEditForm()
                return true
            }
            R.id.menu_guild_leave -> {
                AlertDialog.Builder(context)
                        .setTitle(context?.getString(R.string.leave_party))
                        .setMessage(context?.getString(R.string.leave_party_confirmation))
                        .setPositiveButton(context?.getString(R.string.yes)) { _, _ ->
                            viewModel.leaveGroup {
                                parentFragment.notNull { fragment ->
                                    activity?.supportFragmentManager?.beginTransaction()?.remove(fragment)?.commit()
                                }
                            }
                        }
                        .setNegativeButton(context?.getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
                        .show()
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
        bundle.putString("description", group?.description)
        bundle.putString("leader", group?.leaderID)

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
                    viewModel.updateGroup(data?.extras)
                }
            }
            PartyInviteActivity.RESULT_SEND_INVITES -> {
                if (resultCode == Activity.RESULT_OK) {
                    val inviteData = HashMap<String, Any>()
                    inviteData["inviter"] = user?.profile?.name ?: ""
                    if (data?.getBooleanExtra(PartyInviteActivity.IS_EMAIL_KEY, false) == true) {
                        val emails = data.getStringArrayExtra(PartyInviteActivity.EMAILS_KEY)
                        val invites = ArrayList<HashMap<String, String>>()
                        for (email in emails) {
                            val invite = HashMap<String, String>()
                            invite["name"] = ""
                            invite["email"] = email
                            invites.add(invite)
                        }
                        inviteData["emails"] = invites
                    } else {
                        val userIDs = data?.getStringArrayExtra(PartyInviteActivity.USER_IDS_KEY)
                        val invites = ArrayList<String>()
                        Collections.addAll(invites, *userIDs)
                        inviteData["uuids"] = invites
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

        viewPagerAdapter = object : FragmentPagerAdapter(fragmentManager) {

            override fun getItem(position: Int): androidx.fragment.app.Fragment? {

                val fragment: androidx.fragment.app.Fragment?

                when (position) {
                    0 -> {
                        fragment = if (user?.hasParty() == true) {
                            val detailFragment = PartyDetailFragment(viewModel)
                            detailFragment
                        } else {
                            GroupInformationFragment.newInstance(null, user)
                        }
                    }
                    1 -> {
                        if (chatFragment == null) {
                            chatFragment = ChatFragment(viewModel)
                        }
                        fragment = chatFragment
                    }
                    2 -> {
                        if (partyMemberListFragment == null) {
                            partyMemberListFragment = PartyMemberListFragment()
                            if (user?.hasParty() == true) {
                                partyMemberListFragment?.setPartyId(user?.party?.id ?: "")
                            }
                        }
                        fragment = partyMemberListFragment
                    }
                    else -> fragment = Fragment()
                }

                return fragment
            }

            override fun getCount(): Int {
                return if (viewModel.getGroupData().value == null) {
                    1
                } else {
                    3
                }
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> context?.getString(R.string.party)
                    1 -> context?.getString(R.string.chat)
                    2 -> context?.getString(R.string.members)
                    else -> ""
                } ?: ""
            }
        }
        this.viewPager?.adapter = viewPagerAdapter

        viewPager?.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
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

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        tabLayout?.setupWithViewPager(viewPager)
    }


    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.sidebar_party)
    }

}
