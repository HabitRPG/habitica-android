package com.habitrpg.android.habitica.ui.fragments.social.party

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.*
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.activities.PartyInviteActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.social.ChatListFragment
import com.habitrpg.android.habitica.ui.fragments.social.GroupInformationFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PartyFragment : BaseMainFragment() {

    @Inject
    internal lateinit var socialRepository: SocialRepository
    @Inject
    internal lateinit var inventoryRepository: InventoryRepository

    private val viewPager: ViewPager? by bindView(R.id.viewPager)
    private var group: Group? = null
    private var partyMemberListFragment: PartyMemberListFragment? = null
    private var chatListFragment: ChatListFragment? = null
    private var viewPagerAdapter: FragmentPagerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.usesTabLayout = true
        hideToolbar()
        disableToolbarScrolling()
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        viewPager?.currentItem = 0

        compositeSubscription.add(userRepository.getUser()
                .filter { user?.party?.id?.isNotEmpty() == true }
                .map { user?.party?.id }
                .flatMap { socialRepository.getGroup(it) }
                .firstElement()
                //delay, so that realm can save party first
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { group ->
                    this@PartyFragment.group = group
                    updateGroupUI()
                }, RxErrorHandler.handleEmptyError()))

        // Get the full group data
        if (userHasParty()) {
            compositeSubscription.add(socialRepository.retrieveGroup("party")
                    .flatMap { group1 -> socialRepository.retrieveGroupMembers(group1.id, true) }
                    .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        }

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

    override fun onDestroy() {
        socialRepository.close()
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    private fun updateGroupUI() {
        viewPagerAdapter?.notifyDataSetChanged()

        if (group == null) {
            tabLayout?.visibility = View.GONE
            return
        } else {
            tabLayout?.visibility = View.VISIBLE
        }

        partyMemberListFragment?.setPartyId(group?.id ?: "")

        chatListFragment?.groupId = group?.id ?: ""

        this.activity?.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (this.group != null && this.user != null) {
            if (this.group?.leaderID == this.user?.id) {
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
                            if (this.group != null) {
                                this.socialRepository.leaveGroup(this.group?.id ?: "")
                                        .subscribe(Consumer { activity?.supportFragmentManager?.beginTransaction()?.remove(this@PartyFragment)?.commit() }, RxErrorHandler.handleEmptyError())
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
        bundle.putString("groupID", group?.id)
        bundle.putString("name", this.group?.name)
        bundle.putString("description", this.group?.description)
        bundle.putString("leader", this.group?.leaderID)

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
                    if (this.group == null) {
                        return
                    }
                    this.socialRepository.updateGroup(this.group, bundle?.getString("name"),
                            bundle?.getString("description"),
                            bundle?.getString("leader"),
                            bundle?.getString("privacy"))
                            .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
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
                    if (this.group != null) {
                        this.socialRepository.inviteToGroup(this.group?.id ?: "", inviteData)
                                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                    }
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

            override fun getItem(position: Int): Fragment? {

                val fragment: Fragment?

                when (position) {
                    0 -> {
                        if (user?.hasParty() == true) {
                            val detailFragment = PartyDetailFragment()
                            detailFragment.partyId = user?.party?.id
                            fragment = detailFragment
                        } else {
                            fragment = GroupInformationFragment.newInstance(null, user)
                        }
                    }
                    1 -> {
                        if (chatListFragment == null) {
                            chatListFragment = ChatListFragment()
                            if (user?.hasParty() == true) {
                                chatListFragment?.configure(user?.party?.id ?: "", user, false)
                            }
                        }
                        fragment = chatListFragment
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
                return if (group == null) {
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

        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (position == 1 && group != null) {
                    chatListFragment?.setNavigatedToFragment()
                }
            }

            override fun onPageSelected(position: Int) {
                if (position == 1 && group != null) {
                       chatListFragment?.setNavigatedToFragment()
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
