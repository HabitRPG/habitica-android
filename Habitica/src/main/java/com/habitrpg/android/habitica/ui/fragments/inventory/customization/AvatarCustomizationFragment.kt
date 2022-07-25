package com.habitrpg.android.habitica.ui.fragments.inventory.customization

import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
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
import com.habitrpg.android.habitica.helpers.RxErrorHandler
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
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.kotlin.combineLatest
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
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

    private val currentFilter = BehaviorSubject.create<CustomizationFilter>()
    private val ownedCustomizations = PublishSubject.create<List<OwnedCustomization>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        showsBackButton = true
        compositeSubscription.add(
            adapter.getSelectCustomizationEvents()
                .flatMap { customization ->
                    if (customization.type == "background") {
                        userRepository.unlockPath(customization)
                            .flatMap { userRepository.retrieveUser(false, true, true) }
                    } else {
                        userRepository.useCustomization(customization.type ?: "", customization.category, customization.identifier ?: "")
                    }
                }
                .subscribe({ }, RxErrorHandler.handleEmptyError())
        )

        compositeSubscription.add(
            this.inventoryRepository.getInAppRewards()
                .map { rewards -> rewards.map { it.key } }
                .subscribe({ adapter.setPinnedItemKeys(it) }, RxErrorHandler.handleEmptyError())
        )

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
        }
        adapter.customizationType = type
        binding?.refreshLayout?.setOnRefreshListener(this)
        layoutManager.justifyContent = JustifyContent.CENTER
        layoutManager.alignItems = AlignItems.FLEX_START
        binding?.recyclerView?.layoutManager = layoutManager

        binding?.recyclerView?.addItemDecoration(MarginDecoration(context))

        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
        this.loadCustomizations()

        userViewModel.user.observe(viewLifecycleOwner) { updateUser(it) }
        currentFilter.onNext(CustomizationFilter())

        binding?.recyclerView?.doOnLayout {
            adapter.columnCount = it.width / (80.dpToPx(context))
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
        if (currentFilter.value?.isFiltering != true) {
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
        compositeSubscription.add(
            customizationRepository.getCustomizations(type, category, false)
                .combineLatest(
                    currentFilter.toFlowable(BackpressureStrategy.DROP),
                    ownedCustomizations.toFlowable(BackpressureStrategy.DROP)
                )
                .subscribe(
                    { (customizations, filter, ownedCustomizations) ->
                        adapter.ownedCustomizations = ownedCustomizations.map { it.key + "_" + it.type + "_" + it.category }
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
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
        if (type == "hair" && (category == "beard" || category == "mustache")) {
            val otherCategory = if (category == "mustache") "beard" else "mustache"
            compositeSubscription.add(customizationRepository.getCustomizations(type, otherCategory, true).subscribe({ adapter.additionalSetItems = it }, RxErrorHandler.handleEmptyError()))
        }
    }

    private fun shouldSkip(
        filter: CustomizationFilter,
        ownedCustomizations: List<OwnedCustomization>,
        customization: Customization
    ): Boolean {
        return if (filter.onlyPurchased && ownedCustomizations.find { it.key == customization.identifier } == null) {
            true
        } else filter.months.isNotEmpty() && !filter.months.contains(customization.customizationSetName?.substringAfter('.'))
    }

    fun updateUser(user: User?) {
        if (user == null) return
        this.updateActiveCustomization(user)
        ownedCustomizations.onNext(user.purchased?.customizations?.filter { it.type == this.type && it.purchased })
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
        compositeSubscription.add(
            userRepository.retrieveUser(withTasks = false, forced = true).subscribe(
                {
                    binding?.refreshLayout?.isRefreshing = false
                },
                RxErrorHandler.handleEmptyError()
            )
        )
    }

    fun showFilterDialog() {
        val filter = currentFilter.value ?: CustomizationFilter()
        val context = context ?: return
        val dialog = HabiticaBottomSheetDialog(context)
        val binding = BottomSheetBackgroundsFilterBinding.inflate(layoutInflater)
        binding.showMeWrapper.check(if (filter.onlyPurchased) R.id.show_purchased_button else R.id.show_all_button)
        binding.showMeWrapper.setOnCheckedChangeListener { _, checkedId ->
            filter.onlyPurchased = checkedId == R.id.show_purchased_button
            currentFilter.onNext(filter)
        }
        binding.clearButton.setOnClickListener {
            currentFilter.onNext(CustomizationFilter())
            dialog.dismiss()
        }
        if (type == "background") {
            binding.sortByWrapper.check(if (filter.ascending) R.id.oldest_button else R.id.newest_button)
            binding.sortByWrapper.setOnCheckedChangeListener { _, checkedId ->
                filter.ascending = checkedId == R.id.oldest_button
                currentFilter.onNext(filter)
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
            if (!isChecked && filter.months.contains(identifier)) {
                button.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                filter.months.remove(identifier)
            } else if (isChecked && !filter.months.contains(identifier)) {
                button.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                filter.months.add(identifier)
            }
            currentFilter.onNext(filter)
        }
    }
}
