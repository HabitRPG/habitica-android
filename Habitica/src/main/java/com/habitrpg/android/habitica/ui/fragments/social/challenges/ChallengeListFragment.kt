package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.adapter.social.ChallengesListViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.RecyclerViewEmptySupport
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import com.habitrpg.android.habitica.utils.Action1
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import javax.inject.Inject
import javax.inject.Named


class ChallengeListFragment : BaseFragment(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var challengeRepository: ChallengeRepository
    @Inject
    lateinit var userRepository: UserRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String
    var user: User? = null

    private val swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? by bindView(R.id.refreshLayout)
    private val recyclerView: RecyclerViewEmptySupport? by bindView(R.id.recyclerView)
    private val emptyView: View? by bindView(R.id.emptyView)

    private var challengeAdapter: ChallengesListViewAdapter? = null
    private var viewUserChallengesOnly: Boolean = false

    private var nextPageToLoad = 0
    private var loadedAllData = false

    private var challenges: RealmResults<Challenge>? = null

    private var filterOptions: ChallengeFilterOptions? = null

    fun setViewUserChallengesOnly(only: Boolean) {
        this.viewUserChallengesOnly = only
    }

    override fun onDestroy() {
        challengeRepository.close()
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_challengeslist)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        challengeAdapter = ChallengesListViewAdapter(null, true, viewUserChallengesOnly, userId)
        challengeAdapter?.getOpenDetailFragmentFlowable()?.subscribe(Consumer { openDetailFragment(it) }, RxErrorHandler.handleEmptyError())
                ?.let { compositeSubscription.add(it) }

        swipeRefreshLayout?.setOnRefreshListener(this)

        recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.activity)
        recyclerView?.adapter = challengeAdapter
        if (!viewUserChallengesOnly) {
            this.recyclerView?.setBackgroundResource(R.color.white)
        }

        compositeSubscription.add(userRepository.getUser().subscribe(Consumer { this.user = it}, RxErrorHandler.handleEmptyError()))

        recyclerView?.setEmptyView(emptyView)
        recyclerView?.itemAnimator = SafeDefaultItemAnimator()

        challengeAdapter?.updateUnfilteredData(challenges)
        loadLocalChallenges()

        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1)) {
                    retrieveChallengesPage()
                }
            }
        })
    }

    private fun openDetailFragment(challengeID: String) {
        MainNavigationController.navigate(ChallengesOverviewFragmentDirections.openChallengeDetail(challengeID))
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onRefresh() {
        nextPageToLoad = 0
        loadedAllData = false
        retrieveChallengesPage(true)
    }

    private fun setRefreshing(state: Boolean) {
        swipeRefreshLayout?.isRefreshing = state
    }

    private fun loadLocalChallenges() {
        val observable: Flowable<RealmResults<Challenge>> = if (viewUserChallengesOnly && user != null) {
            challengeRepository.getUserChallenges(user?.id ?: "")
        } else {
            challengeRepository.getChallenges()
        }

        compositeSubscription.add(observable.firstElement().subscribe(Consumer { challenges ->
            if (challenges.size == 0) {
                retrieveChallengesPage()
            }
            this.challenges = challenges
            challengeAdapter?.updateUnfilteredData(challenges)
        }, RxErrorHandler.handleEmptyError()))
    }

    internal fun retrieveChallengesPage(forced: Boolean = false) {
        if ((!forced && swipeRefreshLayout?.isRefreshing == true) || loadedAllData) {
            return
        }
        setRefreshing(true)
        compositeSubscription.add(challengeRepository.retrieveChallenges(nextPageToLoad, viewUserChallengesOnly).doOnComplete {
            setRefreshing(false)
        } .subscribe(Consumer {
            if (it.size < 10) {
                loadedAllData = true
            }
            nextPageToLoad += 1
        }, RxErrorHandler.handleEmptyError()))
    }

    internal fun showFilterDialog() {
        activity?.let {
            ChallengeFilterDialogHolder.showDialog(it,
                    challenges ?: emptyList(),
                    filterOptions, object : Action1<ChallengeFilterOptions> {
                override fun call(t: ChallengeFilterOptions) {
                    changeFilter(t)
                }
            })
        }
    }

    private fun changeFilter(challengeFilterOptions: ChallengeFilterOptions) {
        filterOptions = challengeFilterOptions
        challengeAdapter?.filter(challengeFilterOptions)
    }
}
