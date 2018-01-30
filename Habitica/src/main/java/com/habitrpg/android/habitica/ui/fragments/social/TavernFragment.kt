package com.habitrpg.android.habitica.ui.fragments.social

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import kotlinx.android.synthetic.main.fragment_viewpager.*
import rx.functions.Action1
import javax.inject.Inject

class TavernFragment : BaseMainFragment() {

    @Inject
    lateinit var socialRepository: SocialRepository

    internal var tavern: Group? = null

    internal var tavernDetailFragment = TavernDetailFragment()
    internal var chatListFragment = ChatListFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.usesTabLayout = true
        hideToolbar()
        disableToolbarScrolling()
        super.onCreateView(inflater, container, savedInstanceState)
        val v = inflater.inflate(R.layout.fragment_viewpager, container, false)
        this.tutorialStepIdentifier = "tavern"
        this.tutorialText = getString(R.string.tutorial_tavern)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewPagerAdapter()
        viewPager.currentItem = 0
        compositeSubscription.add(socialRepository.getGroup("habitrpg")?.subscribe(Action1 { group ->
            this@TavernFragment.tavern = group
            if (group.quest != null && group.quest.key != null && this@TavernFragment.isAdded) {
                this@TavernFragment.viewPager.adapter?.notifyDataSetChanged()
                this@TavernFragment.tabLayout?.visibility = View.VISIBLE
                this@TavernFragment.tabLayout?.setupWithViewPager(this@TavernFragment.viewPager)
            }
        }, RxErrorHandler.handleEmptyError()))
    }

    override fun onDestroyView() {
        showToolbar()
        enableToolbarScrolling()
        super.onDestroyView()
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager
        if (this.user == null) {
            return
        }

        viewPager.adapter = object : FragmentPagerAdapter(fragmentManager) {
            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> {
                        tavernDetailFragment
                    }
                    1 -> {
                        chatListFragment.configure("habitrpg", user, true)
                        chatListFragment
                    }
                    else -> Fragment()
                }
            }

            override fun getCount(): Int {
                return if (tavern != null && tavern?.quest != null && tavern?.quest?.key != null) {
                    3
                } else 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                when (position) {
                    0 -> return context?.getString(R.string.inn)
                    1 -> return context?.getString(R.string.chat)
                    2 -> return context?.getString(R.string.world_quest)
                }
                return ""
            }
        }
        tabLayout?.setupWithViewPager(viewPager)
    }

    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.sidebar_tavern)
    }
}
