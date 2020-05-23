package com.habitrpg.android.habitica.ui.fragments.support

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.FAQRepository
import com.habitrpg.android.habitica.databinding.FragmentSupportMainBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.AppTestingLevel
import com.habitrpg.android.habitica.helpers.DeviceName
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import io.reactivex.Completable
import javax.inject.Inject
import javax.inject.Named


class SupportMainFragment : BaseMainFragment() {
    private var deviceInfo: DeviceName.DeviceInfo? = null

    private lateinit var binding: FragmentSupportMainBinding
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String
    @Inject
    lateinit var faqRepository: FAQRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        hidesToolbar = true
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentSupportMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.usingHabiticaWrapper.setOnClickListener {
            MainNavigationController.navigate(R.id.FAQOverviewFragment)
        }
        binding.bugsFixesWrapper.setOnClickListener {
            MainNavigationController.navigate(R.id.bugFixFragment)
        }
        binding.suggestionsFeedbackWrapper.setOnClickListener {
            sendEmail("[Android] Feedback")
        }

        compositeSubscription.add(Completable.fromAction {
            deviceInfo = DeviceName.getDeviceInfo(context)
        }.subscribe())

        binding.resetTutorialButton.setOnClickListener {
            userRepository.resetTutorial(user)
        }
    }

    override fun onDestroy() {
        faqRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private val versionName: String by lazy {
        try {
            @Suppress("DEPRECATION")
            activity?.packageManager?.getPackageInfo(activity?.packageName ?: "", 0)?.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    private val versionCode: Int by lazy {
        try {
            @Suppress("DEPRECATION")
            activity?.packageManager?.getPackageInfo(activity?.packageName ?: "", 0)?.versionCode ?: 0
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }

    private fun sendEmail(subject: String) {
        val version = Build.VERSION.SDK_INT
        val deviceName = deviceInfo?.name ?: DeviceName.getDeviceName()
        val manufacturer = deviceInfo?.manufacturer ?: Build.MANUFACTURER
        var bodyOfEmail = "Device: $manufacturer $deviceName" +
                " \nAndroid Version: $version"+
                " \nAppVersion: " + getString(R.string.version_info, versionName, versionCode)

        if (appConfigManager.testingLevel().name != AppTestingLevel.PRODUCTION.name) {
            bodyOfEmail += " ${appConfigManager.testingLevel().name}"
        }
        bodyOfEmail += " \nUser ID: $userId"

        val user = this.user
        if (user != null) {
            bodyOfEmail += " \nLevel: " + (user.stats?.lvl ?: 0) +
                    " \nClass: " + (if (user.preferences?.disableClasses == true) "Disabled" else (user.stats?.habitClass ?: "None")) +
                    " \nIs in Inn: " + (user.preferences?.sleep ?: false) +
                    " \nUses Costume: " + (user.preferences?.costume ?: false) +
                    " \nCustom Day Start: " + (user.preferences?.dayStart ?: 0) +
                    " \nTimezone Offset: " + (user.preferences?.timezoneOffset ?: 0)
        }

        bodyOfEmail += " \nDetails:\n"

        activity?.let {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            val mailto = "mailto:" + appConfigManager.supportEmail() +
                    "&subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(bodyOfEmail)
            emailIntent.data = Uri.parse(mailto);

            startActivity(Intent.createChooser(emailIntent, "Choose an Email client :"))
        }
    }
}