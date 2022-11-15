package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivityPartyInviteBinding
import com.habitrpg.android.habitica.extensions.runDelayed
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyInviteFragment
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.Companion.showSnackbar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class GroupInviteActivity : BaseActivity() {

    private lateinit var binding: ActivityPartyInviteBinding

    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String
    @Inject
    lateinit var socialRepository: SocialRepository

    internal var fragments: MutableList<PartyInviteFragment> = ArrayList()

    override fun getLayoutResId(): Int {
        return R.layout.activity_party_invite
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityPartyInviteBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar(findViewById(R.id.toolbar))
        binding.viewPager.currentItem = 0

        supportActionBar?.title = null

        setViewPagerAdapter()
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_party_invite, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_send_invites -> {
                setResult(Activity.RESULT_OK, createResultIntent())
                dismissKeyboard()
                if (fragments.size > binding.viewPager.currentItem && fragments[binding.viewPager.currentItem].values.isNotEmpty()) {
                    showSnackbar(binding.snackbarView, "Invite Sent!", HabiticaSnackbar.SnackbarDisplayType.SUCCESS)
                    runDelayed(1, TimeUnit.SECONDS, this::finish)
                } else {
                    finish()
                }
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createResultIntent(): Intent {
        val intent = Intent()
        if (fragments.size == 0) return intent
        for (fragment in fragments) {
            if (fragment.isEmailInvite) {
                intent.putExtra(EMAILS_KEY, fragment.values)
            } else {
                intent.putExtra(USER_IDS_KEY, fragment.values)
            }
        }
        return intent
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = supportFragmentManager

        val statePagerAdapter = object : FragmentStateAdapter(fragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                val fragment = PartyInviteFragment()
                fragment.isEmailInvite = position == 1
                fragments.add(fragment)
                return fragment
            }

            override fun getItemCount(): Int {
                return 2
            }
        }
        binding.viewPager.adapter = statePagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.invite_existing_users)
                1 -> getString(R.string.by_email)
                else -> ""
            }
        }.attach()
        statePagerAdapter.notifyDataSetChanged()
    }

    companion object {
        const val RESULT_SEND_INVITES = 100
        const val USER_IDS_KEY = "userIDs"
        const val IS_EMAIL_KEY = "isEmail"
        const val EMAILS_KEY = "emails"
    }
}
