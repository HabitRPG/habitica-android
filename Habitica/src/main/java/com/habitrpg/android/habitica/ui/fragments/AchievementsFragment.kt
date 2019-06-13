package com.habitrpg.android.habitica.ui.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.ui.adapter.AchievementsAdapter
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.combineLatest
import io.realm.RealmResults
import javax.inject.Inject

class AchievementsFragment: BaseMainFragment(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    private var menuID: Int = 0
    private lateinit var adapter: AchievementsAdapter
    private val layoutManager = GridLayoutManager(activity, 2)
    private var useGridLayout = false
    set(value) {
        field = value
        adapter.useGridLayout = value
        adapter.notifyDataSetChanged()
    }

    private val recyclerView: RecyclerView by bindView(R.id.recyclerView)
    private val refreshLayout: SwipeRefreshLayout by bindView(R.id.refreshLayout)

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        hidesToolbar = true
        super.onCreateView(inflater, container, savedInstanceState)
        adapter = AchievementsAdapter()

        onRefresh()

        return inflater.inflate(R.layout.fragment_refresh_recyclerview, container, false)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        useGridLayout = savedInstanceState?.getBoolean("useGridLayout") ?: false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("useGridLayout", useGridLayout)
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        adapter.useGridLayout = useGridLayout
        context?.let { recyclerView.background = ColorDrawable(ContextCompat.getColor(it, R.color.white)) }

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemViewType(position) == 1) {
                    1
                } else {
                    2
                }
            }
        }

        refreshLayout.setOnRefreshListener(this)

        compositeSubscription.add(userRepository.getAchievements().subscribe(Consumer<RealmResults<Achievement>> {
            val entries = mutableListOf<Any>()
            var lastCategory = ""
            it.forEach { achievement ->
                val categoryIdentifier = achievement.category ?: ""
                if (categoryIdentifier != lastCategory) {
                    val category = Pair(categoryIdentifier, it.count { check ->
                        check.category == categoryIdentifier && check.earned
                    })
                    entries.add(category)
                    lastCategory = categoryIdentifier
                }
                entries.add(achievement)
            }
            adapter.entries = entries
            adapter.notifyDataSetChanged()
        }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(userRepository.getQuestAchievements()
                .combineLatest(userRepository.getQuestAchievements()
                        .map { it.mapNotNull { achievement -> achievement.questKey } }
                        .flatMap { inventoryRepository.getQuestContent(it) })
                .subscribeWithErrorHandler(Consumer { result ->
                    val achievements = result.first.map {achievement ->
                        val questContent = result.second.firstOrNull { achievement.questKey == it.key }
                        achievement.title = questContent?.text
                        achievement
                    }
                    adapter.questAchievements = achievements
                    adapter.notifyDataSetChanged()
                }))
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (useGridLayout) {
            val menuItem = menu?.add(R.string.switch_to_list_view)
            menuID = menuItem?.itemId ?: 0
            menuItem?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menuItem?.setIcon(R.drawable.ic_round_view_list_24px)

        } else {
            val menuItem = menu?.add(R.string.switch_to_grid_view)
            menuID = menuItem?.itemId ?: 0
            menuItem?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menuItem?.setIcon(R.drawable.ic_round_view_module_24px)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == menuID) {
            useGridLayout = !useGridLayout
            activity?.invalidateOptionsMenu()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRefresh() {
        compositeSubscription.add(userRepository.retrieveAchievements().subscribe(Consumer {
        }, RxErrorHandler.handleEmptyError(), Action { refreshLayout.isRefreshing = false }))
    }
}