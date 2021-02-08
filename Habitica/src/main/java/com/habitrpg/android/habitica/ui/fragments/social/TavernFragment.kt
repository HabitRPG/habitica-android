package com.habitrpg.android.habitica.ui.fragments.social

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewModel
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewType
import javax.inject.Inject

class TavernFragment : BaseMainFragment<FragmentViewpagerBinding>() {

    @Inject
    lateinit var socialRepository: SocialRepository

    override var binding: FragmentViewpagerBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentViewpagerBinding {
        return FragmentViewpagerBinding.inflate(inflater, container, false)
    }

    internal lateinit var viewModel: GroupViewModel

    internal var tavernDetailFragment = TavernDetailFragment()
    private var chatFragment: ChatFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.usesTabLayout = true
        this.hidesToolbar = true
        this.tutorialStepIdentifier = "tavern"
        this.tutorialText = getString(R.string.tutorial_tavern)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(GroupViewModel::class.java)
        viewModel.groupViewType = GroupViewType.GUILD
        viewModel.setGroupID(Group.TAVERN_ID)

        setViewPagerAdapter()
        binding?.viewPager?.currentItem = 0
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tavern, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Suppress("ReturnCount")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_guild_refresh -> {
                viewModel.retrieveGroup {  }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager
        binding?.viewPager?.adapter = object : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> {
                        tavernDetailFragment
                    }
                    1 -> {
                        chatFragment = ChatFragment()
                        chatFragment?.viewModel = viewModel
                        chatFragment ?: Fragment()
                    }
                    else -> Fragment()
                }
            }

            override fun getCount(): Int {
                return if (viewModel.getGroupData().value?.quest?.active == true) {
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
        tabLayout?.setupWithViewPager(binding?.viewPager)
    }

}
