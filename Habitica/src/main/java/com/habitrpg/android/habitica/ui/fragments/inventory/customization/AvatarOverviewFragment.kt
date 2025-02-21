package com.habitrpg.android.habitica.ui.fragments.inventory.customization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.FragmentComposeBinding
import com.habitrpg.android.habitica.databinding.FragmentComposeScrollingBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.interactors.ShareAvatarUseCase
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.SegmentedControl
import com.habitrpg.android.habitica.ui.views.equipment.AvatarCustomizationOverviewView
import com.habitrpg.android.habitica.ui.views.equipment.EquipmentOverviewView
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.ComposableAvatarView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@AndroidEntryPoint
open class AvatarOverviewFragment :
    BaseMainFragment<FragmentComposeBinding>(),
    AdapterView.OnItemSelectedListener {
    @Inject
    lateinit var userViewModel: MainUserViewModel

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @Inject
    lateinit var appConfigManager: AppConfigManager

    override var binding: FragmentComposeBinding? = null

    protected var showCustomization = true

    private val battleGearWeapon = mutableStateOf<Equipment?>(null)
    private val costumeWeapon = mutableStateOf<Equipment?>(null)

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentComposeBinding {
        return FragmentComposeBinding.inflate(inflater, container, false)
    }

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hidesToolbar = true
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding?.composeView?.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HabiticaTheme {
                    val avatar by userViewModel.user.observeAsState()
                    Column {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().background(colorResource(R.color.window_background))) {
                            ComposableAvatarView(
                                avatar = avatar,
                                configManager = appConfigManager,
                                modifier =
                                Modifier
                                    .padding(bottom = 24.dp)
                                    .size(140.dp, 147.dp)
                            )
                        }
                        Column(modifier = Modifier
                            .background(colorResource(R.color.window_background))
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                            .background(colorResource(R.color.content_background))
                            .verticalScroll(rememberScrollState())
                            .padding(top = 12.dp)
                            ) {
                            AvatarOverviewView(
                                userViewModel,
                                showCustomization,
                                !showCustomization,
                                battleGearWeapon.value?.twoHanded == true,
                                costumeWeapon.value?.twoHanded == true,
                                { type, category ->
                                    displayCustomizationFragment(type, category)
                                },
                                { type, category ->
                                    displayAvatarEquipmentFragment(type, category)
                                },
                                { type, equipped, isCostume ->
                                    displayEquipmentFragment(type, equipped, isCostume)
                                }
                            )
                            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBarsIgnoringVisibility))
                        }
                    }
                }
            }
        }

        userViewModel.user.map { Pair(it?.items?.gear?.equipped?.weapon, it?.items?.gear?.costume?.weapon) }
            .observe(viewLifecycleOwner) {
                lifecycleScope.launchCatching {
                    battleGearWeapon.value =
                        it.first?.let { key ->
                            inventoryRepository.getEquipment(
                                key
                            ).firstOrNull()
                        }
                    costumeWeapon.value =
                        it.second?.let { key ->
                            inventoryRepository.getEquipment(
                                key
                            ).firstOrNull()
                        }
                }
            }
        return view
    }

    private fun displayCustomizationFragment(
        type: String,
        category: String?
    ) {
        MainNavigationController.navigate(
            AvatarOverviewFragmentDirections.openComposeAvatarDetail(
                type,
                category ?: ""
            )
        )
    }

    private fun displayAvatarEquipmentFragment(
        type: String,
        category: String?
    ) {
        MainNavigationController.navigate(AvatarOverviewFragmentDirections.openComposeAvatarEquipment(type, category ?: ""))
    }

    private fun displayEquipmentFragment(
        type: String,
        equipped: String?,
        isCostume: Boolean = false
    ) {
        MainNavigationController.navigate(AvatarOverviewFragmentDirections.openEquipmentDetail(type, isCostume, equipped ?: ""))
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_share_avatar, menu)

        mainActivity?.toolbar?.let {
            val color = ContextCompat.getColor(requireContext(), R.color.window_background)
            ToolbarColorHelper.colorizeToolbar(it, mainActivity, backgroundColor = color)
            requireActivity().window.statusBarColor = color
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.share_avatar) {
            userViewModel.user.value?.let {
                val usecase = ShareAvatarUseCase()
                lifecycleScope.launchCatching {
                    usecase.callInteractor(
                        ShareAvatarUseCase.RequestValues(
                            requireActivity() as BaseActivity,
                            it,
                            "Check out my avatar on Habitica!",
                            "avatar_customization"
                        )
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemSelected(
        parent: AdapterView<*>,
        view: View?,
        position: Int,
        id: Long
    ) {
        val newSize: String = if (position == 0) "slim" else "broad"

        lifecycleScope.launchCatching {
            userRepository.updateUser("preferences.size", newSize)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) { // no-on
    }
}

@Composable
fun AvatarOverviewView(
    userViewModel: MainUserViewModel,
    showCustomization: Boolean = true,
    showEquipment: Boolean = true,
    battleGearTwoHanded: Boolean = false,
    costumeTwoHanded: Boolean = false,
    onCustomizationTap: (String, String?) -> Unit,
    onAvatarEquipmentTap: (String, String?) -> Unit,
    onEquipmentTap: (String, String?, Boolean) -> Unit
) {
    val user by userViewModel.user.observeAsState()
    Column(
        Modifier
            .padding(horizontal = 8.dp)
            .padding(bottom = 16.dp)
    ) {
        if (showCustomization) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.avatar_size),
                    style = HabiticaTheme.typography.titleMedium,
                    color = HabiticaTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                SegmentedControl(
                    items =
                    listOf(
                        stringResource(R.string.avatar_size_slim),
                        stringResource(
                            R.string.avatar_size_broad
                        )
                    ),
                    defaultSelectedItemIndex = if (user?.preferences?.size == "slim") 0 else 1,
                    onItemSelection = {
                        userViewModel.updateUser(
                            "preferences.size",
                            if (it == 0) "slim" else "broad"
                        )
                    }
                )
            }
            AvatarCustomizationOverviewView(user?.preferences, user?.items?.gear?.equipped, onCustomizationTap, onAvatarEquipmentTap)
        }
        if (showEquipment) {
            Row(
                Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.equipped).uppercase(),
                    style = HabiticaTheme.typography.titleSmall,
                    color = HabiticaTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    stringResource(R.string.equip_automatically),
                    style = HabiticaTheme.typography.bodyMedium,
                    color = HabiticaTheme.colors.textPrimary,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Switch(checked = user?.preferences?.autoEquip == true, onCheckedChange = {
                    userViewModel.updateUser("preferences.autoEquip", it)
                })
            }
            EquipmentOverviewView(user?.items?.gear?.equipped, battleGearTwoHanded, { type, equipped ->
                onEquipmentTap(type, equipped, false)
            })
            Row(
                Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.costume).uppercase(),
                    style = HabiticaTheme.typography.titleSmall,
                    color = HabiticaTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    stringResource(R.string.wear_costume),
                    style = HabiticaTheme.typography.bodyMedium,
                    color = HabiticaTheme.colors.textPrimary,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Switch(checked = user?.preferences?.costume == true, onCheckedChange = {
                    userViewModel.updateUser("preferences.costume", it)
                })
            }
            EquipmentOverviewView(user?.items?.gear?.costume, costumeTwoHanded, { type, equipped ->
                onEquipmentTap(type, equipped, true)
            }, modifier = Modifier.alpha(if (user?.preferences?.costume == true) 1.0f else 0.5f))
        }
    }
}
