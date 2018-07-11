package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.events.commands.ShowChallengeDetailActivityCommand
import com.habitrpg.android.habitica.events.commands.ShowChallengeDetailDialogCommand
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.ui.activities.ChallengeDetailActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.utils.Action1
import io.reactivex.functions.Consumer

import org.greenrobot.eventbus.Subscribe

import javax.inject.Inject

class ChallengesOverviewFragment : BaseMainFragment() {

    @Inject
    internal lateinit var challengeRepository: ChallengeRepository

    private val viewPager: ViewPager? by bindView(R.id.viewPager)
    var statePagerAdapter: FragmentStatePagerAdapter? = null
    private var userChallengesFragment: ChallengeListFragment? = null
    private var availableChallengesFragment: ChallengeListFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.usesTabLayout = true
        super.onCreateView(inflater, container, savedInstanceState)
        val v = inflater.inflate(R.layout.fragment_viewpager, container, false)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewPagerAdapter()

        userChallengesFragment = ChallengeListFragment()
        userChallengesFragment?.user = this.user
        userChallengesFragment?.setViewUserChallengesOnly(true)

        availableChallengesFragment = ChallengeListFragment()
        availableChallengesFragment?.user = this.user
        availableChallengesFragment?.setViewUserChallengesOnly(false)
    }

    override fun onDestroy() {
        challengeRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager

        statePagerAdapter = object : FragmentStatePagerAdapter(fragmentManager) {

            override fun getItem(position: Int): Fragment? {
                val fragment = Fragment()

                when (position) {
                    0 -> return userChallengesFragment
                    1 -> return availableChallengesFragment
                }

                return fragment
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                when (position) {
                    0 -> return getString(R.string.my_challenges)
                    1 -> return getString(R.string.public_challenges)
                }
                return ""
            }
        }
        viewPager?.adapter = statePagerAdapter
        tabLayout?.setupWithViewPager(viewPager)
    }

    @Subscribe
    fun onEvent(cmd: ShowChallengeDetailDialogCommand) {
        challengeRepository.getChallenge(cmd.challengeId).firstElement().subscribe(Consumer { challenge ->
            ChallengeDetailDialogHolder.showDialog(activity, challengeRepository, challenge,
                    object: Action1<Challenge> {
                        override fun call(t: Challenge) {

                        }
                    })
        }, RxErrorHandler.handleEmptyError())
    }

    @Subscribe
    fun onEvent(cmd: ShowChallengeDetailActivityCommand) {
        val bundle = Bundle()
        bundle.putString(ChallengeDetailActivity.CHALLENGE_ID, cmd.challengeId)

        val intent = Intent(activity, ChallengeDetailActivity::class.java)
        intent.putExtras(bundle)
        activity?.startActivity(intent)
    }

    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.challenges)
    }
}
