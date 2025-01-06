package com.habitrpg.android.habitica.ui.fragments.inventory.equipment

import android.app.SearchManager
import android.database.MatrixCursor
import android.graphics.Color
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.FragmentEquipmentDetailBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.ReviewManager
import com.habitrpg.android.habitica.ui.adapter.inventory.EquipmentRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.KeyboardUtil
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.observeOnce
import com.habitrpg.common.habitica.helpers.EmptyItem
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.ComposableAvatarView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EquipmentDetailFragment :
    BaseMainFragment<FragmentEquipmentDetailBinding>(),
    SwipeRefreshLayout.OnRefreshListener, MenuProvider {
    @Inject
    lateinit var inventoryRepository: InventoryRepository

    override var binding: FragmentEquipmentDetailBinding? = null

    @Inject
    lateinit var userViewModel: MainUserViewModel

    @Inject
    lateinit var reviewManager: ReviewManager

    @Inject
    lateinit var configManager: AppConfigManager

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentEquipmentDetailBinding {
        return FragmentEquipmentDetailBinding.inflate(inflater, container, false)
    }

    var type: String? = null
    var equippedGear: String? = null
    var isCostume: Boolean? = null

    private var searchedText = MutableStateFlow<String?>(null)

    private var adapter: EquipmentRecyclerViewAdapter = EquipmentRecyclerViewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        showsBackButton = true
        hidesToolbar = true

        adapter.onEquip = {
            lifecycleScope.launchCatching {
                inventoryRepository.equipGear(it, isCostume ?: false)

                if (this@EquipmentDetailFragment.isAdded) {
                    userViewModel.user.observeOnce(viewLifecycleOwner) { user ->
                        val parentActivity = mainActivity
                        val totalCheckIns = user?.loginIncentives
                        if (totalCheckIns != null && parentActivity != null) {
                            reviewManager.requestReview(parentActivity, totalCheckIns)
                        }
                    }
                }
            }
        }
        activity?.addMenuProvider(this, viewLifecycleOwner)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = EquipmentDetailFragmentArgs.fromBundle(it)
            type = args.type
            isCostume = args.isCostume
            equippedGear = args.equippedGear
        }
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.recyclerView?.onRefresh = { onRefresh() }
        binding?.recyclerView?.emptyItem =
            EmptyItem(
                getString(R.string.empty_title),
                getString(R.string.empty_equipment_description),
                null,
            ) {
                MainNavigationController.navigate(R.id.marketFragment)
            }

        binding?.avatarHeader?.setContent {
            HabiticaTheme {
                val avatar by userViewModel.user.observeAsState()
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(colorResource(R.color.window_background))) {
                    ComposableAvatarView(
                        avatar = avatar,
                        configManager = configManager,
                        modifier =
                            Modifier
                                .padding(top = 6.dp, bottom = 24.dp)
                                .size(140.dp, 147.dp),
                    )
                    Box(
                        Modifier
                            .background(colorResource(R.color.content_background), RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                            .fillMaxWidth()
                            .height(22.dp),
                    )
                }
            }
        }

        this.adapter.equippedGear = this.equippedGear
        this.adapter.isCostume = this.isCostume
        this.adapter.type = this.type

        binding?.recyclerView?.adapter = this.adapter
        binding?.recyclerView?.layoutManager = LinearLayoutManager(mainActivity)
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()

        type?.let { type ->
            lifecycleScope.launchCatching {
                inventoryRepository.getOwnedEquipment(type)
                    .combine(searchedText) { equipment, query ->
                        if (query.isNullOrBlank()) {
                            return@combine equipment
                        }
                        val tokens = query.split(" ")
                        val tokenCount = tokens.size
                        equipment.filter {
                            var matchCount = 0
                            for (token in tokens) {
                                if (it.text.contains(token, true) || it.notes.contains(token, true)) {
                                    matchCount += 1
                                }
                            }
                            return@filter matchCount == tokenCount
                        }
                    }
                    .map { it.sortedBy { equipment -> equipment.text } }
                    .collect { adapter.data = it }
            }
        }
    }

    override fun onDestroy() {
        inventoryRepository.close()
        KeyboardUtil.dismissKeyboard(requireActivity())
        super.onDestroy()
    }

    override fun onRefresh() {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(true, true)
            binding?.refreshLayout?.isRefreshing = false
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_searchable, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchItem.expandActionView()
        val searchView = searchItem.actionView as SearchView
        val suggestions = arrayOf("Spring Gear", "Summer Gear", "Fall Gear", "Winter Gear", "Subscriber Item", "Enchanted Armoire")
        val from = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
        val to = intArrayOf(android.R.id.text1)
        val suggestionAdapter = SimpleCursorAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, null, from, to, 0)
        val cursor = MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1))
        for ((index, suggestion) in suggestions.withIndex()) {
            cursor.addRow(arrayOf(index, suggestion))
        }
        suggestionAdapter.changeCursor(cursor)
        searchView.suggestionsAdapter = suggestionAdapter
        val textView = searchView.findViewById<AutoCompleteTextView>(R.id.search_src_text)
        textView.threshold = 0
        searchView.setIconifiedByDefault(false)
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.queryHint = getString(R.string.search_equipment)
        searchView.findViewById<View>(androidx.appcompat.R.id.search_plate)
            .setBackgroundColor(Color.TRANSPARENT)
        searchView.background = InsetDrawable(
            AppCompatResources.getDrawable(requireContext(), R.drawable.search_background),
            12.dpToPx(requireContext()),
            0,
            8.dpToPx(requireContext()),
            0
        )

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchedText.value = query
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchedText.value = newText
                val filteredCursor = MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1))
                for ((index, suggestion) in suggestions.withIndex()) {
                    if (suggestion.contains(newText, true)) {
                        filteredCursor.addRow(arrayOf(index, suggestion))
                    }
                }
                suggestionAdapter.changeCursor(filteredCursor)
                return false
            }
        })

        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                val selected = suggestionAdapter.getItem(position)
                searchView.setQuery((selected as MatrixCursor).getString(1), true)
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val selected = suggestionAdapter.getItem(position)
                searchView.setQuery((selected as MatrixCursor).getString(1), true)
                return false
            }
        })

        mainActivity?.toolbar?.let {
            val color = ContextCompat.getColor(requireContext(), R.color.window_background)
            ToolbarColorHelper.colorizeToolbar(it, mainActivity, backgroundColor = color)
            requireActivity().window.statusBarColor = color
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        TODO("Not yet implemented")
    }
}
