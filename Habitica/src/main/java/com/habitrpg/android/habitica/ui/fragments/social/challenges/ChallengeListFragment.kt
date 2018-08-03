package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.RelativeLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.activities.ChallengeFormActivity
import com.habitrpg.android.habitica.ui.adapter.social.ChallengesListViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
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

class ChallengeListFragment : BaseMainFragment(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var challengeRepository: ChallengeRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    private val swipeRefreshLayout: SwipeRefreshLayout? by bindView(R.id.refreshLayout)
    private val recyclerView: RecyclerViewEmptySupport? by bindView(R.id.recyclerView)
    private val emptyView: View? by bindView(R.id.emptyView)

    private var challengeAdapter: ChallengesListViewAdapter? = null
    private var viewUserChallengesOnly: Boolean = false


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
                .notNull { compositeSubscription.add(it) }

        swipeRefreshLayout?.setOnRefreshListener(this)

        recyclerView?.layoutManager = LinearLayoutManager(this.activity)
        recyclerView?.adapter = challengeAdapter
        if (!viewUserChallengesOnly) {
            this.recyclerView?.setBackgroundResource(R.color.white)
        }

        recyclerView?.setEmptyView(emptyView)
        recyclerView?.itemAnimator = SafeDefaultItemAnimator()

        challengeAdapter?.updateUnfilteredData(challenges)
        loadLocalChallenges()
    }

    private fun openDetailFragment(challengeID: String) {
        val detailFragment = ChallengeDetailFragment()
        detailFragment.challengeID = challengeID
        this.activity?.displayFragment(detailFragment)
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onRefresh() {
        fetchOnlineChallenges()
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
                fetchOnlineChallenges()
            }
            this.challenges = challenges
            challengeAdapter?.updateUnfilteredData(challenges)
        }, RxErrorHandler.handleEmptyError()))
    }

    private fun fetchOnlineChallenges() {
        setRefreshing(true)
        user.notNull {
            challengeRepository.retrieveChallenges(it).doOnComplete {
                setRefreshing(false)
            } .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
        }
    }

    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.sidebar_challenges)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_list_challenges, menu)


        val badgeLayout = MenuItemCompat.getActionView(menu?.findItem(R.id.action_search)) as? RelativeLayout
        if (badgeLayout != null) {
            val filterCountTextView = badgeLayout.findViewById<TextView>(R.id.badge_textview)
            filterCountTextView.text = null
            filterCountTextView.visibility = View.GONE
            badgeLayout.setOnClickListener { showFilterDialog() }
        }
    }

    private fun showFilterDialog() {
        activity.notNull {
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

    @Suppress("ReturnCount")
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item?.itemId

        when (id) {
            R.id.action_create_challenge -> {
                val intent = Intent(getActivity(), ChallengeFormActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_reload -> {
                fetchOnlineChallenges()
                return true
            }
            R.id.action_search -> {
                showFilterDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
