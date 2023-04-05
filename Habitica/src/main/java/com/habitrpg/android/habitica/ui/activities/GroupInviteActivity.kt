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
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivityPartyInviteBinding
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyInviteFragment
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GroupInviteActivity : BaseActivity() {

    private lateinit var binding: ActivityPartyInviteBinding

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_party_invite, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_send_invites -> {
                setResult(Activity.RESULT_OK, createResultIntent())
                dismissKeyboard()
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
        return intent
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = supportFragmentManager

        val statePagerAdapter = object : FragmentStateAdapter(fragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                val fragment = PartyInviteFragment()
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
