package com.habitrpg.android.habitica.ui.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.adapter.AchievementsAdapter
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class AchievementsFragment : BaseMainFragment<FragmentRefreshRecyclerviewBinding>(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var userViewModel: MainUserViewModel

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        lifecycleScope.launch {
            userRepository.getAchievements().combine(userRepository.getQuestAchievements()) { achievements, questAchievements ->
                return@combine Pair(achievements, questAchievements)
            }.combine(userRepository.getQuestAchievements()
                .map { it.mapNotNull { achievement -> achievement.questKey } }
                .map { inventoryRepository.getQuestContent(it).firstOrNull() }) { achievements, content ->
                Pair(achievements, content)
            }.collect {
                val achievements = it.first.first
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
                val questAchievements = it.first.second
                entries.add(Pair("Quests completed", questAchievements.size))
                entries.addAll(
                    questAchievements.map { achievement ->
                        val questContent = it.second?.firstOrNull { achievement.questKey == it.key }
                        achievement.title = questContent?.text
                        achievement
                    }
                )

                val user = userViewModel.user.value
                val challengeAchievementCount = user?.challengeAchievements?.size ?: 0
                if (challengeAchievementCount > 0) {
                    entries.add(Pair("Challenges won", challengeAchievementCount))
                    user?.challengeAchievements?.let { it1 -> entries.addAll(it1) }
                }

                adapter.entries = entries
                adapter.notifyDataSetChanged()
            }
        }
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
