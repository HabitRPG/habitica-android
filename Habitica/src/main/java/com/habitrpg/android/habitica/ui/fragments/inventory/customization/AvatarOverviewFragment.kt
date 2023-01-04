package com.habitrpg.android.habitica.ui.fragments.inventory.customization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentComposeScrollingBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.launchCatching
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.SegmentedControl
import com.habitrpg.android.habitica.ui.views.equipment.AvatarCustomizationOverviewView
import com.habitrpg.android.habitica.ui.views.equipment.EquipmentOverviewView
import javax.inject.Inject

open class AvatarOverviewFragment : BaseMainFragment<FragmentComposeScrollingBinding>(),
    AdapterView.OnItemSelectedListener {

    @Inject
    lateinit var userViewModel: MainUserViewModel

    override var binding: FragmentComposeScrollingBinding? = null

    protected var showCustomization = true

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentComposeScrollingBinding {
        return FragmentComposeScrollingBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding?.composeView?.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HabiticaTheme {
                    AvatarOverviewView(userViewModel,
                        showCustomization, !showCustomization,
                        { type, category ->
                        displayCustomizationFragment(type, category)
                    }, { type, category ->
                            displayAvatarEquipmentFragment(type, category)
                        },  { type, equipped, isCostume ->
                        displayEquipmentFragment(type, equipped, isCostume)
                    })
                }
            }
        }
        return view
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun displayCustomizationFragment(type: String, category: String?) {
        MainNavigationController.navigate(
            AvatarOverviewFragmentDirections.openAvatarDetail(
                type,
                category ?: ""
            )
        )
    }

    private fun displayAvatarEquipmentFragment(type: String, category: String?) {
        MainNavigationController.navigate(AvatarOverviewFragmentDirections.openAvatarEquipment(type, category ?: ""))
    }

    private fun displayEquipmentFragment(type: String, equipped: String?, isCostume: Boolean = false) {
        MainNavigationController.navigate(AvatarOverviewFragmentDirections.openEquipmentDetail(type, isCostume, equipped ?: ""))
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val newSize: String = if (position == 0) "slim" else "broad"

        lifecycleScope.launchCatching {
            userRepository.updateUser("preferences.size", newSize)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) { /* no-on */
    }
}

@Composable
fun AvatarOverviewView(userViewModel: MainUserViewModel,
    showCustomization: Boolean = true,
    showEquipment: Boolean = true,
    onCustomizationTap: (String, String?) -> Unit,
    onAvatarEquipmentTap: (String, String?) -> Unit,
    onEquipmentTap: (String, String?, Boolean) -> Unit
    ) {
    val user by userViewModel.user.observeAsState()
    Column(
        Modifier
            .padding(horizontal = 8.dp)
            .padding(bottom = 16.dp)) {
        if (showCustomization) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.avatar_size),
                    style = HabiticaTheme.typography.subtitle2,
                    color = HabiticaTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                SegmentedControl(items = listOf(
                    stringResource(R.string.avatar_size_slim), stringResource(
                        R.string.avatar_size_broad
                    )
                ),
                    defaultSelectedItemIndex = if (user?.preferences?.size == "slim") 0 else 1,
                    onItemSelection = {
                        userViewModel.updateUser(
                            "preferences.size",
                            if (it == 0) "slim" else "broad"
                        )
                    })
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
                    stringResource(R.string.equipped), style = HabiticaTheme.typography.subtitle2,
                    color = HabiticaTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    stringResource(R.string.equip_automatically),
                    style = HabiticaTheme.typography.body2,
                    color = HabiticaTheme.colors.textPrimary
                )
                Switch(checked = user?.preferences?.autoEquip == true, onCheckedChange = {
                    userViewModel.updateUser("preferences.autoEquip", it)
                })
            }
            EquipmentOverviewView(user?.items?.gear?.equipped, { type, equipped ->
                onEquipmentTap(type, equipped, false)
            })
            Row(
                Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.costume),
                    style = HabiticaTheme.typography.subtitle2,
                    color = HabiticaTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    stringResource(R.string.wear_costume),
                    style = HabiticaTheme.typography.body2,
                    color = HabiticaTheme.colors.textPrimary
                )
                Switch(checked = user?.preferences?.costume == true, onCheckedChange = {
                    userViewModel.updateUser("preferences.costume", it)
                })
            }
            AnimatedVisibility(visible = user?.preferences?.costume == true) {
                EquipmentOverviewView(user?.items?.gear?.costume, { type, equipped ->
                    onEquipmentTap(type, equipped, true)
                })
            }
        }
    }
}