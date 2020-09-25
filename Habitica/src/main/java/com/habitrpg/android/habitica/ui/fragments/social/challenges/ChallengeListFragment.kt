package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentChallengeslistBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.adapter.social.ChallengesListViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.utils.Action1
import io.reactivex.Flowable
import io.reactivex.rxkotlin.combineLatest
import io.realm.RealmResults
import javax.inject.Inject
import javax.inject.Named


class ChallengeListFragment : BaseFragment<FragmentChallengeslistBinding>(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var challengeRepository: ChallengeRepository
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    override var binding: FragmentChallengeslistBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentChallengeslistBinding {
        return FragmentChallengeslistBinding.inflate(inflater, container, false)
    }

    private var challengeAdapter: ChallengesListViewAdapter? = null
    private var viewUserChallengesOnly: Boolean = false

    private var nextPageToLoad = 0
    private var loadedAllData = false

    private var challenges: RealmResults<Challenge>? = null
    private var filterGroups: MutableList<Group>? = null

    private var filterOptions: ChallengeFilterOptions? = null

    fun setViewUserChallengesOnly(only: Boolean) {
        this.viewUserChallengesOnly = only
    }

    override fun onDestroy() {
        challengeRepository.close()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        challengeAdapter = ChallengesListViewAdapter(null, true, viewUserChallengesOnly, userId)
        challengeAdapter?.getOpenDetailFragmentFlowable()?.subscribe({ openDetailFragment(it) }, RxErrorHandler.handleEmptyError())
                ?.let { compositeSubscription.add(it) }

        binding?.refreshLayout?.setOnRefreshListener(this)

        binding?.recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.activity)
        binding?.recyclerView?.adapter = challengeAdapter
        if (!viewUserChallengesOnly) {
            binding?.recyclerView?.setBackgroundResource(R.color.content_background)
        }

        compositeSubscription.add(socialRepository.getGroup(Group.TAVERN_ID).combineLatest(socialRepository.getUserGroups("guild")).subscribe({
            this.filterGroups = mutableListOf()
            filterGroups?.add(it.first)
            filterGroups?.addAll(it.second)
        }, RxErrorHandler.handleEmptyError()))

        binding?.recyclerView?.setEmptyView(binding?.emptyView)
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()

        challengeAdapter?.updateUnfilteredData(challenges)
        loadLocalChallenges()

        binding?.recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        binding?.refreshLayout?.isRefreshing = state
    }

    private fun loadLocalChallenges() {
        val observable: Flowable<RealmResults<Challenge>> = if (viewUserChallengesOnly) {
            challengeRepository.getUserChallenges()
        } else {
            challengeRepository.getChallenges()
        }

        compositeSubscription.add(observable.firstElement().subscribe({ challenges ->
            if (challenges.size == 0) {
                retrieveChallengesPage()
            }
            this.challenges = challenges
            challengeAdapter?.updateUnfilteredData(challenges)
        }, RxErrorHandler.handleEmptyError()))
    }

    internal fun retrieveChallengesPage(forced: Boolean = false) {
        if ((!forced && binding?.refreshLayout?.isRefreshing == true) || loadedAllData) {
            return
        }
        setRefreshing(true)
        compositeSubscription.add(challengeRepository.retrieveChallenges(nextPageToLoad, viewUserChallengesOnly).doOnComplete {
            setRefreshing(false)
        } .subscribe({
            if (it.size < 10) {
                loadedAllData = true
            }
            nextPageToLoad += 1
        }, RxErrorHandler.handleEmptyError()))
    }

    internal fun showFilterDialog() {
        activity?.let {
            ChallengeFilterDialogHolder.showDialog(it,
                    filterGroups ?: emptyList(),
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
