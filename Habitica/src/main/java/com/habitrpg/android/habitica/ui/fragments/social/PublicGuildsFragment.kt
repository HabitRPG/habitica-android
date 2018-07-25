package com.habitrpg.android.habitica.ui.fragments.social

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
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

    var memberGuildIDs: List<String>? = null

    private val recyclerView: RecyclerView? by bindView(R.id.recyclerView)

    private var viewAdapter = PublicGuildsRecyclerViewAdapter(null, true)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_recyclerview)
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        recyclerView?.layoutManager = LinearLayoutManager(this.activity)
        recyclerView?.addItemDecoration(DividerItemDecoration(getActivity()!!, DividerItemDecoration.VERTICAL))
        viewAdapter.setMemberGuildIDs(this.memberGuildIDs?.toMutableList() ?: mutableListOf<String>())
        viewAdapter.apiClient = this.apiClient
        viewAdapter = PublicGuildsRecyclerViewAdapter(null, true)
        recyclerView?.adapter = viewAdapter
        recyclerView?.itemAnimator = SafeDefaultItemAnimator()
        this.fetchGuilds()
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }

    private fun fetchGuilds() {
        this.socialRepository.getPublicGuilds()
                .firstElement()
                .subscribe(Consumer { groups ->
                    this@PublicGuildsFragment.viewAdapter.updateData(groups)
                }, RxErrorHandler.handleEmptyError())
        this.socialRepository.retrieveGroups("publicGuilds").subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_public_guild, menu)

        val searchItem = menu?.findItem(R.id.action_guild_search)
        val guildSearchView = searchItem?.actionView as? SearchView
        val theTextArea = guildSearchView?.findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text)
        context.notNull { theTextArea?.setHintTextColor(ContextCompat.getColor(it, R.color.white)) }
        guildSearchView?.queryHint = getString(R.string.guild_search_hint)
        guildSearchView?.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(s: String): Boolean {
        viewAdapter.filter.filter(s)
        activity.notNull {
            KeyboardUtil.dismissKeyboard(it)
        }
        return true
    }

    override fun onQueryTextChange(s: String): Boolean {
        viewAdapter.filter.filter(s)
        return true
    }

    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.public_guilds)
    }
}
