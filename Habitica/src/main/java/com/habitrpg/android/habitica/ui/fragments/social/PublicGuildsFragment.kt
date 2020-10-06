package com.habitrpg.android.habitica.ui.fragments.social

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentRecyclerviewBinding
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.adapter.social.PublicGuildsRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.KeyboardUtil
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import javax.inject.Inject

class PublicGuildsFragment : BaseMainFragment<FragmentRecyclerviewBinding>(), SearchView.OnQueryTextListener {

    @Inject
    lateinit var socialRepository: SocialRepository

    override var binding: FragmentRecyclerviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRecyclerviewBinding {
        return FragmentRecyclerviewBinding.inflate(inflater, container, false)
    }

    private var viewAdapter = PublicGuildsRecyclerViewAdapter()

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        binding?.recyclerView?.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(activity, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        compositeSubscription.add(socialRepository.getGroupMemberships()
                .map { it.map { membership -> membership.groupID } }
                .subscribeWithErrorHandler { viewAdapter.setMemberGuildIDs(it) })
        viewAdapter.socialRepository = socialRepository
        binding?.recyclerView?.adapter = viewAdapter
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
        this.fetchGuilds()
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }

    private fun fetchGuilds() {
        compositeSubscription.add(this.socialRepository.getPublicGuilds()
                .subscribe({ groups ->
                    this@PublicGuildsFragment.viewAdapter.setUnfilteredData(groups)
                }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(this.socialRepository.retrieveGroups("publicGuilds").subscribe({ }, RxErrorHandler.handleEmptyError()))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_public_guild, menu)

        val searchItem = menu.findItem(R.id.action_guild_search)
        val guildSearchView = searchItem?.actionView as? SearchView
        val theTextArea = guildSearchView?.findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text)
        context?.let { theTextArea?.setHintTextColor(ContextCompat.getColor(it, R.color.white)) }
        guildSearchView?.queryHint = getString(R.string.guild_search_hint)
        guildSearchView?.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(s: String): Boolean {
        viewAdapter.filter.filter(s)
        activity?.let {
            KeyboardUtil.dismissKeyboard(it)
        }
        return true
    }

    override fun onQueryTextChange(s: String): Boolean {
        viewAdapter.filter.filter(s)
        return true
    }

}
