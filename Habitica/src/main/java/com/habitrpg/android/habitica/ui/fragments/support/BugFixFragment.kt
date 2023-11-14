package com.habitrpg.android.habitica.ui.fragments.support

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentSupportBugFixBinding
import com.habitrpg.android.habitica.databinding.KnownIssueBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.AppTestingLevel
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.jaredrummler.android.device.DeviceName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BugFixFragment : BaseMainFragment<FragmentSupportBugFixBinding>() {
    private var deviceInfo: DeviceName.DeviceInfo? = null
    override var binding: FragmentSupportBugFixBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSupportBugFixBinding {
        return FragmentSupportBugFixBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var appConfigManager: AppConfigManager

    @Inject
    lateinit var userViewModel: MainUserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hidesToolbar = true
        showsBackButton = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            DeviceName.with(context).request { info, _ ->
                deviceInfo = info
            }
        }

        binding?.reportBugButton?.setOnClickListener {
            sendEmail("[Android] Bugreport")
        }

        appConfigManager.knownIssues().forEach { issue ->
            val issueBinding = KnownIssueBinding.inflate(view.context.layoutInflater)
            issueBinding.root.text = issue["title"]
            issueBinding.root.setOnClickListener {
                MainNavigationController.navigate(
                    R.id.FAQDetailFragment,
                    bundleOf(Pair("question", issue["title"]), Pair("answer", issue["text"]))
                )
            }
            binding?.knownIssuesLayout?.addView(issueBinding.root)
        }
    }

    private val versionName: String by lazy {
        try {
            mainActivity?.packageManager?.getPackageInfo(mainActivity?.packageName ?: "", 0)?.versionName
                ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    private val versionCode: Int by lazy {
        try {
            @Suppress("DEPRECATION")
            mainActivity?.packageManager?.getPackageInfo(mainActivity?.packageName ?: "", 0)?.versionCode
                ?: 0
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }

    private fun sendEmail(subject: String) {
        val version = Build.VERSION.SDK_INT
        val deviceName = deviceInfo?.name ?: DeviceName.getDeviceName()
        val manufacturer = deviceInfo?.manufacturer ?: Build.MANUFACTURER
        val newLine = "%0D%0A"
        var bodyOfEmail = Uri.encode("Device: $manufacturer $deviceName") +
            newLine + Uri.encode("Android Version: $version") +
            newLine + Uri.encode(
            "AppVersion: " + getString(
                R.string.version_info,
                versionName,
                versionCode
            )
        )

        if (appConfigManager.testingLevel().name != AppTestingLevel.PRODUCTION.name) {
            bodyOfEmail += " " + Uri.encode(appConfigManager.testingLevel().name)
        }
        bodyOfEmail += newLine + Uri.encode("User ID: ${userViewModel.userID}")

        userViewModel.user.value?.let { user ->
            bodyOfEmail += newLine + Uri.encode("Level: " + (user.stats?.lvl ?: 0)) +
                newLine + Uri.encode(
                "Class: " + (
                    if (user.preferences?.disableClasses == true) {
                        "Disabled"
                    } else {
                        (
                            user.stats?.habitClass
                                ?: "None"
                            )
                    }
                    )
            ) +
                newLine + Uri.encode("Is in Inn: " + (user.preferences?.sleep ?: false)) +
                newLine + Uri.encode("Uses Costume: " + (user.preferences?.costume ?: false)) +
                newLine + Uri.encode("Custom Day Start: " + (user.preferences?.dayStart ?: 0)) +
                newLine + Uri.encode(
                "Timezone Offset: " + (user.preferences?.timezoneOffset ?: 0)
            )
        }

        bodyOfEmail += "%0D%0ADetails:%0D%0A%0D%0A"

        mainActivity?.let {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            val mailto = "mailto:" + appConfigManager.supportEmail() +
                "?subject=" + Uri.encode(subject) +
                "&body=" + bodyOfEmail
            emailIntent.data = Uri.parse(mailto)

            startActivity(Intent.createChooser(emailIntent, "Choose an Email client:"))
        }
    }
}
