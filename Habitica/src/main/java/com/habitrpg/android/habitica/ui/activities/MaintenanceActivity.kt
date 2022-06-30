package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.net.toUri
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.MaintenanceApiService
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.databinding.ActivityMaintenanceBinding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.common.habitica.helpers.setMarkdown
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class MaintenanceActivity : BaseActivity() {

    private lateinit var binding: ActivityMaintenanceBinding

    @Inject
    lateinit var maintenanceService: MaintenanceApiService

    @Inject
    lateinit var apiClient: ApiClient

    private var isDeprecationNotice: Boolean = false

    override fun getLayoutResId(): Int {
        return R.layout.activity_maintenance
    }

    override fun getContentView(): View {
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

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onResume() {
        super.onResume()
        if (!isDeprecationNotice) {
            compositeSubscription.add(
                this.maintenanceService.maintenanceStatus
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { maintenanceResponse ->
                            if (maintenanceResponse.activeMaintenance == false) {
                                finish()
                            }
                        },
                        RxErrorHandler.handleEmptyError()
                    )
            )
        }
    }

    private fun openInPlayStore() {
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$appPackageName".toUri()))
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$appPackageName".toUri()))
        }
    }
}
