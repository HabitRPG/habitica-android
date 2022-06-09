package com.habitrpg.wearos.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.wear.widget.WearableLinearLayoutManager
import com.habitrpg.wearos.habitica.R
import com.habitrpg.wearos.habitica.databinding.ActivitySettingsBinding
import com.habitrpg.wearos.habitica.ui.adapters.SettingsAdapter
import com.habitrpg.wearos.habitica.ui.adapters.SettingsItem
import com.habitrpg.wearos.habitica.ui.viewmodels.SettingsViewModel
import com.habitrpg.wearos.habitica.util.HabiticaScrollingLayoutCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity: BaseActivity<ActivitySettingsBinding, SettingsViewModel>() {
    override val viewModel: SettingsViewModel by viewModels()
    private val adapter = SettingsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        binding.root.apply {
            layoutManager =
                WearableLinearLayoutManager(this@SettingsActivity, HabiticaScrollingLayoutCallback())
            adapter = this@SettingsActivity.adapter
        }

        adapter.data = buildSettings()
    }

    private fun logout() {
        viewModel.logout()

        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun buildSettings(): List<SettingsItem> {
        return listOf(
            SettingsItem(
                "logout",
                getString(R.string.logout),
                SettingsItem.Types.DESTRUCTIVE_BUTTON,
                null
            ) {
                logout()
            }
        )
    }
}