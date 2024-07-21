package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.apiService.MaintenanceApiService
import com.habitrpg.android.habitica.databinding.ActivityMaintenanceBinding
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.helpers.setMarkdown
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MaintenanceActivity : BaseActivity() {
    private lateinit var binding: ActivityMaintenanceBinding

    @Inject
    lateinit var maintenanceService: MaintenanceApiService

    private var isDeprecationNotice: Boolean = false

    override fun getLayoutResId(): Int {
        return R.layout.activity_maintenance
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityMaintenanceBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.extras ?: return

        binding.titleTextView.text = data.getString("title")

        @Suppress("DEPRECATION")
        binding.imageView.setImageURI(data.getString("imageUrl")?.toUri())
        binding.descriptionTextView.setMarkdown(data.getString("description"))
        binding.descriptionTextView.movementMethod = LinkMovementMethod.getInstance()

        isDeprecationNotice = data.getBoolean("deprecationNotice")
        if (isDeprecationNotice) {
            binding.playStoreButton.visibility = View.VISIBLE
        } else {
            binding.playStoreButton.visibility = View.GONE
        }

        binding.playStoreButton.setOnClickListener { openInPlayStore() }
    }

    override fun onResume() {
        super.onResume()
        if (!isDeprecationNotice) {
            lifecycleScope.launchCatching {
                val maintenanceResponse = maintenanceService.getMaintenanceStatus()
                if (maintenanceResponse?.activeMaintenance == false) {
                    finish()
                }
            }
        }
    }

    private fun openInPlayStore() {
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$appPackageName".toUri()))
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=$appPackageName".toUri(),
                ),
            )
        }
    }
}
