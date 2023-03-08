package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.ui.activities.ChallengeFormActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChallengesOverviewFragment : BaseMainFragment<FragmentViewpagerBinding>() {

    @Inject
    internal lateinit var challengeRepository: ChallengeRepository

    override var binding: FragmentViewpagerBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentViewpagerBinding {
        return FragmentViewpagerBinding.inflate(inflater, container, false)
    }

    private var statePagerAdapter: FragmentStateAdapter? = null
    private var userChallengesFragment: ChallengeListFragment? = ChallengeListFragment()
    private var availableChallengesFragment: ChallengeListFragment? = ChallengeListFragment()

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

        userChallengesFragment?.setViewUserChallengesOnly(true)

        availableChallengesFragment?.setViewUserChallengesOnly(false)
        setViewPagerAdapter()
    }

    override fun onResume() {
        super.onResume()
        getActiveFragment()?.retrieveChallengesPage()
    }

    override fun onDestroy() {
        challengeRepository.close()
        super.onDestroy()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_list_challenges, menu)

        @Suppress("Deprecation")
        val badgeLayout = MenuItemCompat.getActionView(menu.findItem(R.id.action_search)) as? RelativeLayout
        if (badgeLayout != null) {
            val filterCountTextView = badgeLayout.findViewById<TextView>(R.id.badge_textview)
            filterCountTextView.text = null
            filterCountTextView.visibility = View.GONE
            badgeLayout.setOnClickListener { getActiveFragment()?.showFilterDialog() }
        }
    }

    @Suppress("ReturnCount")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_create_challenge -> {
                val intent = Intent(getActivity(), ChallengeFormActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_reload -> {
                getActiveFragment()?.retrieveChallengesPage()
                return true
            }
            R.id.action_search -> {
                getActiveFragment()?.showFilterDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getActiveFragment(): ChallengeListFragment? {
        return if (binding?.viewPager?.currentItem == 0) {
            userChallengesFragment
        } else {
            availableChallengesFragment
        }
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager

        statePagerAdapter = object : FragmentStateAdapter(fragmentManager, lifecycle) {

            override fun createFragment(position: Int): Fragment {
                return if (position == 0) {
                    userChallengesFragment
                } else {
                    availableChallengesFragment
                } ?: Fragment()
            }

            override fun getItemCount(): Int {
                return 2
            }
        }
        binding?.viewPager?.adapter = statePagerAdapter
        tabLayout?.let {
            binding?.viewPager?.let { it1 ->
                TabLayoutMediator(it, it1) { tab, position ->
                    tab.text = when (position) {
                        0 -> getString(R.string.my_challenges)
                        1 -> getString(R.string.discover)
                        else -> ""
                    }
                }.attach()
            }
        }
        statePagerAdapter?.notifyDataSetChanged()
    }
}
