package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.ui.adapter.AchievementsAdapter
import com.habitrpg.android.habitica.ui.viewmodels.AchievementViewModel
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AchievementsFragment :
    BaseMainFragment<FragmentRefreshRecyclerviewBinding>(),
    SwipeRefreshLayout.OnRefreshListener,
    MenuProvider {
    @Inject
    lateinit var userViewModel: MainUserViewModel

    val viewModel: AchievementViewModel by viewModels()

    override var binding: FragmentRefreshRecyclerviewBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentRefreshRecyclerviewBinding = FragmentRefreshRecyclerviewBinding.inflate(inflater, container, false)

    private var menuID: Int = 0
    private lateinit var adapter: AchievementsAdapter
    private var useGridLayout = false
        set(value) {
            field = value
            adapter.useGridLayout = value
            adapter.notifyDataSetChanged()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = GridLayoutManager(mainActivity, 3)
        binding?.recyclerView?.layoutManager = layoutManager
        binding?.recyclerView?.adapter = adapter
        adapter.useGridLayout = useGridLayout
        context?.let {
            binding?.recyclerView?.background =
                ContextCompat.getColor(it, R.color.content_background).toDrawable()
        }

        layoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int = viewModel.itemSizeForType(adapter.getItemViewType(position))
            }

        binding?.refreshLayout?.setOnRefreshListener(this)

        viewLifecycleOwner.lifecycleScope.launchCatching {
            viewModel.achievements.collect {
                adapter.entries = it
                adapter.notifyDataSetChanged()
            }
        }

        activity?.addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onRefresh() {
        viewLifecycleOwner.lifecycleScope.launchCatching {
            userRepository.retrieveAchievements()
            binding?.refreshLayout?.isRefreshing = false
        }
    }

    override fun onCreateMenu(
        menu: Menu,
        menuInflater: MenuInflater,
    ) {
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
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == menuID) {
            useGridLayout = !useGridLayout
            mainActivity?.invalidateOptionsMenu()
            return true
        }
        return false
    }
}
