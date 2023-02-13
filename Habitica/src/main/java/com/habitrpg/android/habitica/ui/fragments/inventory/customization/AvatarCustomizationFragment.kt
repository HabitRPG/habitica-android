package com.habitrpg.android.habitica.ui.fragments.inventory.customization

import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection.ROW
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.CustomizationRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.BottomSheetBackgroundsFilterBinding
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.extensions.setTintWith
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.helpers.launchCatching
import com.habitrpg.android.habitica.models.CustomizationFilter
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.user.OwnedCustomization
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.CustomizationRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaBottomSheetDialog
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.getThemeColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class AvatarCustomizationFragment :
    BaseMainFragment<FragmentRefreshRecyclerviewBinding>(),
    SwipeRefreshLayout.OnRefreshListener {

    private var filterMenuItem: MenuItem? = null
    override var binding: FragmentRefreshRecyclerviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRefreshRecyclerviewBinding {
        return FragmentRefreshRecyclerviewBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var customizationRepository: CustomizationRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var userViewModel: MainUserViewModel

    var type: String? = null
    var category: String? = null
    private var activeCustomization: String? = null

    internal var adapter: CustomizationRecyclerViewAdapter = CustomizationRecyclerViewAdapter()
    internal var layoutManager: FlexboxLayoutManager = FlexboxLayoutManager(activity, ROW)

    private val currentFilter = MutableStateFlow(CustomizationFilter(false, true))
    private val ownedCustomizations = MutableStateFlow<List<OwnedCustomization>>(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        showsBackButton = true
        adapter.onCustomizationSelected = { customization ->
            lifecycleScope.launchCatching {
                if (customization.identifier?.isNotBlank() != true) {
                    userRepository.useCustomization(customization.type ?: "", customization.category, activeCustomization ?: "")
                } else if (customization.type == "background" && ownedCustomizations.value.firstOrNull { it.key == customization.identifier } == null) {
                    userRepository.unlockPath(customization)
                    userRepository.retrieveUser(false, true, true)
                } else {
                    userRepository.useCustomization(
                        customization.type ?: "",
                        customization.category,
                        customization.identifier ?: ""
                    )
                }
            }
        }

        lifecycleScope.launchCatching {
            inventoryRepository.getInAppRewards()
                .map { rewards -> rewards.map { it.key } }
                .collect { adapter.setPinnedItemKeys(it) }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val args = AvatarCustomizationFragmentArgs.fromBundle(it)
            type = args.type
            if (args.category.isNotEmpty()) {
                category = args.category
            }
            currentFilter.value.ascending = type != "background"
        }
        adapter.customizationType = type
        binding?.refreshLayout?.setOnRefreshListener(this)
        layoutManager = FlexboxLayoutManager(activity, ROW)
        layoutManager.justifyContent = JustifyContent.CENTER
        layoutManager.alignItems = AlignItems.FLEX_START
        binding?.recyclerView?.layoutManager = layoutManager

        binding?.recyclerView?.addItemDecoration(MarginDecoration(context))

        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
        this.loadCustomizations()

        userViewModel.user.observe(viewLifecycleOwner) { updateUser(it) }

        binding?.recyclerView?.doOnLayout {
            adapter.columnCount = it.width / (80.dpToPx(context))
        }

        lifecycleScope.launchCatching {
            currentFilter.collect {
                Log.e("NewFilter", it.toString())
            }
        }
    }

    override fun onDestroy() {
        customizationRepository.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_list_customizations, menu)

        filterMenuItem = menu.findItem(R.id.action_filter)
        updateFilterIcon()
    }

    private fun updateFilterIcon() {
        if (!currentFilter.value.isFiltering) {
            filterMenuItem?.setIcon(R.drawable.ic_action_filter_list)
            context?.let {
                val filterIcon = ContextCompat.getDrawable(it, R.drawable.ic_action_filter_list)
                filterIcon?.setTintWith(it.getThemeColor(R.attr.headerTextColor), PorterDuff.Mode.MULTIPLY)
                filterMenuItem?.setIcon(filterIcon)
            }
        } else {
            context?.let {
                val filterIcon = ContextCompat.getDrawable(it, R.drawable.ic_filters_active)
                filterIcon?.setTintWith(it.getThemeColor(R.attr.textColorPrimaryDark), PorterDuff.Mode.MULTIPLY)
                filterMenuItem?.setIcon(filterIcon)
            }
        }
    }

    @Suppress("ReturnCount")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun loadCustomizations() {
        val type = this.type ?: return
        lifecycleScope.launchCatching {
            customizationRepository.getCustomizations(type, category, false)
                .combine(currentFilter) { customizations, filter -> Pair(customizations, filter) }
                .combine(ownedCustomizations) { pair, ownedCustomizations -> Triple(pair.first, pair.second, ownedCustomizations) }
                .collect { (customizations, filter, ownedCustomizations) ->
                    adapter.ownedCustomizations =
                        ownedCustomizations.map { it.key + "_" + it.type + "_" + it.category }
                    if (filter.isFiltering) {
                        val displayedCustomizations = mutableListOf<Customization>()
                        for (customization in customizations) {
                            if (shouldSkip(filter, ownedCustomizations, customization)) continue
                            displayedCustomizations.add(customization)
                        }
                        adapter.setCustomizations(
                            if (!filter.ascending) {
                                displayedCustomizations.reversed()
                            } else {
                                displayedCustomizations
                            }
                        )
                    } else {
                        adapter.setCustomizations(
                            if (!filter.ascending) {
                                customizations.reversed()
                            } else {
                                customizations
                            }
                        )
                    }
                }
        }
        if (type == "hair" && (category == "beard" || category == "mustache")) {
            val otherCategory = if (category == "mustache") "beard" else "mustache"
            lifecycleScope.launchCatching {
                customizationRepository.getCustomizations(type, otherCategory, true).collect {
                    adapter.additionalSetItems = it
                }
            }
        }
    }

    private fun shouldSkip(
        filter: CustomizationFilter,
        ownedCustomizations: List<OwnedCustomization>,
        customization: Customization
    ): Boolean {
        return if (filter.onlyPurchased && ownedCustomizations.find { it.key == customization.identifier } == null) {
            true
        } else filter.months.isNotEmpty() && !filter.months.contains(customization.customizationSet?.substringAfter('.'))
    }

    fun updateUser(user: User?) {
        if (user == null) return
        this.updateActiveCustomization(user)
        ownedCustomizations.value = user.purchased?.customizations?.filter { it.type == this.type && it.purchased } ?: emptyList()
        this.adapter.userSize = user.preferences?.size
        this.adapter.hairColor = user.preferences?.hair?.color
        this.adapter.gemBalance = user.gemCount
        this.adapter.avatar = user
        adapter.notifyDataSetChanged()
    }

    private fun updateActiveCustomization(user: User) {
        if (this.type == null || user.preferences == null) {
            return
        }
        val prefs = user.preferences
        val activeCustomization = when (this.type) {
            "skin" -> prefs?.skin
            "shirt" -> prefs?.shirt
            "background" -> prefs?.background
            "chair" -> prefs?.chair
            "hair" -> when (this.category) {
                "bangs" -> prefs?.hair?.bangs.toString()
                "base" -> prefs?.hair?.base.toString()
                "color" -> prefs?.hair?.color
                "flower" -> prefs?.hair?.flower.toString()
                "beard" -> prefs?.hair?.beard.toString()
                "mustache" -> prefs?.hair?.mustache.toString()
                else -> ""
            }
            else -> ""
        }
        if (activeCustomization != null) {
            this.activeCustomization = activeCustomization
            this.adapter.activeCustomization = activeCustomization
        }
    }

    override fun onRefresh() {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(true, true)
            binding?.refreshLayout?.isRefreshing = false
        }
    }

    fun showFilterDialog() {
        val filter = currentFilter.value
        val context = context ?: return
        val dialog = HabiticaBottomSheetDialog(context)
        val binding = BottomSheetBackgroundsFilterBinding.inflate(layoutInflater)
        binding.showMeWrapper.check(if (filter.onlyPurchased) R.id.show_purchased_button else R.id.show_all_button)
        binding.showMeWrapper.setOnCheckedChangeListener { _, checkedId ->
            val newFilter = filter.copy()
            newFilter.onlyPurchased = checkedId == R.id.show_purchased_button
            currentFilter.value = newFilter
        }
        binding.clearButton.setOnClickListener {
            currentFilter.value = CustomizationFilter(false, type != "background")
            dialog.dismiss()
        }
        if (type == "background") {
            binding.sortByWrapper.check(if (filter.ascending) R.id.oldest_button else R.id.newest_button)
            binding.sortByWrapper.setOnCheckedChangeListener { _, checkedId ->
                val newFilter = filter.copy()
                newFilter.ascending = checkedId == R.id.oldest_button
                currentFilter.value = newFilter
            }
            configureMonthFilterButton(binding.januaryButton, 1, filter)
            configureMonthFilterButton(binding.febuaryButton, 2, filter)
            configureMonthFilterButton(binding.marchButton, 3, filter)
            configureMonthFilterButton(binding.aprilButton, 4, filter)
            configureMonthFilterButton(binding.mayButton, 5, filter)
            configureMonthFilterButton(binding.juneButton, 6, filter)
            configureMonthFilterButton(binding.julyButton, 7, filter)
            configureMonthFilterButton(binding.augustButton, 8, filter)
            configureMonthFilterButton(binding.septemberButton, 9, filter)
            configureMonthFilterButton(binding.octoberButton, 10, filter)
            configureMonthFilterButton(binding.novemberButton, 11, filter)
            configureMonthFilterButton(binding.decemberButton, 12, filter)
        } else {
            binding.sortByTitle.visibility = View.GONE
            binding.sortByWrapper.visibility = View.GONE
            binding.monthReleasedTitle.visibility = View.GONE
            binding.monthReleasedWrapper.visibility = View.GONE
        }
        dialog.setContentView(binding.root)
        dialog.setOnDismissListener { updateFilterIcon() }
        dialog.show()
    }

    private fun configureMonthFilterButton(button: CheckBox, value: Int, filter: CustomizationFilter) {
        val identifier = value.toString().padStart(2, '0')
        button.isChecked = filter.months.contains(identifier)
        button.text
        button.setOnCheckedChangeListener { _, isChecked ->
            val newFilter = filter.copy()
            newFilter.months = mutableListOf()
            newFilter.months.addAll(currentFilter.value.months)
            if (!isChecked && newFilter.months.contains(identifier)) {
                button.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                newFilter.months.remove(identifier)
            } else if (isChecked && !newFilter.months.contains(identifier)) {
                button.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                newFilter.months.add(identifier)
            }
            currentFilter.value = newFilter
        }
    }
}
