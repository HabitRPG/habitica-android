package com.habitrpg.android.habitica.ui.fragments.social

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import io.reactivex.functions.Consumer

import javax.inject.Inject

class GuildFragment : BaseMainFragment() {

    @Inject
    internal lateinit var socialRepository: SocialRepository

    var isMember: Boolean = false
    private val viewPager: androidx.viewpager.widget.ViewPager? by bindView(R.id.viewPager)
    private var guild: Group? = null
    private var guildInformationFragment: GroupInformationFragment? = null
    private var chatListFragment: ChatListFragment? = null
    private var guildId: String? = null

    fun setGuildId(guildId: String) {
        this.guildId = guildId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.usesTabLayout = true
        this.hidesToolbar = true
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_viewpager, container, false)
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val newArguments = arguments
        if (newArguments != null) {
            val args = GuildFragmentArgs.fromBundle(newArguments)
            guildId = args.groupID
            isMember = args.isMember
        }

        viewPager?.currentItem = 0

        setViewPagerAdapter()

        guildId.notNull { guildId ->
            compositeSubscription.add(socialRepository.getGroup(guildId).subscribe(Consumer { this.setGroup(it) }, RxErrorHandler.handleEmptyError()))
            socialRepository.retrieveGroup(guildId).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
            socialRepository.getGroup(guildId).subscribe(Consumer<Group> { this.setGroup(it) }, RxErrorHandler.handleEmptyError())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (this.activity != null && this.guild != null) {
            if (this.isMember) {
                if (this.user != null && this.user?.id == this.guild?.leaderID) {
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
        val id = item.itemId

        when (id) {
            R.id.menu_guild_join -> {
                this.socialRepository.joinGroup(this.guild?.id).subscribe(Consumer { this.setGroup(it) }, RxErrorHandler.handleEmptyError())
                this.isMember = true
                return true
            }
            R.id.menu_guild_leave -> {
                this.socialRepository.leaveGroup(this.guild?.id)
                        .subscribe(Consumer {
                            this.activity?.invalidateOptionsMenu()
                        }, RxErrorHandler.handleEmptyError())
                this.isMember = false
                return true
            }
            R.id.menu_guild_edit -> {
                this.displayEditForm()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager

        viewPager?.adapter = object : FragmentPagerAdapter(fragmentManager) {

            override fun getItem(position: Int): androidx.fragment.app.Fragment {

                val fragment: androidx.fragment.app.Fragment?

                when (position) {
                    0 -> {
                        guildInformationFragment = GroupInformationFragment.newInstance(this@GuildFragment.guild, user)
                        fragment = guildInformationFragment
                    }
                    1 -> {
                        chatListFragment = ChatListFragment()
                        chatListFragment?.configure(this@GuildFragment.guildId ?: "", user, false, "guild")
                        fragment = chatListFragment
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

        viewPager?.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (position == 1 && this@GuildFragment.guild != null) {
                    chatListFragment?.setNavigatedToFragment()
                }
            }

            override fun onPageSelected(position: Int) {
                if (position == 1 && this@GuildFragment.guild != null && chatListFragment != null) {
                    chatListFragment?.setNavigatedToFragment()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        tabLayout?.setupWithViewPager(viewPager)
    }

    private fun displayEditForm() {
        val bundle = Bundle()
        bundle.putString("groupID", guild?.id)
        bundle.putString("name", guild?.name)
        bundle.putString("description", guild?.description)
        bundle.putString("privacy", guild?.privacy)
        bundle.putString("leader", guild?.leaderID)
        bundle.putBoolean("leaderCreateChallenge", guild?.leaderOnlyChallenges ?: true)


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
                    this.socialRepository.updateGroup(this.guild,
                            bundle?.getString("name"),
                            bundle?.getString("description"),
                            bundle?.getString("leader"),
                            bundle?.getBoolean("leaderCreateChallenge"))
                            .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                }
            }
        }
    }

    private fun setGroup(group: Group?) {
        if (group != null) {
            guildInformationFragment?.group = group

            this.chatListFragment?.groupId = group.id

            this.guild = group
        }
        this.activity?.invalidateOptionsMenu()

        if (group?.privacy == "public") {
            chatListFragment?.autocompleteContext = "publicGuild"
        } else {
            chatListFragment?.autocompleteContext = "privateGuild"
        }
    }

}
