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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.CustomizationRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.BottomSheetBackgroundsFilterBinding
import com.habitrpg.android.habitica.databinding.FragmentComposeBinding
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.CustomizationFilter
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.user.OwnedCustomization
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.PixelArtView
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaBottomSheetDialog
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.setTintWith
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.ComposableAvatarView
import com.habitrpg.shared.habitica.models.Avatar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class CustomizationViewModel : ViewModel() {
    var type: String? = null
    var category: String? = null

    val customizations = mutableStateListOf<Customization>()
    val activeCustomization = mutableStateOf<String?>(null)

    val userSize = mutableStateOf("slim")
    val hairColor = mutableStateOf<String?>(null)

    val typeNameId: Int
        get() = when (type) {
            "shirt" -> R.string.avatar_shirts
            "skin" -> R.string.avatar_skins
            "hair" -> {
                when (category) {
                    "color" -> R.string.avatar_hair_colors
                    "base" -> R.string.avatar_hair_styles
                    "bangs" -> R.string.avatar_hair_bangs
                    "mustache" -> R.string.avatar_mustaches
                    "beard" -> R.string.avatar_beards
                    "flower" -> R.string.avatar_accents
                    else -> R.string.avatar_hair
                }
            }

            "background" -> R.string.standard_backgrounds
            else -> R.string.customizations
        }
}

@AndroidEntryPoint
class ComposeAvatarCustomizationFragment :
    BaseMainFragment<FragmentComposeBinding>(),
    SwipeRefreshLayout.OnRefreshListener {

    private var filterMenuItem: MenuItem? = null
    override var binding: FragmentComposeBinding? = null

    private val viewModel: CustomizationViewModel by viewModels()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentComposeBinding {
        return FragmentComposeBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var configManager: AppConfigManager

    @Inject
    lateinit var customizationRepository: CustomizationRepository

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @Inject
    lateinit var userViewModel: MainUserViewModel

    var type: String? = null
    var category: String? = null
    private var activeCustomization: String? = null

    private val currentFilter = MutableStateFlow(CustomizationFilter(false, true))
    private val ownedCustomizations = MutableStateFlow<List<OwnedCustomization>>(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        showsBackButton = true
        hidesToolbar = true

        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding?.composeView?.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HabiticaTheme {
                    val userSize by viewModel.userSize
                    val hairColor by viewModel.hairColor
                    val activeCustomization by viewModel.activeCustomization
                    val avatar by userViewModel.user.observeAsState()
                    AvatarCustomizationView(avatar = avatar, configManager = configManager, viewModel.customizations, userSize, hairColor, type, stringResource(viewModel.typeNameId), activeCustomization) { customization ->
                        lifecycleScope.launchCatching {
                            if (customization.identifier?.isNotBlank() != true) {
                                userRepository.useCustomization(type ?: "", category, activeCustomization ?: "")
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
                }
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val args = ComposeAvatarCustomizationFragmentArgs.fromBundle(it)
            type = args.type
            viewModel.type = type
            if (args.category.isNotEmpty()) {
                category = args.category
                viewModel.category = category
            }
            currentFilter.value.ascending = type != "background"
        }
        this.loadCustomizations()

        userViewModel.user.observe(viewLifecycleOwner) { updateUser(it) }

        lifecycleScope.launchCatching {
            currentFilter.collect {
                Log.e("NewFilter", it.toString())
            }
        }

        Analytics.sendNavigationEvent("$type screen")
    }

    override fun onDestroy() {
        customizationRepository.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_list_customizations, menu)

        filterMenuItem = menu.findItem(R.id.action_filter)
        if (type == "background") {
            updateFilterIcon()
        } else {
            filterMenuItem?.isVisible = false
        }

        mainActivity?.toolbar?.let {
            val color = ContextCompat.getColor(requireContext(), R.color.window_background)
            ToolbarColorHelper.colorizeToolbar(it, mainActivity, backgroundColor = color)
            requireActivity().window.statusBarColor = color
        }
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

    private fun loadCustomizations() {
        val type = this.type ?: return
        lifecycleScope.launchCatching {
            customizationRepository.getCustomizations(type, category, false)
                .combine(currentFilter) { customizations, filter -> Pair(customizations, filter) }
                .combine(ownedCustomizations) { pair, ownedCustomizations ->
                    val ownedKeys = ownedCustomizations.map { it.key }
                    return@combine Pair(pair.first.filter { ownedKeys.contains(it.identifier) || (it.price ?: 0) == 0 }, pair.second)
                }
                .map { (customizations, filter) ->
                    var displayedCustomizations = customizations
                    if (filter.isFiltering) {
                        displayedCustomizations = mutableListOf<Customization>()
                        for (customization in customizations) {
                            if (shouldSkip(filter, customization)) continue
                            displayedCustomizations.add(customization)
                        }
                    }
                    if (!filter.ascending) {
                        displayedCustomizations.reversed()
                    } else {
                        displayedCustomizations
                    }
                }
                .collect { customizations ->
                    viewModel.customizations.clear()
                    viewModel.customizations.addAll(customizations)
                }
        }
    }

    private fun shouldSkip(
        filter: CustomizationFilter,
        customization: Customization
    ): Boolean {
        return if (filter.onlyPurchased) {
            true
        } else {
            filter.months.isNotEmpty() && !filter.months.contains(customization.customizationSet?.substringAfter('.'))
        }
    }

    fun updateUser(user: User?) {
        if (user == null) return
        this.updateActiveCustomization(user)
        ownedCustomizations.value = user.purchased?.customizations?.filter { it.type == this.type && it.purchased } ?: emptyList()
        viewModel.userSize.value = user.preferences?.size ?: "slim"
        viewModel.hairColor.value = user.preferences?.hair?.color
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
            viewModel.activeCustomization.value = activeCustomization
        }
    }

    override fun onRefresh() {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(true, true)
        }
    }

    private fun showFilterDialog() {
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

@Composable
private fun AvatarCustomizationView(avatar: Avatar?, configManager: AppConfigManager, customizations: List<Customization>, userSize: String, hairColor: String?, type: String?, typeName: String, activeCustomization: String?, onSelect: (Customization) -> Unit) {
    val nestedScrollInterop = rememberNestedScrollInteropConnection()
    val totalWidth = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (totalWidth - (84.dp * 3)) / 2
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(colorResource(R.color.window_background))) {
            ComposableAvatarView(
                avatar = avatar, configManager = configManager, modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(140.dp, 147.dp)
            )
            Box(
                Modifier
                    .background(colorResource(R.color.content_background), RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .fillMaxWidth()
                    .height(22.dp)
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(76.dp),
            horizontalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            modifier = Modifier
                .nestedScroll(nestedScrollInterop)
                .background(colorResource(R.color.content_background))
        ) {
            item(span = { GridItemSpan(3) }) {
                Text(
                    typeName.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.text_ternary),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
            }
            if (customizations.size > 1) {
                items(customizations) { customization ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(4.dp)
                            .border(if (activeCustomization == customization.identifier) 2.dp else 0.dp, if (activeCustomization == customization.identifier) HabiticaTheme.colors.tintedUiMain else colorResource(R.color.transparent), RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onSelect(customization)
                            }
                            .background(colorResource(id = R.color.window_background))) {
                        if (customization.identifier.isNullOrBlank() || customization.identifier == "0") {
                            Image(painterResource(R.drawable.empty_slot), contentDescription = null, contentScale = ContentScale.None, modifier = Modifier.size(68.dp))
                        } else {
                            PixelArtView(
                                imageName = customization.getImageName(userSize, hairColor),
                                Modifier.size(68.dp)
                            )
                        }
                    }
                }
            }
            item(span = { GridItemSpan(3) }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 40.dp).clickable {
                    MainNavigationController.navigate(R.id.customizationsShopFragment)
                }) {
                    Image(
                        painterResource(if (type == "backgrounds") R.drawable.customization_background else R.drawable.customization_mix),
                        null, modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (customizations.size <= 1) {
                        Text(stringResource(R.string.customizations_no_owned), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colorResource(R.color.text_secondary))
                        Text(stringResource(R.string.customization_shop_check_out), fontSize = 13.sp, color = colorResource(R.color.text_ternary), textAlign = TextAlign.Center)
                    } else {
                        Text(stringResource(R.string.looking_for_more), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colorResource(R.color.text_secondary))
                        Text(stringResource(R.string.customization_shop_more), fontSize = 13.sp, color = colorResource(R.color.text_ternary), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}