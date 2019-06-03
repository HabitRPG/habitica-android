package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.ui.activities.ChallengeFormActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import javax.inject.Inject

class ChallengesOverviewFragment : BaseMainFragment() {

    @Inject
    internal lateinit var challengeRepository: ChallengeRepository

    private val viewPager: androidx.viewpager.widget.ViewPager? by bindView(R.id.viewPager)
    var statePagerAdapter: FragmentStatePagerAdapter? = null
    private var userChallengesFragment: ChallengeListFragment? = ChallengeListFragment()
    private var availableChallengesFragment: ChallengeListFragment? = ChallengeListFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.usesTabLayout = true
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        userChallengesFragment?.user = this.user
        userChallengesFragment?.setViewUserChallengesOnly(true)

        availableChallengesFragment?.user = this.user
        availableChallengesFragment?.setViewUserChallengesOnly(false)
        setViewPagerAdapter()
    }

    override fun onDestroy() {
        challengeRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        when (id) {
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
        return if (viewPager?.currentItem == 0) {
            userChallengesFragment
        } else {
            availableChallengesFragment
        }
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager

        statePagerAdapter = object : FragmentStatePagerAdapter(fragmentManager) {

            override fun getItem(position: Int): Fragment {
                return if (position == 0) {
                    userChallengesFragment
                } else {
                    availableChallengesFragment
                } ?: Fragment()
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> getString(R.string.my_challenges)
                    1 -> getString(R.string.discover)
                    else -> ""
                }
            }
        }
        viewPager?.adapter = statePagerAdapter
        tabLayout?.setupWithViewPager(viewPager)
        statePagerAdapter?.notifyDataSetChanged()
    }

}
