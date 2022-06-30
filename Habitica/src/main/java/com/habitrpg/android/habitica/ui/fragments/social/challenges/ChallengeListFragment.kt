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
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.adapter.social.ChallengesListViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.common.habitica.helpers.EmptyItem
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.Flowables
import javax.inject.Inject
import javax.inject.Named

class ChallengeListFragment : BaseFragment<FragmentRefreshRecyclerviewBinding>(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var challengeRepository: ChallengeRepository
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    override var binding: FragmentRefreshRecyclerviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRefreshRecyclerviewBinding {
        return FragmentRefreshRecyclerviewBinding.inflate(inflater, container, false)
    }

    private var challengeAdapter: ChallengesListViewAdapter? = null
    private var viewUserChallengesOnly: Boolean = false

    private var nextPageToLoad = 0
    private var loadedAllData = false

    private var challenges: List<Challenge>? = null
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

        challengeAdapter = ChallengesListViewAdapter(viewUserChallengesOnly, userId)
        challengeAdapter?.getOpenDetailFragmentFlowable()?.subscribe({ openDetailFragment(it) }, RxErrorHandler.handleEmptyError())
            ?.let { compositeSubscription.add(it) }

        binding?.refreshLayout?.setOnRefreshListener(this)

        if (viewUserChallengesOnly) {
            binding?.recyclerView?.emptyItem = EmptyItem(
                getString(R.string.empty_challenge_list),
                getString(R.string.empty_discover_description)
            )
        }
        binding?.recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.activity)
        binding?.recyclerView?.adapter = challengeAdapter
        if (!viewUserChallengesOnly) {
            binding?.recyclerView?.setBackgroundResource(R.color.content_background)
        }

        compositeSubscription.add(
            Flowables.combineLatest(socialRepository.getGroup(Group.TAVERN_ID), socialRepository.getUserGroups("guild")).subscribe(
                {
                    this.filterGroups = mutableListOf()
                    filterGroups?.add(it.first)
                    filterGroups?.addAll(it.second)
                },
                RxErrorHandler.handleEmptyError()
            )
        )

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
        val observable: Flowable<out List<Challenge>> = if (viewUserChallengesOnly) {
            challengeRepository.getUserChallenges()
        } else {
            challengeRepository.getChallenges()
        }

        compositeSubscription.add(
            observable.subscribe(
                { challenges ->
                    if (challenges.isEmpty()) {
                        retrieveChallengesPage()
                    }
                    this.challenges = challenges
                    challengeAdapter?.updateUnfilteredData(challenges)
                },
                RxErrorHandler.handleEmptyError()
            )
        )
    }

    internal fun retrieveChallengesPage(forced: Boolean = false) {
        if ((!forced && binding?.refreshLayout?.isRefreshing == true) || loadedAllData || !this::challengeRepository.isInitialized) {
            return
        }
        setRefreshing(true)
        compositeSubscription.add(
            challengeRepository.retrieveChallenges(nextPageToLoad, viewUserChallengesOnly).doOnComplete {
                setRefreshing(false)
            }.subscribe(
                {
                    if (it.size < 10) {
                        loadedAllData = true
                    }
                    nextPageToLoad += 1
                },
                RxErrorHandler.handleEmptyError()
            )
        )
    }

    internal fun showFilterDialog() {
        activity?.let {
            ChallengeFilterDialogHolder.showDialog(
                it,
                filterGroups ?: emptyList(),
                filterOptions) {
                changeFilter(it)
            }
        }
    }

    private fun changeFilter(challengeFilterOptions: ChallengeFilterOptions) {
        filterOptions = challengeFilterOptions
        challengeAdapter?.filter(challengeFilterOptions)
    }
}
