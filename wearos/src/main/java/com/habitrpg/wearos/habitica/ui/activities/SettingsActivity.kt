package com.habitrpg.wearos.habitica.ui.activities

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.wear.widget.WearableLinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivitySettingsBinding
import com.habitrpg.wearos.habitica.ui.adapters.SettingsAdapter
import com.habitrpg.wearos.habitica.ui.adapters.SettingsItem
import com.habitrpg.wearos.habitica.ui.viewmodels.SettingsViewModel
import com.habitrpg.wearos.habitica.ui.views.TextActionChipView
import com.habitrpg.wearos.habitica.util.HabiticaScrollingLayoutCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : BaseActivity<ActivitySettingsBinding, SettingsViewModel>() {
    override val viewModel: SettingsViewModel by viewModels()
    private val adapter = SettingsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        binding.recyclerView.apply {
            layoutManager =
                WearableLinearLayoutManager(this@SettingsActivity, HabiticaScrollingLayoutCallback())
            adapter = this@SettingsActivity.adapter
        }

        adapter.data = buildSettings()
        adapter.title = getString(R.string.settings)
        lifecycleScope.launch {
            appStateManager.isAppConnected.collect {
                adapter.isDisconnected = !it
            }
        }
    }

    private fun logout() {
        viewModel.logout()
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build()
            val client = GoogleSignIn.getClient(this, gso)
            client.signOut()
        } catch (_: Exception) {
        }

        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun buildSettings(): List<SettingsItem> {
        return listOf(
            SettingsItem(
                "sync",
                getString(R.string.sync_data),
                SettingsItem.Types.BUTTON,
                null
            ) {
                viewModel.resyncData()
            },
            SettingsItem(
                "logout",
                getString(R.string.logout),
                SettingsItem.Types.BUTTON,
                null
            ) {
                showLogoutConfirmation()
            },
            SettingsItem(
                "spacer",
                getString(R.string.settings),
                SettingsItem.Types.SPACER,
                null
            ) {
            },
            SettingsItem(
                "footer",
                getString(R.string.version_info, versionName, versionCode),
                SettingsItem.Types.FOOTER,
                null
            ) {
            }
        )
    }

    private val versionName: String by lazy {
        try {
            @Suppress("DEPRECATION")
            packageManager?.getPackageInfo(packageName ?: "", 0)?.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    private val versionCode: Int by lazy {
        try {
            @Suppress("DEPRECATION")
            packageManager?.getPackageInfo(packageName ?: "", 0)?.versionCode ?: 0
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }

    private fun showLogoutConfirmation() {
        val logoutDialog = Dialog(this)
        val myLayout = layoutInflater.inflate(R.layout.logout_layout, null)
        val positiveButton: TextActionChipView = myLayout.findViewById(R.id.logout_button)
        positiveButton.setOnClickListener {
            logout()
            logoutDialog.dismiss()
        }
        val negativeButton: TextActionChipView = myLayout.findViewById(R.id.cancel_button)
        negativeButton.setOnClickListener {
            logoutDialog.dismiss()
        }
        logoutDialog.setContentView(myLayout)
        logoutDialog.show()
    }
}
