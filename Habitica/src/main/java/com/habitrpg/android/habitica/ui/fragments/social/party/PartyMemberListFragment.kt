package com.habitrpg.android.habitica.ui.fragments.social.party

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.adapter.social.PartyMemberRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.viewmodels.PartyViewModel
import io.reactivex.functions.Consumer
import javax.inject.Inject

class PartyMemberListFragment constructor() : BaseFragment() {

    lateinit var viewModel: PartyViewModel

    @Inject
    lateinit var socialRepository: SocialRepository

    private val recyclerView: androidx.recyclerview.widget.RecyclerView? by bindView(R.id.recyclerView)
    private val refreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? by bindView(R.id.refreshLayout)
    private var adapter: PartyMemberRecyclerViewAdapter? = null
    private var partyId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_refresh_recyclerview)
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        adapter = PartyMemberRecyclerViewAdapter(null, true)
        adapter?.getUserClickedEvents()?.subscribe(Consumer { userId -> FullProfileActivity.open(userId) }, RxErrorHandler.handleEmptyError()).notNull { compositeSubscription.add(it) }
        recyclerView?.adapter = adapter
        recyclerView?.itemAnimator = SafeDefaultItemAnimator()

        refreshLayout?.setOnRefreshListener { this.refreshMembers() }

        getUsers()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getGroupData().observe(viewLifecycleOwner, Observer {
            adapter?.leaderID = it?.leaderID
            adapter?.notifyDataSetChanged()
        })
    }

    private fun refreshMembers() {
        setRefreshing(true)
        compositeSubscription.add(socialRepository.retrieveGroupMembers(partyId ?: "", true).doOnComplete { setRefreshing(false) }.subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
    }

    private fun setRefreshing(isRefreshing: Boolean) {
        refreshLayout?.isRefreshing = isRefreshing
    }

    fun setPartyId(id: String) {
        this.partyId = id
    }

    private fun getUsers() {
        if (partyId == null) {
            return
        }
        compositeSubscription.add(socialRepository.getGroupMembers(partyId ?: "").firstElement().subscribe(Consumer { users ->
            adapter?.updateData(users)
        }, RxErrorHandler.handleEmptyError()))
    }
}
