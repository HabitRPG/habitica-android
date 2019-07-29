package com.habitrpg.android.habitica.ui.fragments.social

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import kotlinx.android.synthetic.main.fragment_viewpager.*
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
        this.hidesToolbar = true
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
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
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
                        chatListFragment.configure(Group.TAVERN_ID, user, true, "tavern")
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
                return when (position) {
                    0 -> context?.getString(R.string.inn)
                    1 -> context?.getString(R.string.chat)
                    2 -> context?.getString(R.string.world_quest)
                    else -> ""
                }
            }
        }
        tabLayout?.setupWithViewPager(viewPager)
    }

}
