package com.habitrpg.android.habitica.ui.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.adapter.AchievementsAdapter
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import io.reactivex.rxjava3.kotlin.Flowables
import io.reactivex.rxjava3.kotlin.combineLatest
import javax.inject.Inject

class AchievementsFragment : BaseMainFragment<FragmentRefreshRecyclerviewBinding>(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    override var binding: FragmentRefreshRecyclerviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRefreshRecyclerviewBinding {
        return FragmentRefreshRecyclerviewBinding.inflate(inflater, container, false)
    }

    private var menuID: Int = 0
    private lateinit var adapter: AchievementsAdapter
    private var useGridLayout = false
        set(value) {
            field = value
            adapter.useGridLayout = value
            adapter.notifyDataSetChanged()
        }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        hidesToolbar = true
        adapter = AchievementsAdapter()
        onRefresh()
        return super.onCreateView(inflater, container, savedInstanceState)
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

        val layoutManager = GridLayoutManager(activity, 2)
        binding?.recyclerView?.layoutManager = layoutManager
        binding?.recyclerView?.adapter = adapter
        adapter.useGridLayout = useGridLayout
        context?.let { binding?.recyclerView?.background = ColorDrawable(ContextCompat.getColor(it, R.color.content_background)) }

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemViewType(position) == 1) {
                    1
                } else {
                    2
                }
            }
        }

        binding?.refreshLayout?.setOnRefreshListener(this)

        compositeSubscription.add(
            userRepository.getAchievements().map { achievements ->
                achievements.sortedBy {
                    if (it.category == "onboarding") {
                        it.index
                    } else {
                        (it.category?.first()?.toInt() ?: 2) * it.index
                    }
                }
            }.combineLatest(
                Flowables.combineLatest(
                    userRepository.getQuestAchievements(),
                    userRepository.getQuestAchievements()
                        .map { it.mapNotNull { achievement -> achievement.questKey } }
                        .flatMap { inventoryRepository.getQuestContent(it) }
                )
            ).subscribe(
                {
                    val achievements = it.first
                    val entries = mutableListOf<Any>()
                    var lastCategory = ""
                    achievements.forEach { achievement ->
                        val categoryIdentifier = achievement.category ?: ""
                        if (categoryIdentifier != lastCategory) {
                            val category = Pair(
                                categoryIdentifier,
                                achievements.count { check ->
                                    check.category == categoryIdentifier && check.earned
                                }
                            )
                            entries.add(category)
                            lastCategory = categoryIdentifier
                        }
                        entries.add(achievement)
                    }
                    val questAchievements = it.second
                    entries.add(Pair("Quests completed", questAchievements.first.size))
                    entries.addAll(
                        questAchievements.first.map { achievement ->
                            val questContent = questAchievements.second.firstOrNull { achievement.questKey == it.key }
                            achievement.title = questContent?.text
                            achievement
                        }
                    )

                    val challengeAchievementCount = user?.challengeAchievements?.size ?: 0
                    if (challengeAchievementCount > 0) {
                        entries.add(Pair("Challenges won", challengeAchievementCount))
                        user?.challengeAchievements?.let { it1 -> entries.addAll(it1) }
                    }

                    adapter.entries = entries
                    adapter.notifyDataSetChanged()
                },
                RxErrorHandler.handleEmptyError()
            )
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (useGridLayout) {
            val menuItem = menu.add(R.string.switch_to_list_view)
            menuID = menuItem?.itemId ?: 0
            menuItem?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menuItem?.setIcon(R.drawable.ic_round_view_list_24px)
            tintMenuIcon(menuItem)
        } else {
            val menuItem = menu.add(R.string.switch_to_grid_view)
            menuID = menuItem?.itemId ?: 0
            menuItem?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menuItem?.setIcon(R.drawable.ic_round_view_module_24px)
            tintMenuIcon(menuItem)
        }
        activity?.findViewById<Toolbar>(R.id.toolbar)?.let {
            ToolbarColorHelper.colorizeToolbar(it, activity, null)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == menuID) {
            useGridLayout = !useGridLayout
            activity?.invalidateOptionsMenu()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRefresh() {
        compositeSubscription.add(
            userRepository.retrieveAchievements().subscribe(
                {
                },
                RxErrorHandler.handleEmptyError(), { binding?.refreshLayout?.isRefreshing = false }
            )
        )
    }
}
