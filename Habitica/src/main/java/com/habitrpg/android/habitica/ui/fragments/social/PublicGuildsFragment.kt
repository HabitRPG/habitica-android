package com.habitrpg.android.habitica.ui.fragments.social

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.adapter.social.PublicGuildsRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.KeyboardUtil
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import io.reactivex.functions.Consumer
import javax.inject.Inject

class PublicGuildsFragment : BaseMainFragment(), SearchView.OnQueryTextListener {

    @Inject
    lateinit var socialRepository: SocialRepository

    private val recyclerView: androidx.recyclerview.widget.RecyclerView? by bindView(R.id.recyclerView)

    private var viewAdapter = PublicGuildsRecyclerViewAdapter(null, true)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_recyclerview)
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        recyclerView?.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(activity, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        viewAdapter = PublicGuildsRecyclerViewAdapter(null, true)
        compositeSubscription.add(socialRepository.getGroupMemberships()
                .map { it.map { membership -> membership.groupID } }
                .subscribeWithErrorHandler(Consumer { viewAdapter.setMemberGuildIDs(it) }))
        viewAdapter.socialRepository = socialRepository
        recyclerView?.adapter = viewAdapter
        recyclerView?.itemAnimator = SafeDefaultItemAnimator()
        this.fetchGuilds()
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }

    private fun fetchGuilds() {
        compositeSubscription.add(this.socialRepository.getPublicGuilds()
                .firstElement()
                .subscribe(Consumer { groups ->
                    this@PublicGuildsFragment.viewAdapter.setUnfilteredData(groups)
                }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(this.socialRepository.retrieveGroups("publicGuilds").subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
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
