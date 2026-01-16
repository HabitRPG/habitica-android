package com.habitrpg.android.habitica.ui.fragments.inventory.customization

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.FragmentComposeBinding
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.PixelArtView
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.ComposableAvatarView
import com.habitrpg.shared.habitica.models.Avatar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AvatarEquipmentViewModel : ViewModel() {
    var type: String? = null
    var category: String? = null

    val items = mutableStateListOf<Any>()
    val activeEquipment = mutableStateOf<String?>(null)

    val typeNameId: Int
        get() =
            when (type) {
                "headAccessory" -> R.string.animal_ears
                "back" -> R.string.animal_tails
                else -> R.string.customizations
            }
}

@AndroidEntryPoint
class ComposeAvatarEquipmentFragment :
    BaseMainFragment<FragmentComposeBinding>() {
    private val viewModel: AvatarEquipmentViewModel by viewModels()

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @Inject
    lateinit var userViewModel: MainUserViewModel

    @Inject
    lateinit var configManager: AppConfigManager

    override var binding: FragmentComposeBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentComposeBinding {
        return FragmentComposeBinding.inflate(inflater, container, false)
    }

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
                    val activeEquipment by viewModel.activeEquipment
                    val avatar by userViewModel.user.observeAsState()
                    AvatarEquipmentView(avatar = avatar, configManager = configManager, viewModel.items, viewModel.type, stringResource(viewModel.typeNameId), activeEquipment) { equipment ->
                        lifecycleScope.launchCatching {
                            if ((equipment.key?.isNotBlank() != true || equipment.key?.endsWith("_0") == true) && equipment.key != activeEquipment) {
                                inventoryRepository.equip(
                                    if (userViewModel.user.value?.preferences?.costume == true) "costume" else "equipped",
                                    activeEquipment ?: ""
                                )
                            } else if (equipment.key?.contains("base_0") == false) {
                                inventoryRepository.equip(
                                    if (userViewModel.user.value?.preferences?.costume == true) "costume" else "equipped",
                                    equipment.key ?: ""
                                )
                            }
                        }
                    }
                }
            }
        }
        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        showsBackButton = true
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val args = ComposeAvatarEquipmentFragmentArgs.fromBundle(it)
            viewModel.type = args.type
            if (args.category.isNotEmpty()) {
                viewModel.category = args.category
            }
        }
        this.loadEquipment()

        userViewModel.user.observe(viewLifecycleOwner) { updateUser(it) }

        Analytics.sendNavigationEvent("${viewModel.type} screen")
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        super.onCreateOptionsMenu(menu, inflater)

        mainActivity?.toolbar?.let {
            val color = ContextCompat.getColor(requireContext(), R.color.window_background)
            ToolbarColorHelper.colorizeToolbar(it, mainActivity, backgroundColor = color,
                appbar = mainActivity?.findViewById(R.id.appbar))
        }
    }

    override fun onResume() {
        if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            navigationBarColor = ContextCompat.getColor(requireContext(), R.color.window_background)
        }
        super.onResume()
    }

    private fun loadEquipment() {
        val type = viewModel.type ?: return
        lifecycleScope.launchCatching {
            inventoryRepository.getEquipmentType(type, viewModel.category ?: "")
                .combine(inventoryRepository.getOwnedEquipment(type).map { it.map { owned -> owned.key } }, ::Pair)
                .collect { (equipment, ownedEquipment) ->
                    viewModel.items.clear()
                    val blank = Equipment()
                    blank.key = "${type}_base_0"
                    viewModel.items.add(blank)
                    viewModel.items.addAll(
                        equipment.filter {
                            ownedEquipment.contains(it.key)
                        }
                    )
                }
        }
    }

    fun updateUser(user: User?) {
        this.updateActiveCustomization(user)
    }

    private fun updateActiveCustomization(user: User?) {
        if (viewModel.type == null || user?.preferences == null) {
            return
        }
        val outfit =
            if (user.preferences?.costume == true) user.items?.gear?.costume else user.items?.gear?.equipped
        val activeEquipment =
            when (viewModel.type) {
                "headAccessory" -> outfit?.headAccessory
                "back" -> outfit?.back
                "eyewear" -> outfit?.eyeWear
                else -> ""
            }
        if (activeEquipment != null) {
            viewModel.activeEquipment.value = activeEquipment
        }
    }
}

@Composable
private fun AvatarEquipmentView(
    avatar: Avatar?,
    configManager: AppConfigManager,
    items: List<Any>,
    type: String?,
    typeName: String,
    activeCustomization: String?,
    onSelect: (Equipment) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isWidthGreaterHeight = configuration.screenWidthDp > configuration.screenHeightDp

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.window_background))) {
            ComposableAvatarView(
                avatar = avatar,
                configManager = configManager,
                modifier =
                Modifier
                    .padding(bottom = if (isWidthGreaterHeight) 8.dp else 24.dp)
                    .size(140.dp, 147.dp)
            )
        }
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        var gridWidth by remember { mutableStateOf(screenWidth) }
        val horizontalPadding = (gridWidth - (84.dp * 3)) / 2
        val density = LocalDensity.current
        val insets = WindowInsets.systemBars.add(WindowInsets.displayCutout).asPaddingValues()
        val ld = LocalLayoutDirection.current
        LazyVerticalGrid(
            columns = GridCells.Adaptive(76.dp),
            horizontalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            modifier =
            Modifier
                .padding(bottom = insets.calculateBottomPadding())
                .background(colorResource(R.color.window_background))
                .padding(start = insets.calculateStartPadding(ld), end = insets.calculateEndPadding(ld))
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(colorResource(R.color.content_background))
                .onGloballyPositioned {
                    gridWidth = with(density) {
                        it.size.width.toDp()
                    }
                }
                .nestedScroll(nestedScrollInterop)
        ) {
            item(span = { GridItemSpan(3) }) {
                Text(
                    typeName.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = R.color.text_ternary),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
            }
            if (items.size > 1) {
                items(items, span = { item -> if (item is Equipment) GridItemSpan(1) else GridItemSpan(3) }) { item ->
                    if (item is Equipment) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier =
                            Modifier
                                .padding(4.dp)
                                .border(if (activeCustomization == item.key) 2.dp else 0.dp, if (activeCustomization == item.key) HabiticaTheme.colors.tintedUiMain else colorResource(R.color.transparent), RoundedCornerShape(8.dp))
                                .size(76.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSelect(item)
                                }
                                .background(colorResource(id = R.color.window_background))
                        ) {
                            if (item.key.isNullOrBlank() || item.key == "0" || item.key?.endsWith("_0") == true || item.key == "none") {
                                Image(painterResource(R.drawable.empty_slot), contentDescription = null, contentScale = ContentScale.None, modifier = Modifier.size(68.dp))
                            } else {
                                PixelArtView(
                                    imageName = "shop_" + item.key,
                                    Modifier.size(68.dp)
                                )
                            }
                        }
                    } else if (item is String) {
                        Text(
                            item.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text_ternary),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp).padding(top = 16.dp)
                        )
                    }
                }
            }
            item(span = { GridItemSpan(3) }) {
                EmptyFooter(type, items.size > 1)
            }
        }
    }
}

@Composable
internal fun EmptyFooter(type: String?, hasItems: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
        Modifier
            .padding(top = 56.dp)
            .clickable {
                MainNavigationController.navigate(R.id.customizationsShopFragment)
            }
    ) {
        Image(
            painterResource(if (type == "background") R.drawable.customization_background else R.drawable.customization_mix),
            null,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (!hasItems) {
            Text(
                stringResource(R.string.customizations_no_owned),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_secondary),
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                buildAnnotatedString {
                    val original = stringResource(id = R.string.customization_shop_check_out)
                    val customizationShopName = stringResource(id = R.string.customization_shop)
                    val customizationShopNameIndex = original.indexOf(customizationShopName)
                    if (customizationShopNameIndex == -1) {
                        append(original)
                        return@buildAnnotatedString
                    }
                    val first = original.substring(0, )
                    val second = original.substring(customizationShopNameIndex + customizationShopName.length, original.length)
                    append(first)
                    withStyle(SpanStyle(color = HabiticaTheme.colors.tintedUiMain)) {
                        append(customizationShopName)
                    }
                    append(second)
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.text_ternary),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                stringResource(R.string.looking_for_more),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_secondary),
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                buildAnnotatedString {
                    val original = stringResource(id = R.string.customization_shop_more)
                    val customizationShopName = stringResource(id = R.string.customization_shop)
                    val customizationShopNameIndex = original.indexOf(customizationShopName)
                    if (customizationShopNameIndex == -1) {
                        append(original)
                        return@buildAnnotatedString
                    }
                    val first = original.substring(0, original.indexOf(customizationShopName))
                    val second = original.substring(customizationShopNameIndex + customizationShopName.length, original.length)
                    append(first)
                    withStyle(SpanStyle(color = HabiticaTheme.colors.tintedUiMain)) {
                        append(customizationShopName)
                    }
                    append(second)
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.text_ternary),
                textAlign = TextAlign.Center
            )
        }
    }
}
