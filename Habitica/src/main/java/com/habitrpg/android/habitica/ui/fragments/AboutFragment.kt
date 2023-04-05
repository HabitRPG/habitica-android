package com.habitrpg.android.habitica.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.core.net.toUri
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentAboutBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.common.habitica.extensions.DataBindingUtils
import com.plattysoft.leonids.ParticleSystem
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : BaseMainFragment<FragmentAboutBinding>() {

    @Inject
    lateinit var appConfigManager: AppConfigManager


    private val privacyPolicyLink = "https://habitica.com/static/privacy"
    private val termsLink = "https://habitica.com/static/terms"
    private val androidSourceCodeLink = "https://github.com/HabitRPG/habitrpg-android/"
    private val twitterLink = "https://twitter.com/habitica"

    private var versionNumberTappedCount = 0

    private fun openGooglePlay() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = "market://details?id=com.habitrpg.android.habitica".toUri()
        startActivity(intent)
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAboutBinding {
        return FragmentAboutBinding.inflate(layoutInflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding?.versionInfo?.setOnClickListener {
            versionNumberTappedCount += 1
            when (versionNumberTappedCount) {
                1 -> context?.let { context ->
                    Toast.makeText(context, "Oh! You tapped me!", Toast.LENGTH_SHORT).show()
                }
                in 5..7 -> context?.let { context ->
                    Toast.makeText(context, "Only ${8 - versionNumberTappedCount} taps left!", Toast.LENGTH_SHORT).show()
                }
                8 -> {
                    context?.let { context ->
                        Toast.makeText(context, "You were blessed with cats!", Toast.LENGTH_SHORT).show()
                    }
                    doTheThing()
                }
            }
        }
    }

    private val versionName: String by lazy {
        try {
            @Suppress("DEPRECATION")
            mainActivity?.packageManager?.getPackageInfo(mainActivity?.packageName ?: "", 0)?.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    private val versionCode: Int by lazy {
        try {
            @Suppress("DEPRECATION")
            mainActivity?.packageManager?.getPackageInfo(mainActivity?.packageName ?: "", 0)?.versionCode ?: 0
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.versionInfo?.text = getString(R.string.version_info, versionName, versionCode)

        if (appConfigManager.lastVersionCode() > versionCode) {
            binding?.updateAvailableWrapper?.visibility = View.VISIBLE
            binding?.updateAvailableTextview?.text = getString(R.string.update_available, appConfigManager.lastVersionNumber(), appConfigManager.lastVersionCode())
        } else {
            binding?.updateAvailableWrapper?.visibility = View.GONE
        }

        binding?.privacyPolicyButton?.setOnClickListener { openBrowserLink(privacyPolicyLink) }
        binding?.termsButton?.setOnClickListener { openBrowserLink(termsLink) }
        binding?.sourceCodeLink?.setOnClickListener { openBrowserLink(androidSourceCodeLink) }
        binding?.twitter?.setOnClickListener { openBrowserLink(twitterLink) }
        binding?.sourceCodeButton?.setOnClickListener { openBrowserLink(androidSourceCodeLink) }
        binding?.reportBug?.setOnClickListener { MainNavigationController.navigate(R.id.bugFixFragment) }
        binding?.googlePlayStoreButton?.setOnClickListener { openGooglePlay() }
        binding?.updateAvailableWrapper?.setOnClickListener { openGooglePlay() }
    }

    private fun openBrowserLink(url: String) {
        val uriUrl = url.toUri()
        val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
        startActivity(launchBrowser)
    }

    private fun doTheThing() {
        val context = context ?: return
        FirebaseAnalytics.getInstance(context).logEvent("found_easter_egg", null)
        DataBindingUtils.loadImage(context, "Pet-Sabretooth-Base") { bitmap ->
            mainActivity?.runOnUiThread {
                mainActivity?.let {
                    ParticleSystem(it, 50, bitmap, 3000)
                        .setAcceleration(0.00013f, 90)
                        .setSpeedByComponentsRange(-0.08f, 0.08f, 0.05f, 0.1f)
                        .setFadeOut(200, AccelerateInterpolator())
                        .setRotationSpeed(100f)
                        .emitWithGravity(binding?.anchor, Gravity.BOTTOM, 20, 10000)
                }
            }
        }
        DataBindingUtils.loadImage(context, "Pet-Sabretooth-Golden") { bitmap ->
            mainActivity?.runOnUiThread {
                mainActivity?.let {
                    ParticleSystem(it, 50, bitmap, 3000)
                        .setAcceleration(0.00013f, 90)
                        .setSpeedByComponentsRange(-0.08f, 0.08f, 0.05f, 0.1f)
                        .setFadeOut(200, AccelerateInterpolator())
                        .setRotationSpeed(100f)
                        .emitWithGravity(binding?.anchor, Gravity.BOTTOM, 20, 10000)
                }
            }
        }
        DataBindingUtils.loadImage(context, "Pet-Sabretooth-Red") { bitmap ->
            mainActivity?.runOnUiThread {
                mainActivity?.let {
                    ParticleSystem(it, 50, bitmap, 3000)
                        .setAcceleration(0.00013f, 90)
                        .setSpeedByComponentsRange(-0.08f, 0.08f, 0.05f, 0.1f)
                        .setFadeOut(200, AccelerateInterpolator())
                        .setRotationSpeed(100f)
                        .emitWithGravity(binding?.anchor, Gravity.BOTTOM, 20, 10000)
                }
            }
        }
    }

    override var binding: FragmentAboutBinding? = null
}
